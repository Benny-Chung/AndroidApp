package hkucs.example.comp3330project


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var favlistView: ListView? = null
    private var favarrayAdapter: ArrayAdapter<String>?=null
    private var favfoodName = arrayListOf<String>()
    private var SearchBar_button: Button? = null
    private var save: Button? = null
    private var share: Button? = null
    private var get: Button? = null
    private var clean: Button? = null
    private val DB_URL =
        "jdbc:mysql://comp3330.clwvhleuhg.ap-southeast-2.rds.amazonaws.com:3306/fooddb"
    private val USER = "admin"
    private val PASS = "comp33302023hku"
    private var msg :String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_list, container, false)
        requireActivity().title = "Favourite Food List"
        get= view.findViewById(R.id.get)


        var selectedDate = requireActivity().intent.getStringExtra("date")

        favlistView = view.findViewById(R.id.fav_record)

        SearchBar_button = view.findViewById(R.id.SearchBar_button)
        SearchBar_button!!.setOnClickListener {
            val intent = Intent(requireContext(),SearchBar2::class.java)
            startActivity(intent)
        }
        share = view.findViewById(R.id.export)
        clean = view.findViewById(R.id.clean)


        clean!!.setOnClickListener{
            AlertDialog.Builder(requireContext()).setTitle("Delete all food?").setNeutralButton("No",null).setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                favfoodName = arrayListOf<String>()
                saveArrayListToSharedPreferences("fav", favfoodName)
                favarrayAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,favfoodName)
                favlistView?.adapter = favarrayAdapter
            }.show()
            true
        }
        share!!.setOnClickListener{
            val editTextField = EditText(requireActivity())
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Share")
                .setMessage("Share with this ID:")
                .setView(editTextField)
                .setPositiveButton("OK") { _, _ ->
                    val editTextInput = editTextField.text.toString()
                    updateRecordList(editTextInput)
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }
        get!!.setOnClickListener{
            val editTextField = EditText(requireActivity())
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Share")
                .setMessage("Import list with ID:")
                .setView(editTextField)
                .setPositiveButton("OK") { _, _ ->
                    val editTextInput = editTextField.text.toString()
                    importRecordList(editTextInput)
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }


        val sharedPreferences = context?.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val mySet = sharedPreferences?.getStringSet("food", emptySet())
        if (mySet!!.size == 0) {
            getUpdate()
        }

        favfoodName= getArrayListFromSharedPreferences("fav")
        favarrayAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,favfoodName)
        favlistView?.adapter = favarrayAdapter
        favlistView?.setOnItemClickListener { parent, view, position, id ->
            val foodClicked = parent?.getItemAtPosition(position).toString()
            ///////////////////////////////////////////////
            val sharedPreferences = context?.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
            val mySet = sharedPreferences?.getStringSet("food", emptySet())
            if (mySet!!.size != 0) {
                val myArrayList = arrayListOf<String>()
                myArrayList.addAll(mySet!!)
                val resultList = myArrayList.map{it.split(",")}.toList()
                for(i in resultList) {
                    if(i.contains(foodClicked)) {
                        msg = """
                    calories:${i.get(1)}
                    protein:${i.get(2)}
                    carbs:${i.get(3)}
                    fat:${i.get(4)}
                    sugar:${i.get(5)}
                    viamin C:${i.get(6)}
                    Volume:${i.get(7)}g
                    """.trimIndent()
                    }
                }
            }
            ///////////////////////////////////////////////////////
            var drawableID = "food"
            if(requireActivity().getResources().getIdentifier(foodClicked.lowercase(), "drawable", requireActivity().getPackageName())!=0){
                drawableID = foodClicked.lowercase()
            }
            AlertDialog.Builder(requireContext()).setIcon(requireActivity().getResources().getIdentifier(drawableID, "drawable", requireActivity().getPackageName())).setMessage(msg).setTitle(foodClicked).setPositiveButton("OK", null).show()
        }
        favlistView?.setOnItemLongClickListener { parent, view, position, id ->
            AlertDialog.Builder(requireContext()).setTitle("Delete or Add food?").setPositiveButton("Add") { _: DialogInterface, _: Int ->
                val foodClicked = parent?.getItemAtPosition(position).toString()
                var foodList: List<String> = ArrayList()
                val databaseAccess: DataBaseAccess = DataBaseAccess.getInstance(requireContext().applicationContext)
                databaseAccess.open()
                foodList = databaseAccess.getFoodList((activity as MainActivity).date)
                (foodList as MutableList<String>?)?.add(foodClicked)
                databaseAccess.addFood_to_dateRecord((activity as MainActivity).date,foodList)
                databaseAccess.close()
            }.setNegativeButton("Delete") { _: DialogInterface, _: Int ->
                val foodClicked = parent?.getItemAtPosition(position).toString()
                favfoodName.remove(foodClicked)
                saveArrayListToSharedPreferences("fav", favfoodName)
                favarrayAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,favfoodName)
                favlistView?.adapter = favarrayAdapter
            }.setNeutralButton("Cancel",null).show()
            true
        }


        return view
    }
    private fun saveArrayListToSharedPreferences(key: String, arrayList: ArrayList<String>) {
        // Convert the ArrayList to a Set of strings
        val mySet = arrayList.toSet()

        // Get an instance of the SharedPreferences
        val sharedPreferences = context?.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

        // Get an instance of the SharedPreferences.Editor
        val editor = sharedPreferences?.edit()

        // Put the Set of strings into the SharedPreferences.Editor
        if (editor != null) {
            editor.putStringSet(key, mySet)
        }

        // Apply the changes to the SharedPreferences
        if (editor != null) {
            editor.apply()
        }
    }
    private fun getArrayListFromSharedPreferences(key: String): ArrayList<String> {
        val sharedPreferences = context?.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val mySet = sharedPreferences?.getStringSet(key, emptySet())
        val myArrayList = arrayListOf<String>()
        myArrayList.addAll(mySet!!)
        return myArrayList
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    fun updateRecordList(id:String) {
        Thread {
            try {
                Log.d("DEBUGTEXT","123")
                Class.forName("com.mysql.jdbc.Driver")
                val connection: Connection = DriverManager.getConnection(DB_URL, USER, PASS)
                val statement: Statement = connection.createStatement()
                statement.execute("DROP TABLE IF EXISTS "+id+";")
                statement.execute("CREATE TABLE IF NOT EXISTS " + id + " (name VARCHAR(30), no INT, PRIMARY KEY (name));")
                for(item in favfoodName) {
                    statement.execute("INSERT INTO " + id + " (name, no) VALUES('" + item + "', 1);")
                }
                statement.close()
                connection.close()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext() ,"Shared Successfully.", Toast.LENGTH_LONG).show()
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
                    var msgg = rs.getString("name").toString() + "," +"," + rs.getDouble("calories").toString() +"," + rs.getDouble("protein").toString() +"," + rs.getDouble("carbs").toString() +"," + rs.getDouble("fat").toString() +"," + rs.getDouble("sugar").toString() +"," + rs.getDouble("vitaminC").toString() +"," + rs.getDouble("volume").toString()
                    myArrayList.add(msgg)
                }
                rs.close()
                statement.close()
                connection.close()
                activity?.runOnUiThread {
                    val mySet = myArrayList.toSet()
                    val sharedPreferences = context?.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
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
    fun importRecordList(id:String) {
        Thread {
            try {

                Class.forName("com.mysql.jdbc.Driver")
                val connection: Connection = DriverManager.getConnection(DB_URL, USER, PASS)
                val statement: Statement = connection.createStatement()
                val rs: ResultSet = statement.executeQuery("SELECT * FROM "+id+";")
                val myArrayList = arrayListOf<String>()
                    while (rs.next()) {
                        val n = rs.getInt("no")
                        for (i in 1..n) {
                            var msgg = rs.getString("name").toString()
                            myArrayList.add(msgg)
                        }
                }
                statement.close()
                connection.close()
                activity?.runOnUiThread {
                    if(myArrayList!=null) {
                        Toast.makeText(
                            requireContext(),
                            "Imported Successfully.",
                            Toast.LENGTH_LONG
                        ).show()
                        val mySet = myArrayList.toSet()
                        val sharedPreferences =
                            context?.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                        val editor = sharedPreferences?.edit()
                        if (editor != null) {
                            editor.putStringSet("fav", mySet)
                        }
                        if (editor != null) {
                            editor.apply()
                        }

                        favfoodName = getArrayListFromSharedPreferences("fav")
                        favarrayAdapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            favfoodName
                        )
                        favlistView?.adapter = favarrayAdapter
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
