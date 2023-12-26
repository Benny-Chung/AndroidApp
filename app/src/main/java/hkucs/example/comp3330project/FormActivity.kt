package hkucs.example.comp3330project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.FileInputStream
import java.io.FileOutputStream


class FormActivity : AppCompatActivity() {
    // Initiate fields
    private var sexGroup: RadioGroup? = null
    private var sex: RadioButton? = null
    private var exerciseGroup: RadioGroup? = null
    private var exercise: RadioButton? = null
    private var height: EditText? = null
    private var weight: EditText? = null
    private var extraInfo: EditText? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        // Enable back button
        getSupportActionBar()!!.setTitle("Edit User Profile")
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)

        // Initiate fields
        height = findViewById<EditText>(R.id.height)
        weight = findViewById<EditText>(R.id.weight)
        extraInfo = findViewById<EditText>(R.id.extraInfo)

        sexGroup = findViewById<RadioGroup>(R.id.sex)
        exerciseGroup = findViewById<RadioGroup>(R.id.exercise)

        try {
            // Read json profile
            val fin: FileInputStream = openFileInput("userinfo")
            var c: Int
            var temp = ""
            while (fin.read().also { c = it } != -1) {
                temp = temp + Character.toString(c.toChar())
            }
            fin.close()

            // set fields according to json if present

            val profile = JSONObject(temp)
            sexGroup!!.check(profile.getInt("sexId"))
            height!!.setText(profile.getString("height"))
            weight!!.setText(profile.getString("weight"))
            exerciseGroup!!.check(profile.getInt("exerciseId"))
            extraInfo!!.setText(profile.getString("extra"))
        } catch (e: Exception) {}
    }

    fun save(v: View) {
        try {
            val h = height!!.text.toString()
            val w = weight!!.text.toString()
            val i = extraInfo!!.text.toString()

            val sexId = sexGroup!!.checkedRadioButtonId
            sex = findViewById<RadioButton>(sexId)
            val s = sex!!.text.toString()

            val exerciseId = exerciseGroup!!.checkedRadioButtonId
            exercise = findViewById<RadioButton>(exerciseId)
            val e = exercise!!.text.toString()

            // Save user profile as json
            val jsonObject = JSONObject()
            jsonObject.put("sex", s)
            jsonObject.put("height", h)
            jsonObject.put("weight", w)
            jsonObject.put("exercise", e)
            jsonObject.put("extra", i)
            jsonObject.put("sexId", sexId)
            jsonObject.put("exerciseId", exerciseId)

            try {
                var fos: FileOutputStream = openFileOutput("userinfo", MODE_PRIVATE)
                fos.write(jsonObject.toString().toByteArray())
                fos.close()
                Toast.makeText(this@FormActivity, "User profile updated.", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("to","ChatbotFragment")
                startActivity(intent)   // Back to chatbot
            } catch (e: Exception){
                Toast.makeText(this@FormActivity, "An error has occured. Please try again.", Toast.LENGTH_LONG).show()
            }


        } catch (e: NullPointerException) {
            Toast.makeText(this@FormActivity, "An error has occured. Please check if you have filled all required fields.", Toast.LENGTH_LONG).show()
        }

    }
}