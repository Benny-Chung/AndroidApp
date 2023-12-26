package hkucs.example.comp3330project

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException
import android.widget.TextView
import android.widget.Toast
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import org.json.JSONObject
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class BarcodeActivity : AppCompatActivity() {
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeTextView: TextView
    private val DB_URL =
        "jdbc:mysql://comp3330.clwvhleuhg.ap-southeast-2.rds.amazonaws.com:3306/fooddb"
    private val USER = "admin"
    private val PASS = "comp33302023hku"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)
        supportActionBar!!.title = "Barcode Reader"
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            initializeBarcodeScanner()
        }
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


    private fun initializeBarcodeScanner() {
        val barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setAutoFocusEnabled(true)
            .build()

        val cameraView: SurfaceView = findViewById(R.id.cameraView)

        //activity_barcode.xml popup
        barcodeTextView = findViewById(R.id.barcodeTextView)


        cameraView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            this@BarcodeActivity,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    cameraSource.start(cameraView.holder)
                } catch (e: IOException) {
                    Log.d(TAG, "Failed to start Camera.")
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems

                if (barcodes.size() > 0) {
                    val barcode = barcodes.valueAt(0)

                    val barcodeValue = barcode.displayValue
                    // Call a method to retrieve the details from the database based on the barcode value
                    val barcodeDetails = getBarcodeDetailsFromDatabase(barcodeValue).toString()

                    runOnUiThread {
                        val d=stringToDictionary(barcodeDetails)

                        if(!d.containsKey("error")) {
                            if(d.containsKey("name")){
                                showBarcodeDetailsPopup(d["name"].toString())
                            }
                            val intent = Intent(this@BarcodeActivity, CreateFood::class.java)
                            intent.putExtra("barcode_food_name", barcodeDetails)
                            startActivity(intent)
                        }
                        else{
                            showBarcodeDetailsPopup("Cannot find this barcode in database.")
                        }
                    }

                }
            }
        })
    }

    private fun showBarcodeDetailsPopup(barcodeDetails: String) {
        barcodeTextView.text = barcodeDetails
    }


    interface BarcodeDetailsCallback {
        fun onBarcodeDetailsReceived(name: String)
        fun onBarcodeDetailsError(error: String)
    }

    private fun getBarcodeDetailsFromDatabase(barcodeValue: String): Map<String, Any> {
        // Implement the logic to retrieve barcode details from the database based on barcodeValue
        // Return the barcode details as a string
        val apiUrl = "https://www.brocade.io/api/items/$barcodeValue"
        val url = URL(apiUrl)
        val connection = url.openConnection()

        // Set request method to GET
        connection.setRequestProperty("Request-Method", "GET")

        try {
            // Read the response
            val response = StringBuilder()
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()
            // Parse the response string as a dictionary
            val dictionary = mutableMapOf<String, Any>()
            val jsonResponse = JSONObject(response.toString())
            val keys = jsonResponse.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonResponse.get(key)
                dictionary[key] = value
            }
            return dictionary
        } catch (e: IOException) {
            // Handle IO exception, possibly due to a status 500 response
            return mapOf("error" to "Internal Server Error")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeBarcodeScanner()
            } else {
                Toast.makeText(this, "Please give access to camera before proceeding.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001
        private const val TAG = "BarcodeActivity"
    }
}
