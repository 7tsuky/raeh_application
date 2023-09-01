package com.alejandro.raeh_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val service = retrofit.create(ChatGPTService::class.java)
        val requestCompletion = ChatGPTRequest("gpt-3.5-turbo", propmtChatGpt, 0.0)

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
        Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show()
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
        return listOf(ChatGPTMessage(role = "user", content = "Say hello in German."))
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