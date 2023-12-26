package hkucs.example.comp3330project

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement


class test : AppCompatActivity(){
    private val DB_URL =
        "jdbc:mysql://comp3330.clwvhleuhg.ap-southeast-2.rds.amazonaws.com:3306/fooddb"
    private val USER = "admin"
    private val PASS = "comp33302023hku"
    private val TABLE_NAME: String? = null
    var FoodName: List<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        try {
            utilFun()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
    fun utilFun() {
        Thread {
            try {
                val AllName: MutableList<String> = java.util.ArrayList()
                Class.forName("com.mysql.jdbc.Driver")
                val connection: Connection = DriverManager.getConnection(DB_URL, USER, PASS)
                val statement: Statement = connection.createStatement()
                val rs: ResultSet = statement.executeQuery("SELECT * FROM foods")
                //val ans = if (rs.next()) rs.getString(1) else ""
                while (rs.next()) {
                    AllName.add(rs.getString("name"))
                }
                //val ans = rs.getArray("name").toString()
                // Process the result set here
                rs.close()
                statement.close()
                connection.close()

                runOnUiThread {
                    FoodName = AllName
                    Log.d("DEBUGTEXT", AllName.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
