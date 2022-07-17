package com.team12.fruitwatch.ui.camera


import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.team12.fruitwatch.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class CameraActivity : Activity() {
    private val TAG = "CameraActivity"
    var goBack: Button? = null
    var capture: Button? = null
    private var camera: Camera? = null
    private var preview: CameraPreview? = null
    private var  cropWidth: Int? = null
    private var cropHeight: Int? = null

    /** Create a File for saving an image or video */
    private fun getOutputImageFile(): File? {
        // path to /data/data/yourapp/app_data/imageDir
        val directory: File = applicationContext.getDir("search_images", Context.MODE_PRIVATE)
        // Create imageDir
        return File(directory, "image.png")
    }

    private val mPicture = PictureCallback { data, camera ->
        val pictureFile: File = getOutputImageFile()!!
        try {
            if(postProcessImage(data, pictureFile)){
                Log.d(TAG, "Image File Saved!")
                setResult(RESULT_OK)
            }else{
                Log.d(TAG, "Image File Save Failed!")
                setResult(RESULT_FAILED)
            }
            finish()
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "File not found: " + e.message)
        } catch (e: IOException) {
            Log.d(TAG, "Error accessing file: " + e.message)
        }
    }


    companion object {
        val RESULT_FAILED = -200
    }

    /** Called when the activity is first created.  */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkCameraHardware(applicationContext)) {
            setResult(RESULT_FAILED)
            finish()
        }

//        cropWidth = (200 * resources.getDisplayMetrics().density).toInt() // Converts 200dp to pixels
//        cropHeight = (200 * resources.getDisplayMetrics().density).toInt() // Converts 200dp to pixels

        cropWidth = 200
        cropHeight = 200

        setContentView(R.layout.activity_camera)

        goBack = findViewById<View>(R.id.goBackBtn) as Button
        capture = findViewById<View>(R.id.captureBtn) as Button
        goBack!!.setOnClickListener { stopCamera() }
        capture!!.setOnClickListener { // TODO Auto-generated method stub
            captureImage()
        }
        camera = getCameraInstance()

        preview = camera?.let {
            // Create our Preview view
            CameraPreview(this, it)
        }

        // Set the Preview view as the content of our activity.
        preview?.also {
            val preview: FrameLayout = findViewById(R.id.camera_preview)
            preview.addView(it)
        }

    }

    // Fixes the Image rotation and crops the image to the boundaries of the guide box
    private fun postProcessImage(imageByteArray: ByteArray, targetFile: File): Boolean {
        val fos = FileOutputStream(targetFile)
        fos.write(imageByteArray)
        fos.close()
        val ei = ExifInterface(targetFile)
        val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        var bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
        Log.d(TAG,"Orientation: $orientation")
        var processedBitmap: Bitmap? = null
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> processedBitmap = rotateAndCropImage(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> processedBitmap = rotateAndCropImage(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> processedBitmap = rotateAndCropImage(bitmap, 270)
            ExifInterface.ORIENTATION_NORMAL -> processedBitmap = Bitmap.createBitmap(bitmap!!, findImageXOrigin(bitmap.width), findImageYOrigin(bitmap.height), cropWidth!!, cropHeight!!)
            else -> processedBitmap = Bitmap.createBitmap(bitmap!!, findImageXOrigin(bitmap.width), findImageYOrigin(bitmap.height), cropWidth!!, cropHeight!!)
        }

        var postProcessedFOS: FileOutputStream? = null
        try {
            postProcessedFOS = FileOutputStream(targetFile)
            // Use the compress method on the BitMap object to write image to the OutputStream
            processedBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, postProcessedFOS)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        } finally {
            try {
                postProcessedFOS?.close()
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
        }
        return true
    }


    fun rotateAndCropImage(source: Bitmap, angle: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            source, findImageXOrigin(source.width), findImageYOrigin(source.height), cropWidth!!,cropHeight!!,
            matrix, true
        )
    }

    fun findImageXOrigin(imageWidth:Int):Int{
        return imageWidth/2-(cropWidth!!/2)
    }

    fun findImageYOrigin(imageHeight:Int):Int{
        return imageHeight/2-(cropHeight!!/2)
    }

    private fun captureImage() {
        // TODO Auto-generated method stub
        camera!!.takePicture(null, null, mPicture)
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCamera() // release the camera immediately on destroy event
    }

    override fun onPause() {
        super.onPause()
        releaseCamera() // release the camera immediately on pause event
    }


    private fun releaseCamera() {
        camera?.release() // release the camera for other applications
        camera = null
    }


    private fun stopCamera() {
        setResult(RESULT_CANCELED)
        finish()
    }

    /** A safe way to get an instance of the Camera object. */
    fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }

    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }
}