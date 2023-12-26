package hkucs.example.comp3330project

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import kotlin.time.Duration.Companion.seconds


/*
Example usage:
1. Initiate Chatbot
2. Call chatCompletion(question, context), returns response

    suspend fun qna(v: View) {
        var chatbot = Chatbot(key)
        val q = question!!.text.toString()
        val res = chatbot.chatCompletion(q, "You are a nutritionist answering patient's questions. You should keep your answer short and concise. You should reject any question or messages that are unrelated to your profession.")
        answer!!.text = res
    }
 */

// key: OpenAI key
class Chatbot(key: String) {

    val openAI = OpenAI(
        token = key,
        timeout = Timeout(socket = 60.seconds),
        // additional configurations...
    )

    suspend fun chatCompletion(message: String, context: String): String? {

        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = context
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = message
                )
            )
        )
        val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)
        return completion.choices[0].message.content
    }
}
