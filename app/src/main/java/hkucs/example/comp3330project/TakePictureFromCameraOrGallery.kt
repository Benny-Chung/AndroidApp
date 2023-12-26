package hkucs.example.comp3330project

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TakePictureFromCameraOrGalley: ActivityResultContract<Unit, Uri?>() {

    private lateinit var photoUri: Uri

    override fun createIntent(context: Context, input: Unit): Intent {
        return openImageIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.data ?: photoUri
    }

    private fun openImageIntent(context: Context): Intent {
        val camIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoUri = createPhotoTakenUri(context)
        // write the captured image to a file
        camIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

        val gallIntent = Intent(Intent.ACTION_GET_CONTENT)
        gallIntent.type = "image/*"

        // look for available intents
        val info = ArrayList<ResolveInfo>()
        val yourIntentsList = ArrayList<Intent>()
        val packageManager = context.packageManager

        packageManager.queryIntentActivities(camIntent, 0).forEach{
            val finalIntent = Intent(camIntent)
            finalIntent.component = ComponentName(it.activityInfo.packageName, it.activityInfo.name)
            yourIntentsList.add(finalIntent)
            info.add(it)
        }

        packageManager.queryIntentActivities(gallIntent, 0).forEach {
            val finalIntent = Intent(gallIntent)
            finalIntent.component = ComponentName(it.activityInfo.packageName, it.activityInfo.name)
            yourIntentsList.add(finalIntent)
            info.add(it)
        }

        val chooser = Intent.createChooser(camIntent, "Select source")
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, yourIntentsList.toTypedArray())

        return chooser

    }

    private fun createFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = context.filesDir
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
    }

    private fun createPhotoTakenUri(context: Context): Uri{
        val file = createFile(context)
        return FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file);
    }
}