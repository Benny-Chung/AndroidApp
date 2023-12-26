package hkucs.example.comp3330project

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.tensorflow.lite.support.label.Category
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class PhotoActivity : AppCompatActivity() {
    private val DB_URL =
        "jdbc:mysql://comp3330.clwvhleuhg.ap-southeast-2.rds.amazonaws.com:3306/fooddb"
    private val USER = "admin"
    private val PASS = "comp33302023hku"

    private var cameraAccess = false
    private var foodCategory: Category? = null
    private var foodClassifierHelper: FoodClassifierHelper? = null
    private var selectedDate: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.title = "Food Classifier"
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_photo)
        foodClassifierHelper = FoodClassifierHelper(this)
        selectedDate = intent.getStringExtra("date")

        cameraAccess = ContextCompat.checkSelfPermission(
            this@PhotoActivity,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        openCamera()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish();
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val requestSinglePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Please give access to camera before proceeding.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@PhotoActivity, MainActivity::class.java)
                intent.putExtra("date", selectedDate)
                startActivity(intent)
            } else {
                cameraAccess = true
                openCamera()
            }
        }

    private val getPicture = registerForActivityResult(TakePictureFromCameraOrGalley()) { uri ->
        if (uri != null) {
            //Log.d("TEST", uri.path.toString())
            // NOTE TO MARKERS:
            // The model only supports following dishes only:
            // https://www.gstatic.com/aihub/tfhub/labelmaps/aiy_food_V1_labelmap.csv
            foodCategory = foodClassifierHelper!!.getFoodName(uri)
            val builder = AlertDialog.Builder(this@PhotoActivity)
            if (foodCategory == null) {
                builder
                    .setMessage("Could not recognise food! Please type the name manually.")
                    .setCancelable(false)
                    .setNeutralButton("Continue") { _, _ ->
                        val intent = Intent(this@PhotoActivity, SearchBar::class.java)
                        intent.putExtra("date", selectedDate)
                        startActivity(intent)
                    }.show()
            } else {
                builder.setTitle("Confirm the classified food")
                    .setMessage("You are trying to input ${foodCategory?.label}")
                    .setCancelable(false)
                    .setPositiveButton("Yes") { dialog, id ->
                        dialog.dismiss()
                        sendFoodName()
                    }
                    .setNeutralButton("Try again") { dialog, id ->
                        // Dismiss the dialog
                        dialog.dismiss()
                        openCamera()
                    }
                    .setNegativeButton("Manual Input") { dialog, id ->
                        // Dismiss the dialog
                        dialog.dismiss()
                        val intent = Intent(this@PhotoActivity, SearchBar::class.java)
                        intent.putExtra("date", selectedDate)
                        startActivity(intent)
                    }.show()
            }

            return@registerForActivityResult
        }
        //Log.d("TEST", "failed to save the pic")
        return@registerForActivityResult
    }

    private fun openCamera() {
        // check if user gave access to the camera or not
        if (!cameraAccess) {
            requestSinglePermissionLauncher.launch(android.Manifest.permission.CAMERA)

            if (!cameraAccess) {
                return
            }
        }
        // set directory folder
        getPicture.launch(Unit)
    }

    private fun sendFoodName() {
        val foodName = foodCategory!!.label
        var similarFoods: ArrayList<String>? = null
        fun getSimilarFoodInDB(foodName: String) {
            Thread {
                val foodNames = ArrayList<String>()
                try {
                    Class.forName("com.mysql.jdbc.Driver")
                    val connection: Connection = DriverManager.getConnection(DB_URL, USER, PASS)
                    val statement: Statement = connection.createStatement()
                    val rs: ResultSet = statement.executeQuery("SELECT name FROM foods WHERE name LIKE '%$foodName%';")
                    while (rs.next()) {
                        val currFoodName = rs.getString("name")
                        if (currFoodName != null && currFoodName != "") {
                            foodNames.add(currFoodName)
                        }
                    }
                    rs.close()
                    statement.close()
                    connection.close()
                    runOnUiThread {
                        similarFoods = foodNames
                       // Log.d("TEST", similarFoods.toString())
                        if (similarFoods!!.isEmpty()) {
                            redirectToCreateFood(foodName)
                        }
                        // Let users pick which food they're inputting
                        val builder: AlertDialog.Builder = AlertDialog.Builder(this@PhotoActivity)
                        val similarItems = if (similarFoods!!.size > 3) similarFoods!!.subList(0, 3) else similarFoods
                        if (similarItems != null) {
                            builder
                                .setTitle("Pick from created food")
                                .setNegativeButton("Not What I Want") { dialog, which ->
                                    dialog.dismiss()
                                    redirectToCreateFood(foodName)
                                }
                                .setSingleChoiceItems(
                                    similarItems.toTypedArray(), -1
                                ) { dialog, which ->
                                    dialog.dismiss()
                                    val selectedDish = similarItems.get(which)
                                    insertFoodToDate(selectedDish, selectedDate!!)
                                    Toast.makeText(this@PhotoActivity, "Inserted food! It may take some time to reflect changes locally!", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this@PhotoActivity, MainActivity::class.java)
                                    intent.putExtra("date", selectedDate)
                                    intent.putExtra("food", foodName)
                                    startActivity(intent)
                                }
                                .setCancelable(false)
                                .show()
                        }


                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
        getSimilarFoodInDB(foodName)
    }

    private fun redirectToCreateFood(foodName: String) {
        // user need to create the food !
        val intent = Intent(this@PhotoActivity, CreateFood::class.java)
        intent.putExtra("classifier_food_name", foodName)
        startActivity(intent)
    }

    private fun insertFoodToDate(foodName: String, date: String) {
        val databaseAccess: DataBaseAccess = DataBaseAccess.getInstance(this)
        databaseAccess.open()
        val foodList = databaseAccess.getFoodList(date)

        (foodList as MutableList<String>?)?.add(foodName)
        databaseAccess.addFood_to_dateRecord(date,foodList)
        databaseAccess.close()
    }
}
