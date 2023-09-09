package com.alejandro.raeh_app

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
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
import java.util.concurrent.TimeUnit


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
    private lateinit var chatGPTButtonFinish: Button

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

        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.connectTimeout(60, TimeUnit.SECONDS) // Set the connect timeout
        httpClientBuilder.readTimeout(60, TimeUnit.SECONDS) // Set the read timeout
        httpClientBuilder.writeTimeout(60, TimeUnit.SECONDS) // Set the write timeout
        var client: OkHttpClient = httpClientBuilder.build()

        retrofit = Retrofit.Builder().client(client)
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
        chatGPTButtonFinish = view.findViewById<Button>(R.id.chatGPTButtonFinish)
        chatGPTButtonFinish.setOnClickListener{
            Navigation.findNavController(view).navigate(R.id.action_chatgptFragment_to_welcomeFragment)
        }

        val service = retrofit.create(ChatGPTService::class.java)
        val requestCompletion = ChatGPTRequest(model="gpt-3.5-turbo", messages=propmtChatGpt, temperature=0.0)

        service.getCompletion(CHAT_GPT_API_KEY, requestCompletion).enqueue(object : Callback<ChatGPTResponse> {
            override fun onResponse(call: Call<ChatGPTResponse>, response: Response<ChatGPTResponse>) {
                if (response.isSuccessful) {
                    callbackAPISucceeded(response.body())
                } else {
                    callbackAPIFailed()
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
            "<html><body>${response?.choices?.get(0)?.message?.content}</body></html>";
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
        var messages: Vector<ChatGPTMessage> = Vector<ChatGPTMessage>()

        messages.add(ChatGPTMessage("system", resources.getString(R.string.chatgpt_role_system)))
        messages.add(ChatGPTMessage("assistant", resources.getString(R.string.chatgpt_role_assistance_1)))
        messages.add(ChatGPTMessage("user", resources.getString(R.string.chatgpt_role_user_1)))
        messages.add(ChatGPTMessage("assistant", resources.getString(R.string.chatgpt_role_assistance_2)))

        for (id in context){
            var node = getNodeById(id)
            var role = "assistant"
            if(node.type == "answer"){
                role = "user"
            }
            var surveyMessage = "(Conversación) " + node.value
            messages.add(ChatGPTMessage(role, surveyMessage))
        }
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
        val model: String,
        val messages: List<ChatGPTMessage>,
        val temperature: Double
    )

    data class ChatGPTResponse(
        val id: String,
        @SerializedName("object") val objectType: String,
        val created: Int,
        val model: String,
        val choices: List<Choice>,
        val usage: Usage
    )

    data class Choice(
        val index: Int,
        val message: Message,
        val finish_reason: String
    )

    data class Message(
        val role: String,
        val content: String
    )

    data class Usage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )

    interface ChatGPTService {
        @Headers("Content-Type: application/json")
        @POST("/v1/chat/completions")
        fun getCompletion(@Header("Authorization") authHeader: String,
                          @Body requestBody: ChatGPTRequest
            ): Call<ChatGPTResponse>
    }

}