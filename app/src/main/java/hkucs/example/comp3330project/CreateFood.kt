package hkucs.example.comp3330project

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Double.parseDouble
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class CreateFood : AppCompatActivity() {
    var question: EditText? = null
    var answer: String? = null
    var thinking: TextView? = null
    var calories: EditText? = null
    var protein: EditText? = null
    var carbs: EditText? = null
    var fat: EditText? = null
    var sugar: EditText? = null
    var vitaminC: EditText? = null
    var volume: EditText? = null
    var caloriesVal: Double? = null
    var proteinVal: Double? = null
    var carbsVal: Double? = null
    var fatVal: Double? = null
    var sugarVal: Double? = null
    var vitaminCVal: Double? = null
    var volumeVal: Double? = null
    private val DB_URL =
        "jdbc:mysql://comp3330.clwvhleuhg.ap-southeast-2.rds.amazonaws.com:3306/fooddb"
    private val USER = "admin"
    private val PASS = "comp33302023hku"

    private var listView: ListView? = null
    private var arrayAdapter: ArrayAdapter<String>?=null
    private var foodName = listOf<String>()
    private val foodArrayList = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_food)
        getSupportActionBar()!!.setTitle("Create A New Food")
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        val getOnlineFood = findViewById<Button>(R.id.getOnlineFood)
        val addFood = findViewById<Button>(R.id.addFood)

        question = findViewById<EditText?>(R.id.foodName)
        thinking = findViewById<TextView>(R.id.thinking)
        calories = findViewById<EditText?>(R.id.caloriesVal)
        protein = findViewById<EditText?>(R.id.proteinVal)
        carbs = findViewById<EditText?>(R.id.carbsVal)
        fat = findViewById<EditText?>(R.id.fatVal)
        sugar = findViewById<EditText?>(R.id.sugarVal)
        vitaminC = findViewById<EditText?>(R.id.vitaminCVal)
        volume = findViewById<EditText?>(R.id.volumeVal)


        val sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val mySet = sharedPreferences?.getStringSet("food", emptySet())
        if (mySet!!.size == 0) {
            getUpdate()
            val sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
            val mySet = sharedPreferences?.getStringSet("food", emptySet())
            foodArrayList.addAll(mySet!!)
        }
        if (mySet!!.size != 0) {
            foodArrayList.addAll(mySet!!)
        }

        val givenFoodName = intent.getStringExtra("classifier_food_name")
        if (givenFoodName != null && givenFoodName != "") {
            question!!.setText(givenFoodName)
        }
        val givenBarCode = intent.getStringExtra("barcode_food_name")
        if (givenBarCode != null && givenBarCode != "") {
            val dictionary = stringToDictionary(givenBarCode)
            GetInfoDict(dictionary)


        }


        getOnlineFood!!.setOnClickListener{
            var flag=true
            val resultList = foodArrayList.map{it.split(",")}.toList()
            for(i in resultList) {
                if(i[0].toString().lowercase()==question!!.text.toString().lowercase()) {
                    flag = false
                }
            }
            if(flag) {
                lifecycleScope.launch { GetFood(it) }
            }else{
                Toast.makeText(this@CreateFood, "Sorry, the food is already in the database.", Toast.LENGTH_LONG).show()
            }
        }

        addFood!!.setOnClickListener{
            var flag=true
            val resultList = foodArrayList.map{it.split(",")}.toList()
            for(i in resultList) {
                if(i[0].toString().lowercase()==question!!.text.toString().lowercase()) {
                    flag = false
                }
            }
            if(flag==false) {
                Toast.makeText(this@CreateFood, "Sorry, the food is already in the database.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if(checkNumer(calories?.text.toString())) {
                caloriesVal = parseDouble(calories?.text.toString())
            }
            else{
                Toast.makeText(this@CreateFood, "Sorry, the value of calroies should be a number.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(checkNumer(protein?.text.toString())) {
                proteinVal = parseDouble(protein?.text.toString())
            }
            else{
                Toast.makeText(this@CreateFood, "Sorry, the value of protein should be a number.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(checkNumer(carbs?.text.toString())) {
                carbsVal = parseDouble(carbs?.text.toString())
            }
            else{
                Toast.makeText(this@CreateFood, "Sorry, the value of carbs should be a number.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(checkNumer(fat?.text.toString())) {
                fatVal = parseDouble(fat?.text.toString())
            }
            else{
                Toast.makeText(this@CreateFood, "Sorry, the value of fat should be a number.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(checkNumer(sugar?.text.toString())) {
                sugarVal = parseDouble(sugar?.text.toString())
            }
            else{
                Toast.makeText(this@CreateFood, "Sorry, the value of sugar should be a number.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(checkNumer(vitaminC?.text.toString())) {
                vitaminCVal = parseDouble(vitaminC?.text.toString())
            }
            else{
                Toast.makeText(this@CreateFood, "Sorry, the value of vitamin C should be a number.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if(checkNumer(volume?.text.toString())) {
                volumeVal = parseDouble(volume?.text.toString())
            }
            else{
                Toast.makeText(this@CreateFood, "Sorry, the value of volume should be a number.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            insertFood(question!!.text.toString(),
                caloriesVal!!, proteinVal!!, carbsVal!!, fatVal!!, sugarVal!!,
                vitaminCVal!!, volumeVal!!
            )


        }
    }
    fun stringToDictionary(inputString: String): Map<String, String> {
        val keyValuePairs = inputString
            .trim()
            .removeSurrounding("{", "}")
            .split(", ")
            .map { it.trim().split("=") }
            .map { it[0].trim() to it[1].trim() }
            .toMap()
        return keyValuePairs
    }

    fun GetInfoDict(FoodDict: Map<String, String>){
        if (FoodDict.containsKey("name")){
            val name = "PackagedFood:" + FoodDict["name"].toString()
            question?.setText(name)
        }

        if (FoodDict.containsKey("calories")){
            calories?.setText(FoodDict["calories"].toString())
        }
        if (FoodDict.containsKey("protein")){
            protein?.setText(FoodDict["protein"].toString())
        }
        if (FoodDict.containsKey("carbohydrate")){
            carbs?.setText(FoodDict["carbohydrate"].toString())
        }
        if (FoodDict.containsKey("fat")){
            fat?.setText(FoodDict["fat"].toString())
        }
        if (FoodDict.containsKey("sugars")){
            sugar?.setText(FoodDict["sugars"].toString())
        }
        if (FoodDict.containsKey("vitamin C")){
            vitaminC?.setText(FoodDict["vitamin C"].toString())
        }
        if (FoodDict.containsKey("size")){
            volume?.setText(FoodDict["size"].toString())
        }
    }
    suspend fun GetFood(v:View) {
        try {
            thinking!!.text ="Searching food from Internet"
            var chatbot = Chatbot("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")
            val q = question!!.text.toString()
            try {
                val res = chatbot.chatCompletion(
                    q,
                    "You are a database answering user's query. You will be given a name of food, you need to return the value of column: {calories, protein, carbs, fat, sugar, vitaminC, volume} The value should be based on the volume of 100 g. You should answer in a format of json {calories, protein, carbs, fat, sugar, vitaminC, volume} only without fullstop in the end of the answer. If you cannot find the vale of the column, return 0 for the value. You should return 0 to any question or messages that are unrelated to your profession.")

                answer = res
                if (isJSONValid(answer)) {

                    val mapper = ObjectMapper()
                    mapper.readTree(answer)
                    val map: Map<String?, Any?>? =
                        mapper.readValue(answer, object : TypeReference<Map<String?, Any?>?>() {})

                    if(checkNumer(map?.get("calories").toString())) {
                        calories?.setText(map?.get("calories").toString())
                        caloriesVal = parseDouble(map?.get("calories").toString())
                    }
                    else{
                        calories?.setText("0")
                        caloriesVal =  0 as Double
                    }
                    if(checkNumer(map?.get("protein").toString())) {
                        protein?.setText(map?.get("protein").toString())
                        proteinVal = parseDouble(map?.get("protein").toString())
                    }
                    else{
                        protein?.setText("0")
                        proteinVal =  0 as Double
                    }
                    if(checkNumer(map?.get("carbs").toString())) {
                        carbs?.setText(map?.get("carbs").toString())
                        carbsVal = parseDouble(map?.get("carbs").toString())
                    }
                    else{
                        carbs?.setText("0")
                        carbsVal = 0 as Double
                    }
                    if(checkNumer(map?.get("fat").toString())) {
                        fat?.setText(map?.get("fat").toString())
                        fatVal = parseDouble(map?.get("fat").toString())
                    }
                    else{
                        fat?.setText("0")
                        fatVal = 0 as Double
                    }
                    if(checkNumer(map?.get("sugar").toString())) {
                        sugar?.setText(map?.get("sugar").toString())
                        sugarVal = parseDouble(map?.get("sugar").toString())
                    }
                    else{
                        sugar?.setText("0")
                        sugarVal = 0 as Double
                    }
                    if(checkNumer(map?.get("vitaminC").toString())) {
                        vitaminC?.setText(map?.get("vitaminC").toString())
                        vitaminCVal = parseDouble(map?.get("vitaminC").toString())
                    }
                    else{
                        vitaminC?.setText("0")
                        vitaminCVal = 0 as Double
                    }
                    if(checkNumer(map?.get("volume").toString())) {
                        volume?.setText(map?.get("volume").toString())
                        volumeVal = parseDouble(map?.get("volume").toString())
                    }
                    else{
                        volume?.setText("0")
                        volumeVal = 0 as Double
                    }
                    thinking!!.text ="Done"
                } else {
                    thinking!!.text ="Cannot find from Internet"
                }

            } catch (e: Exception) {
                Toast.makeText(this@CreateFood, "Sorry, cannot find food from online", Toast.LENGTH_LONG).show()
            }
        }catch (e: IOException) {

        }
    }

    fun checkNumer(s:String):Boolean{
        var numeric = true
        try {
            val num = parseDouble(s)
        } catch (e: NumberFormatException) {
            numeric = false
        }
        return numeric
    }

    fun isJSONValid(jsonInString: String?): Boolean {
        return try {

            val mapper = ObjectMapper()
            mapper.readTree(jsonInString)
            true
        } catch (e: IOException) {
            false
        }
    }
    fun getUpdate(){
        Thread {
            try {

                Class.forName("com.mysql.jdbc.Driver")
                val connection: Connection = DriverManager.getConnection(DB_URL, USER, PASS)
                val statement: Statement = connection.createStatement()
                val rs: ResultSet = statement.executeQuery("SELECT * FROM foods;")
                val myArrayList = arrayListOf<String>()
                while (rs.next()) {
                    var msgg = rs.getString("name").toString() + "," + rs.getDouble("calories").toString() +"," + rs.getDouble("protein").toString() +"," + rs.getDouble("carbs").toString() +"," + rs.getDouble("fat").toString() +"," + rs.getDouble("sugar").toString() +"," + rs.getDouble("vitaminC").toString() +"," + rs.getDouble("volume").toString()
                    myArrayList.add(msgg)

                }
                rs.close()
                statement.close()
                connection.close()
                runOnUiThread {
                    val mySet = myArrayList.toSet()
                    val sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences?.edit()
                    if (editor != null) {
                        editor.putStringSet("food", mySet)
                    }
                    if (editor != null) {
                        editor.apply()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
    fun insertFood( foodname: String?, calories: Double, protein: Double, carbs: Double,
                    fat: Double, sugar: Double, vitaminC: Double, volume: Double) {
        Thread {
            try {
                Class.forName("com.mysql.jdbc.Driver")
                val connection: Connection = DriverManager.getConnection(DB_URL, USER, PASS)
                val statement: Statement = connection.createStatement()
                statement.execute("INSERT INTO foods (name, calories,protein,carbs,fat,sugar,vitaminC,volume) VALUES('"+foodname+"','"+calories+"','"+protein+"','"+carbs+"','"+fat+"','"+sugar+"','"+vitaminC+"','"+volume+"');")
                statement.close()
                connection.close()
                runOnUiThread {
                    getUpdate()
                    val sharedPreferences = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                    val mySet = sharedPreferences?.getStringSet("food", emptySet())
                    foodArrayList.addAll(mySet!!)
                    Toast.makeText(this@CreateFood, "Successfully added food to database.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
