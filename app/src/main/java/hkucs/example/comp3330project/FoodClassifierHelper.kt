package hkucs.example.comp3330project

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import hkucs.example.comp3330project.ml.FoodClassifierV11
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.model.Model


class FoodClassifierHelper(private val context: Context) {
    private var model: FoodClassifierV11? = null
    init {
        val compatList = CompatibilityList()


        val options = if(compatList.isDelegateSupportedOnThisDevice) {
            // if the device has a supported GPU, add the GPU delegate
            Model.Options.Builder().setDevice(Model.Device.GPU).build()
        } else {
            // if the GPU is not supported, run on 4 threads
            Model.Options.Builder().setNumThreads(4).build()
        }

        // Initialize the model as usual feeding in the options object
        model = FoodClassifierV11.newInstance(context, options)
    }

    fun closeModel() {
        model!!.close()
    }

    fun getFoodName(uri: Uri): Category? {
        val lastSegment = uri.lastPathSegment
        val imagePath = "${context.filesDir}/$lastSegment"
        val outputs: FoodClassifierV11.Outputs
        try {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            Log.d("TEST", imagePath)
            val bitmap = BitmapFactory.decodeFile(imagePath, options)
            val image = TensorImage.fromBitmap(bitmap)
            outputs = model!!.process(image)
        } catch (e: Exception) {
            Log.d("ERROR", e.toString())
            return null
        }

        val probableDishes = outputs.probabilityAsCategoryList
        val mostProbableDish = probableDishes.maxByOrNull { it.score }
        if (mostProbableDish != null && mostProbableDish.score >= 0.5) {
            Log.d("TEST", mostProbableDish.label)
            return mostProbableDish
        }
        return null
    }

}