package hkucs.example.comp3330project

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.FileInputStream


class ChatbotActivity : AppCompatActivity() {
    val key: String = "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"

    // Initiate fields
    var icon: ImageView? = null
    var question: EditText? = null
    var answer: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        // Enable Back Button
        getSupportActionBar()!!.setTitle("AI Nutritionist")
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        question = findViewById<EditText>(R.id.question)
        answer = findViewById<TextView>(R.id.answer)
        icon = findViewById<ImageView>(R.id.iconAI)


        val editProfile = findViewById<Button>(R.id.editInfo)
        editProfile.setOnClickListener {
            val myIntent = Intent(this, FormActivity::class.java)
            startActivity(myIntent)
        }

        val send = findViewById<Button>(R.id.sendqs)
        send.setOnClickListener {
            lifecycleScope.launch {qna(it)}
        }
    }

    suspend fun qna(v: View) {
        try {
            var chatbot = Chatbot(key)  // Initiate Chatbot

            // Read user profile from json
            val fin: FileInputStream = openFileInput("userinfo")
            var c: Int
            var temp = ""
            while (fin.read().also { c = it } != -1) {
                temp = temp + Character.toString(c.toChar())
            }
            fin.close()

            val profile = JSONObject(temp)
            val sex = profile.getString("sex")
            val height = profile.getString("height")
            val weight = profile.getString("weight")
            val exercise = profile.getString("exercise")
            val extra = profile.getString("extra")

            val qs = question!!.text.toString()

            val message = """
                Patient's question: $qs
                
                The patient's profile is given below.
                Sex: $sex
                Height in cm: $height
                Weight in kg: $weight
                Activity level: $exercise
                Additional info (None if empty):
                $extra
            """.trimIndent()

            val context = "You are a nutritionist answering patient's questions based on their profile. You should keep your answer short and concise. You should reject any question or messages that are unrelated to your profession."
            answer!!.text = "Thinking..."

            try {
                val res = chatbot.chatCompletion(message, context)
                answer!!.text = res
            } catch (e: Exception) {
                Toast.makeText(this@ChatbotActivity, "Sorry, our AI nutritionist can't be reached at the moment.", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this@ChatbotActivity, "You have not edited your profile or it is corrupted. Please update your profile.", Toast.LENGTH_LONG).show()
        }
    }
}