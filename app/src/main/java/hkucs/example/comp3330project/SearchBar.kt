package hkucs.example.comp3330project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement


class SearchBar : AppCompatActivity() {
    private var listView: ListView? = null
    private var foodName = listOf<String>()
    private var foodList: List<String> = ArrayList()
    private var arrayAdapter:ArrayAdapter<String>?=null
    private val DB_URL =
        "jdbc:mysql://comp3330.clwvhleuhg.ap-southeast-2.rds.amazonaws.com:3306/fooddb"
    private val USER = "admin"
    private val PASS = "comp33302023hku"
    private var loading = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_search_bar)
        getSupportActionBar()!!.setTitle("Search Food From Database")
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)

        try {
            getUpdate()
            getAllFood()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        listView = findViewById(R.id.listView)

    }
   override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            searchView.clearFocus()
            //Log.d("DEBUGTEXT", "Search query was:")
            return false
        }
        override fun onQueryTextChange(newText: String?): Boolean {
            arrayAdapter?.filter?.filter(newText)
            return false
        }
        })
        listView?.setOnItemClickListener { parent, view, position, id ->
            if (!loading) {
                val date=intent.getStringExtra("date")
                val databaseAccess: DataBaseAccess = DataBaseAccess.getInstance(this)
                databaseAccess.open()
                foodList = databaseAccess.getFoodList(date)

                (foodList as MutableList<String>?)?.add(foodName[position].toString())
                databaseAccess.addFood_to_dateRecord(date,foodList)
                databaseAccess.close()
                val intent = Intent(this@SearchBar, MainActivity::class.java)
                intent.putExtra("to","SummaryFragment")
                intent.putExtra("date",date.toString())
                startActivity(intent)
            }
        }
        return true
    }
    fun getAllFood() {
        Thread {
            try {
                val AllName: MutableList<String> = java.util.ArrayList()
                Class.forName("com.mysql.jdbc.Driver")
                val connection: Connection = DriverManager.getConnection(DB_URL, USER, PASS)
                val statement: Statement = connection.createStatement()
                val rs: ResultSet = statement.executeQuery("SELECT * FROM foods")
                while (rs.next()) {
                    AllName.add(rs.getString("name"))
                }
                rs.close()
                statement.close()
                connection.close()

                runOnUiThread {
                    foodName = AllName

                    arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,foodName)
                    listView?.adapter = arrayAdapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
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

}
