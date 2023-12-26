package hkucs.example.comp3330project

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.FileInputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatbotFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatbotFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private val key: String = "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"

    // Initiate fields
    private var icon: ImageView? = null
    private var question: EditText? = null
    private var answer: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_chatbot, container, false)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        question = view.findViewById<EditText>(R.id.question)
        answer = view.findViewById<TextView>(R.id.answer)
        icon = view.findViewById<ImageView>(R.id.iconAI)


        val editProfile = view.findViewById<Button>(R.id.editInfo)
        editProfile.setOnClickListener {
            val myIntent = Intent(requireContext(), FormActivity::class.java)
            startActivity(myIntent)
        }

        val send = view.findViewById<Button>(R.id.sendqs)
        send.setOnClickListener {
            lifecycleScope.launch {qna(it)}
        }
        return view
    }

    suspend fun qna(v: View) {
        var chatbot = Chatbot(key)  // Initiate Chatbot
        var message = ""
        try {
            // Read user profile from json
            val fin: FileInputStream = requireContext().openFileInput("userinfo")
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
            message = """
                Patient's question: $qs
                
                The patient's profile is given below.
                Sex: $sex
                Height in cm: $height
                Weight in kg: $weight
                Activity level: $exercise
                Additional info (None if empty):
                $extra
            """.trimIndent()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "You have not edited your profile or it is corrupted. Please update your profile.", Toast.LENGTH_LONG).show()
            return
        }

        val context = "You are a nutritionist answering patient's questions based on their profile. You should keep your answer short and concise. You should reject any question or messages that are unrelated to your profession."
        answer!!.text = "Thinking..."

        try {
            val res = chatbot.chatCompletion(message, context)
            answer!!.text = res
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Sorry, our AI nutritionist can't be reached at the moment.", Toast.LENGTH_LONG).show()
        }
    }
}