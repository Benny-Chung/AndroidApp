package hkucs.example.comp3330project

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SummaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SummaryFragment : Fragment(){
    private val DB_URL =
        "jdbc:mysql://comp3330.clwvhleuhg.ap-southeast-2.rds.amazonaws.com:3306/fooddb"
    private val USER = "admin"
    private val PASS = "comp33302023hku"
    // TODO: Rename and change types of parameters
    var last7avg: MutableList<Float> = mutableListOf(0f,0f,0f,0f,0f,0f,0f)
    private var totalVal0 = listOf(0f,0f,0f,0f,0f,0f)
    private var totalVal1 = listOf(0f,0f,0f,0f,0f,0f)
    private var totalVal2 = listOf(0f,0f,0f,0f,0f,0f)
    private var totalVal3 = listOf(0f,0f,0f,0f,0f,0f)
    private var totalVal4 = listOf(0f,0f,0f,0f,0f,0f)
    private var totalVal5 = listOf(0f,0f,0f,0f,0f,0f)
    private var totalVal6 = listOf(0f,0f,0f,0f,0f,0f)
    private var totalVal7 = listOf(0f,0f,0f,0f,0f,0f)
    private var transpose7 = listOf(listOf(0f,0f,0f,0f,0f,0f))

    private lateinit var tdyCal: TextView
    private lateinit var tdyCarbs: TextView
    private lateinit var tdyVit: TextView
    private lateinit var tdyFat: TextView
    private lateinit var tdySug: TextView
    private lateinit var tdyPro: TextView
    private lateinit var lastCal: TextView
    private lateinit var lastCarbs: TextView
    private lateinit var lastVit: TextView
    private lateinit var lastFat: TextView
    private lateinit var lastSug: TextView
    private lateinit var lastPro: TextView

    private lateinit var addFood_button: FloatingActionButton

    private lateinit var radar: RadarChart
    private lateinit var line1: LineChart
    private lateinit var line2: LineChart
    private lateinit var line3: LineChart
    private lateinit var line4: LineChart
    private lateinit var line5: LineChart
    private lateinit var line6: LineChart

    private lateinit var grid11: LinearLayout
    private lateinit var grid12: LinearLayout
    private lateinit var grid21: LinearLayout
    private lateinit var grid22: LinearLayout
    private lateinit var grid31: LinearLayout
    private lateinit var grid32: LinearLayout

    private var selectedDate: String? = null


    private var reloadButton: Button? = null

    private var openCalendar:Button?= null


    //food record
    private var listView: ListView? = null
    private var arrayAdapter:ArrayAdapter<String>?=null
    private var foodName = listOf<String>()

    private var msg :String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_summary, container, false)

        addFood_button = view.findViewById(R.id.addFood)
        addFood_button.setOnClickListener {view->showBottomDialog()}

        openCalendar = view.findViewById(R.id.OpenCalendar)


        //when calendar is click, go to calendar intent
        openCalendar!!.setOnClickListener{
            val intent = Intent(requireContext(),RecordCalendar::class.java)
            startActivity(intent)}

        selectedDate = requireActivity().intent.getStringExtra("date")
        listView = view.findViewById(R.id.food_record)
        //select the record of food of the selected date
        if(selectedDate==null) {
            val date = LocalDate.now().format(DateTimeFormatter.ofPattern("d_M_yyyy"))
            requireActivity().title = date
            updateRecord(date)
            SummaryUpdate()
            selectedDate = date
            (activity as MainActivity).date = selectedDate
        }
        else{
            val date = selectedDate
            requireActivity().title = selectedDate
            (activity as MainActivity).date = selectedDate
            updateRecord(date)
            SummaryUpdate()
        }
        reloadButton = view.findViewById(R.id.Reload)
        reloadButton!!.setOnClickListener {
            reloadButton!!.setEnabled(false)
            updateRecord((activity as MainActivity).date)
            SummaryUpdate()
            val handler = Handler()
            handler.postDelayed(Runnable { reloadButton!!.setEnabled(true) }, 2000)
        }
        //get name of food return from previous intent, if it is no null, refresh the ListView
        val food_item = requireActivity().intent.getStringExtra("food")
        if(food_item!=null) {
            updateRecord((activity as MainActivity).date)
            SummaryUpdate()
        }


        val sharedPreferences = context?.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val mySet = sharedPreferences?.getStringSet("food", emptySet())
        if (mySet!!.size == 0) {
            getUpdate()
        }

        //list item is clicked
        listView?.setOnItemClickListener { parent, view, position, id ->
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
            var drawableID = "food"
            if(requireActivity().getResources().getIdentifier(foodClicked.lowercase(), "drawable", requireActivity().getPackageName())!=0){
                drawableID = foodClicked.lowercase()
            }
            AlertDialog.Builder(requireContext()).setIcon(requireActivity().getResources().getIdentifier(drawableID, "drawable", requireActivity().getPackageName())).setMessage(msg).setTitle(foodClicked).setPositiveButton("OK", null).show()
        }
        //list item is long clicked
        listView?.setOnItemLongClickListener { parent, view, position, id ->
            AlertDialog.Builder(requireContext()).setTitle("Delete this food?").setNeutralButton("No",null).setPositiveButton("Yes") { _: DialogInterface, _: Int ->

                var foodList: List<String> = ArrayList()
                val databaseAccess: DataBaseAccess = DataBaseAccess.getInstance(requireContext().applicationContext)
                databaseAccess.open()
                foodList = databaseAccess.getFoodList( (activity as MainActivity).date)
                (foodList as MutableList<String>?)?.remove(foodName[position].toString())
                databaseAccess.addFood_to_dateRecord( (activity as MainActivity).date,foodList)
                updateRecord((activity as MainActivity).date)
                databaseAccess.close()
                SummaryUpdate()

            }.show()
            true
        }
        radar = view.findViewById(R.id.radar)
        radar.yAxis.axisMinimum = 0f
        radar.yAxis.axisMaximum = 1f
        radar.yAxis.isEnabled = false
        radar.description.isEnabled = false
        radar.xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("Calories", "Protein", "Carbs", "Fat", "Sugar", "Vitamin C"))

        tdyCal = view.findViewById(R.id.tdyCal)
        tdyPro = view.findViewById(R.id.tdyPro)
        tdyCarbs = view.findViewById(R.id.tdyCarbs)
        tdySug = view.findViewById(R.id.tdySug)
        tdyFat = view.findViewById(R.id.tdyFat)
        tdyVit = view.findViewById(R.id.tdyVit)

        lastCal = view.findViewById(R.id.lastCal)
        lastPro = view.findViewById(R.id.lastPro)
        lastCarbs = view.findViewById(R.id.lastCarbs)
        lastSug = view.findViewById(R.id.lastSug)
        lastFat = view.findViewById(R.id.lastFat)
        lastVit = view.findViewById(R.id.lastVit)



        grid11 = view.findViewById(R.id.grid11)
        line1 = view.findViewById(R.id.line1)
        line1.xAxis.isEnabled = false
        line1.legend.isEnabled = false
        line1.description.isEnabled = false
        grid11.setOnClickListener {
            if (line1.visibility == View.VISIBLE) {
                line1.visibility = View.GONE
            } else {
                line1.data = setDataLine(transpose7[0])
                line1.visibility = View.VISIBLE
                line1.invalidate()
            }
        }

        grid12 = view.findViewById(R.id.grid12)
        line2 = view.findViewById(R.id.line2)
        line2.xAxis.isEnabled = false
        line2.legend.isEnabled = false
        line2.description.isEnabled = false
        grid12.setOnClickListener {
            if (line2.visibility == View.VISIBLE) {
                line2.visibility = View.GONE
            } else {
                line2.data = setDataLine(transpose7[1])
                line2.visibility = View.VISIBLE
                line2.invalidate()
            }
        }

        grid21 = view.findViewById(R.id.grid21)
        line3 = view.findViewById(R.id.line3)
        line3.xAxis.isEnabled = false
        line3.legend.isEnabled = false
        line3.description.isEnabled = false
        grid21.setOnClickListener {
            if (line3.visibility == View.VISIBLE) {
                line3.visibility = View.GONE
            } else {
                line3.data = setDataLine(transpose7[2])
                line3.visibility = View.VISIBLE
                line3.invalidate()
            }
        }

        grid22 = view.findViewById(R.id.grid22)
        line4 = view.findViewById(R.id.line4)
        line4.xAxis.isEnabled = false
        line4.legend.isEnabled = false
        line4.description.isEnabled = false
        grid22.setOnClickListener {
            if (line4.visibility == View.VISIBLE) {
                line4.visibility = View.GONE
            } else {
                line4.data = setDataLine(transpose7[3])
                line4.visibility = View.VISIBLE
                line4.invalidate()
            }
        }

        grid31 = view.findViewById(R.id.grid31)
        line5 = view.findViewById(R.id.line5)
        line5.xAxis.isEnabled = false
        line5.legend.isEnabled = false
        line5.description.isEnabled = false
        grid31.setOnClickListener {
            if (line5.visibility == View.VISIBLE) {
                line5.visibility = View.GONE
            } else {
                line5.data = setDataLine(transpose7[4])
                line5.visibility = View.VISIBLE
                line5.invalidate()
            }
        }

        grid32 = view.findViewById(R.id.grid32)
        line6 = view.findViewById(R.id.line6)
        line6.xAxis.isEnabled = false
        line6.legend.isEnabled = false
        line6.description.isEnabled = false
        grid32.setOnClickListener {
            if (line6.visibility == View.VISIBLE) {
                line6.visibility = View.GONE
            } else {
                line6.data = setDataLine(transpose7[5])
                line6.visibility = View.VISIBLE
                line6.invalidate()
            }
        }

        requireActivity().intent.removeExtra("food")

        return view
    }




    private fun updateRecord(date:String?){
        val databaseAccess: DataBaseAccess = DataBaseAccess.getInstance(requireContext().applicationContext)
        databaseAccess.open()
        foodName = databaseAccess.getDateRecord(date)
        databaseAccess.close()
        arrayAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,foodName)
        listView?.adapter = arrayAdapter
    }

    private fun showBottomDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_layout)

        val manual_input = dialog.findViewById<LinearLayout>(R.id.manual_input)
        val new_food = dialog.findViewById<LinearLayout>(R.id.new_food)
        val barcode_input = dialog.findViewById<LinearLayout>(R.id.barcode_input)
        val photo_input = dialog.findViewById<LinearLayout>(R.id.photo_input)

        manual_input.setOnClickListener {
            dialog.dismiss()
            //Search food from database when click, go to search intent
            val intent = Intent(requireContext(),SearchBar::class.java)
            intent.putExtra("date", selectedDate)
            startActivity(intent)
        }

        new_food.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(requireContext(),CreateFood::class.java)
            startActivity(intent)
        }

        barcode_input.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(requireContext(),BarcodeActivity::class.java)
            startActivity(intent)
        }

        photo_input.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(requireContext(),PhotoActivity::class.java)
            intent.putExtra("date", selectedDate)
            startActivity(intent)
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    fun SummaryUpdate() {
        Thread {
            totalVal0 = getDailyVal(selectedDate)
            activity?.runOnUiThread {
                tdyCal.setText(String.format("%.1f", totalVal0[0]))
                tdyPro.setText(String.format("%.1f", totalVal0[1]))
                tdyCarbs.setText(String.format("%.1f", totalVal0[2]))
                tdySug.setText(String.format("%.1f", totalVal0[3]))
                tdyFat.setText(String.format("%.1f", totalVal0[4]))
                tdyVit.setText(String.format("%.1f", totalVal0[5]))
            }

            var lists7: MutableList<List<Float>> = mutableListOf(listOf(0f,0f,0f,0f,0f,0f))
            try {
                totalVal1 = getDailyVal(getDateNDaysAgo(selectedDate, 1))
            } catch (e: Exception) {}
            try {
                totalVal2 = getDailyVal(getDateNDaysAgo(selectedDate, 2))
            } catch (e: Exception) {}
            try {
                totalVal3 = getDailyVal(getDateNDaysAgo(selectedDate, 3))
            } catch (e: Exception) {}
            try {
                totalVal4 = getDailyVal(getDateNDaysAgo(selectedDate, 4))
            } catch (e: Exception) {}
            try {
                totalVal5 = getDailyVal(getDateNDaysAgo(selectedDate, 5))
            } catch (e: Exception) {}
            try {
                totalVal6 = getDailyVal(getDateNDaysAgo(selectedDate, 6))
            } catch (e: Exception) {}
            try {
                totalVal7 = getDailyVal(getDateNDaysAgo(selectedDate, 7))
            } catch (e: Exception) {}

            lists7[0]=(totalVal7)
            lists7.add(totalVal6)
            lists7.add(totalVal5)
            lists7.add(totalVal4)
            lists7.add(totalVal3)
            lists7.add(totalVal2)
            lists7.add(totalVal1)

            transpose7 = transpose(lists7)
            val last7avg = calculateAverage(transpose7)

            activity?.runOnUiThread {
                lastCal.setText(String.format("Last week: %.1f ", last7avg[0]))
                lastPro.setText(String.format("Last week: %.1f ", last7avg[1]))
                lastCarbs.setText(String.format("Last week: %.1f ", last7avg[2]))
                lastSug.setText(String.format("Last week: %.1f ", last7avg[3]))
                lastFat.setText(String.format("Last week: %.1f ", last7avg[4]))
                lastVit.setText(String.format("Last week: %.1f ", last7avg[5]))

                setDataRadar(last7avg, totalVal0)
                radar.notifyDataSetChanged();
                radar.invalidate()
            }

        }.start()
    }
    fun getUpdate(){
        Thread {
            try {
                // val AllName: MutableList<String> = java.util.ArrayList()
                Class.forName("com.mysql.jdbc.Driver")
                val connection: Connection = DriverManager.getConnection(DB_URL, USER, PASS)
                val statement: Statement = connection.createStatement()
                val rs: ResultSet = statement.executeQuery("SELECT * FROM foods;")
                val myArrayList = arrayListOf<String>()
                while (rs.next()) {
                    var msgg = rs.getString("name").toString() + "," + rs.getDouble("calories").toString() +"," + rs.getDouble("protein").toString() +"," + rs.getDouble("carbs").toString() +"," + rs.getDouble("fat").toString() +"," + rs.getDouble("sugar").toString() +"," + rs.getDouble("vitaminC").toString() +"," + rs.getDouble("volume").toString()
                    myArrayList.add(msgg)
                    //var arrayList = arrayListOf<String>(msgg)
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

    fun getDailyVal(date: String?): List<Float> {
        var cal = 0f
        var pro = 0f
        var car = 0f
        var fat = 0f
        var sug = 0f
        var vit = 0f
        var foodList: List<String> = ArrayList()
        val databaseAccess: DataBaseAccess = DataBaseAccess.getInstance(requireContext().applicationContext)
        databaseAccess.open()
        foodList = databaseAccess.getFoodList(date)
        databaseAccess.addFood_to_dateRecord(date,foodList)
        databaseAccess.close()
        val sharedPreferences = context?.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val mySet = sharedPreferences?.getStringSet("food", emptySet())
        if (mySet!!.size != 0) {
            val myArrayList = arrayListOf<String>()
            myArrayList.addAll(mySet!!)
            val resultList = myArrayList.map{it.split(",")}.toList()
            for(f in foodList) {
                for (i in resultList) {
                    if (i.contains(f)) {
                        cal = cal + i.get(1).toFloat()
                        pro = pro + i.get(2).toFloat()
                        car = car + i.get(3).toFloat()
                        fat=  fat + i.get(4).toFloat()
                        sug = sug + i.get(5).toFloat()
                        vit = vit + i.get(6).toFloat()
                    }
                }
            }
        }
        return listOf(cal, pro, car, fat, sug, vit)
    }

    fun getDateNDaysAgo(initialDate: String?, n: Int): String {
        val formatter = DateTimeFormatter.ofPattern("d_M_yyyy")
        val parsedDate = LocalDate.parse(initialDate, formatter)
        val dateNDaysAgo = parsedDate.minusDays(n.toLong())
        return dateNDaysAgo.format(formatter)
    }

    fun transpose(matrix: List<List<Float>>): List<List<Float>> {
        if (matrix.isEmpty()) {
            return emptyList()
        }

        val numRows = matrix.size
        val numCols = matrix[0].size

        return (0 until numCols).map { col ->
            (0 until numRows).map { row ->
                matrix[row][col]
            }
        }
    }

    fun calculateAverage(matrix: List<List<Float>>): List<Float> {
        return matrix.map { row ->
            val filteredRow = row.filter { it != 0f }
            if (filteredRow.isNotEmpty()) {
                filteredRow.sum() / filteredRow.size
            } else {
                0f
            }
        }
    }

    private fun setDataRadar(list1: List<Float>, list2: List<Float>) {
        var chartList1 = ArrayList<RadarEntry>()
        var chartList2 = ArrayList<RadarEntry>()


        chartList1.add(RadarEntry(list1[0]/2500f))
        chartList1.add(RadarEntry(list1[1]/55f))
        chartList1.add(RadarEntry(list1[2]/325f))
        chartList1.add(RadarEntry(list1[3]/80f))
        chartList1.add(RadarEntry(list1[4]/35f))
        chartList1.add(RadarEntry(list1[5]/100f))


        chartList2.add(RadarEntry(list2[0]/2500f))
        chartList2.add(RadarEntry(list2[1]/55f))
        chartList2.add(RadarEntry(list2[2]/325f))
        chartList2.add(RadarEntry(list2[3]/80f))
        chartList2.add(RadarEntry(list2[4]/35f))
        chartList2.add(RadarEntry(list2[5]/100f))

        val set1 = RadarDataSet(chartList1, "Last 7 days avg")
        set1.color = Color.rgb(5, 0, 125)
        set1.fillColor = Color.rgb(5, 0, 125)
        set1.fillAlpha = 50
        set1.setDrawFilled(true)
        set1.setDrawValues(!set1.isDrawValuesEnabled())

        val set2 = RadarDataSet(chartList2, "Today")
        set2.color = Color.rgb(0, 130, 0)
        set2.fillColor = Color.rgb(0, 130, 0)
        set2.fillAlpha = 50
        set2.setDrawFilled(true)
        set2.setDrawValues(!set2.isDrawValuesEnabled())

        val sets = ArrayList<IRadarDataSet>()
        sets.add(set1)
        sets.add(set2)

        val data = RadarData(sets)
        radar.data = data

    }

    private fun setDataLine(list1: List<Float>): LineData {
        val entries = mutableListOf<Entry>()
        for ((index, value) in list1.withIndex()) {
            entries.add(Entry(index.toFloat(), value))
        }

        val dataSet = LineDataSet(entries, "Line Chart")
        val lineData = LineData(dataSet)

        return lineData
    }
}

