package com.alejandro.raeh_app

import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.io.IOException
import java.nio.charset.Charset
import java.util.Vector

class ChatgptFragment : Fragment() {

    // Strings
    private lateinit var CHAT_GPT_API_KEY: String
    private lateinit var CHAT_GPT_BASE_URL: String

    // Members
    private lateinit var surveyGraph: SurveyFragment.Survey
    private lateinit var propmtChatGpt: List<ChatGPTMessage>
    private lateinit var retrofit: Retrofit

    // XML
    private lateinit var chatgptSpinnerContainer: RelativeLayout
    private lateinit var chatgptErrorContainer: RelativeLayout
    private lateinit var chatgptHTMLContainer: RelativeLayout
    private lateinit var chatgptHTMLOutput: WebView

    // Args
    val args: ChatgptFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val jsonMsg = loadJSONFile("survey.json")
        if(jsonMsg != null){
            surveyGraph = Gson().fromJson(jsonMsg, SurveyFragment.Survey::class.java)
        } else {
            Toast.makeText(context, "Error reading JSON survey.", Toast.LENGTH_SHORT).show()
            return
        }

        CHAT_GPT_BASE_URL = resources.getString(R.string.chatgpt_base_url)
        CHAT_GPT_API_KEY = "Bearer " + resources.getString(R.string.chatgpt_api_key)

        var context: Array<Int> = args.surveyContext.toTypedArray()
        propmtChatGpt = assemblePrompt(context)

        retrofit = Retrofit.Builder()
            .baseUrl(CHAT_GPT_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /// Create thread and call aip
        var view = inflater.inflate(R.layout.fragment_chatgpt, container, false)

        chatgptSpinnerContainer = view.findViewById<RelativeLayout>(R.id.chatgptSpinnerContainer)
        chatgptErrorContainer = view.findViewById<RelativeLayout>(R.id.chatgptErrorContainer)
        chatgptHTMLContainer = view.findViewById<RelativeLayout>(R.id.chatgptHTMLContainer)
        chatgptHTMLOutput = view.findViewById<WebView>(R.id.chatgptHTMLOutput)

        val service = retrofit.create(ChatGPTService::class.java)
        val requestCompletion = ChatGPTRequest("gpt-3.5-turbo", propmtChatGpt, 0.0)

        service.getCompletion(CHAT_GPT_API_KEY, requestCompletion).enqueue(object : Callback<ChatGPTResponse> {
            override fun onResponse(call: Call<ChatGPTResponse>, response: Response<ChatGPTResponse>) {
                if (response.isSuccessful) {
                    callbackAPISucceeded(response.body())
                } else {
                    var html = "<h1>Reporte de Atención al Estudiante</h1>\n" +
                            "    <h2>Información del Estudiante</h2>\n" +
                            "    <ul>\n" +
                            "        <li>Edad: 11-13 años</li>\n" +
                            "    </ul>\n" +
                            "    <h2>Síntomas reportados</h2>\n" +
                            "    <ul>\n" +
                            "        <li>Tos productiva</li>\n" +
                            "    </ul>\n" +
                            "    <h2>Diagnóstico</h2>\n" +
                            "    <p>El estudiante presenta una tos productiva.</p>\n" +
                            "    <h2>Tratamiento</h2>\n" +
                            "    <p>Se recomienda al estudiante:</p>\n" +
                            "    <ol>\n" +
                            "        <li>Tomar abundante líquido para mantenerse hidratado.</li>\n" +
                            "        <li>Descansar adecuadamente.</li>\n" +
                            "        <li>Evitar cambios bruscos de temperatura y ambientes con mucho polvo.</li>\n" +
                            "        <li>Utilizar medicamentos para el alivio de la tos (bajo la supervisión de un adulto).</li>\n" +
                            "    </ol>"

                    callbackAPISucceeded(ChatGPTResponse(html))
                    //callbackAPIFailed()
                }
            }
            override fun onFailure(call: Call<ChatGPTResponse>, t: Throwable) {
                callbackAPIFailed()
            }
        })


        return view
    }

    private fun callbackAPISucceeded(response: ChatGPTResponse?){
        val unencodedHtml =
            "<html><body>${response?.response}</body></html>";
        val encodedHtml = Base64.encodeToString(unencodedHtml.toByteArray(), Base64.NO_PADDING)
        chatgptHTMLOutput.loadData(encodedHtml, "text/html", "base64")

        chatgptSpinnerContainer.animate().alpha(0f).setDuration(200).withEndAction {
            chatgptSpinnerContainer.visibility = View.GONE

            chatgptHTMLContainer.alpha = 0f
            chatgptHTMLContainer.visibility = View.VISIBLE
            chatgptHTMLContainer.animate().alpha(1f).duration = 200
        }.start()
    }

    private fun callbackAPIFailed(){
        chatgptSpinnerContainer.animate().alpha(0f).setDuration(200).withEndAction {
            chatgptSpinnerContainer.visibility = View.GONE

            chatgptErrorContainer.alpha = 0f
            chatgptErrorContainer.visibility = View.VISIBLE
            chatgptErrorContainer.animate().alpha(1f).duration = 200
        }.start()
    }

    private fun loadJSONFile(path: String): String? {
        try {
            val inputStream = context?.assets?.open(path)
            val size = inputStream?.available()
            val buffer = ByteArray(size ?: 0)
            inputStream?.read(buffer)
            inputStream?.close()
            return String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
            Toast.makeText(context, "Failed loading the JSON file", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun assemblePrompt(context: Array<Int>): List<ChatGPTMessage> {
        var surveyMessage: String = ""
        for (id in context){
            var node = getNodeById(id)
            surveyMessage += if (node.type == "question") "[Pregunta] " else "[Respuesta] "
            surveyMessage += node.value + " -> "
        }

        var messages: Vector<ChatGPTMessage> = Vector<ChatGPTMessage>()

        messages.add(ChatGPTMessage("system", resources.getString(R.string.chatgpt_role_system)))
        messages.add(ChatGPTMessage("assistant", resources.getString(R.string.chatgpt_role_assistance_1)))
        messages.add(ChatGPTMessage("user", resources.getString(R.string.chatgpt_role_user_1)))
        messages.add(ChatGPTMessage("assistant", resources.getString(R.string.chatgpt_role_assistance_2)))
        messages.add(ChatGPTMessage("user", surveyMessage))
        messages.add(ChatGPTMessage("assistant", resources.getString(R.string.chatgpt_role_assistance_3)))
        messages.add(ChatGPTMessage("user", resources.getString(R.string.chatgpt_role_user_instruction)))

        return messages.toList()
    }

    private fun getNodeById(nodeId: Int): SurveyFragment.Node {
        return surveyGraph.nodes.find { it.id == nodeId }
            ?: throw IllegalStateException("Current node not found in survey graph.")
    }

    data class ChatGPTMessage(
        val role: String,
        val content: String
    )

    data class ChatGPTRequest(
        val Model: String,
        val messages: List<ChatGPTMessage>,
        val temperature: Double
    )

    data class ChatGPTResponse(
        val response: String
    )

    interface ChatGPTService {
        @Headers("Content-Type: application/json")
        @POST("/v1/chat/completions")
        fun getCompletion(@Header("Authorization") authHeader: String,
                          @Body requestBody: ChatGPTRequest
            ): Call<ChatGPTResponse>
    }

}