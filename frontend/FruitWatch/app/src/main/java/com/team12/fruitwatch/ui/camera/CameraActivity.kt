package com.team12.fruitwatch.ui.camera

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import com.team12.fruitwatch.R
import com.team12.fruitwatch.ui.main.MainActivity
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt


class CameraActivity : Activity() {
    private val TAG = "CameraActivity"
    var goBack: Button? = null
    var capture: Button? = null
    private var camera: Camera? = null
    private var preview: CameraPreview? = null
    private var cropWidth: Int? = null
    private var cropHeight: Int? = null
    private var reductionFactor: Double? = null

    // Creates the file to hold a users captures item
    private fun getSavedImageFileFromInternalStorage(): File {
        val directory: File = File(applicationContext.filesDir, "ItemSearchImages")
        if (!directory.exists()) directory.mkdir()
        if (MainActivity.IN_DEVELOPMENT && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            return File(directory, "lime.jpg")
        }else{
            return File(directory, "image.png")
        }
    }

    // This method handles the result of a picture being taken
    private val mPicture = PictureCallback { data, camera ->
        val pictureFile: File = getSavedImageFileFromInternalStorage()
        try {
            if (postProcessImage(data, pictureFile)) {
                Log.d(TAG, "Image File Saved!")
                setResult(RESULT_OK)
            } else {
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

    // Called when the activity is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkCameraHardware(applicationContext)) {
            setResult(RESULT_FAILED)
            finish()
        }
        setContentView(R.layout.activity_camera)
        goBack = findViewById<View>(R.id.goBackBtn) as Button
        capture = findViewById<View>(R.id.captureBtn) as Button
        goBack!!.setOnClickListener { stopCamera() }
        capture!!.setOnClickListener {
            captureImage()
        }

        camera = getCameraInstance()
        if(camera == null){
            setResult(RESULT_FAILED)
            finish()
            return
        }

        var parameters = camera!!.parameters
        if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        } else {
            Log.i(TAG, "Continuous Focus Mode is not supported!")
        }

        //Looks for a common camera resolution size so the picture captured will be a balance of good quality and small size
        var foundOptSize = false
        for(size in  parameters.supportedPictureSizes){
            if(size.width == 1280 && size.height == 720){
                //Log.i(TAG,"Setting Picture Size to preferred size of: ${size.width}X${size.height}")
                parameters.setPictureSize(size.width,size.height)
                if(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        display!!.rotation == 2 || display!!.rotation == 0
                    } else {
                        val display: Display = (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
                        display.rotation == 2 || display.rotation == 0
                    }) {
                    reductionFactor =
                        size.width.toDouble() / resources.displayMetrics.heightPixels.toDouble()
                }else{
                    reductionFactor =
                        size.width.toDouble() / resources.displayMetrics.widthPixels.toDouble()
                }
                foundOptSize = true
            }
        }
        if(!foundOptSize){
            val sortedPicturesSizes = parameters.supportedPictureSizes.sortedBy { it.width*it.height}
            //Log.i(TAG,"Setting Picture Size to: ${sortedPicturesSizes.last().width}X${sortedPicturesSizes.last().height}")
            parameters.setPictureSize(sortedPicturesSizes.last().width,sortedPicturesSizes.last().height)
            if(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    display!!.rotation == 2 || display!!.rotation == 0
                } else {
                    val display: Display = (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
                    display.rotation == 2 || display.rotation == 0
                }
            ) {
                reductionFactor = sortedPicturesSizes.last().width.toDouble()/resources.displayMetrics.heightPixels.toDouble()
            }else{
                reductionFactor = sortedPicturesSizes.last().width.toDouble()/resources.displayMetrics.widthPixels.toDouble()
            }
        }
        camera!!.parameters = parameters
        parameters.supportedPictureSizes

        //Log.i(TAG,"ReductionFactor is: $reductionFactor")

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
        //Log.d(TAG, "Orientation: $orientation")
        var processedBitmap: Bitmap? = null
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> processedBitmap = cropBitmap(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> processedBitmap = cropBitmap(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> processedBitmap = cropBitmap(bitmap, 270)
            ExifInterface.ORIENTATION_NORMAL -> processedBitmap = cropBitmap(bitmap, 0)
            else -> processedBitmap = cropBitmap(bitmap, 0)
        }
        var postProcessedFOS: FileOutputStream? = null
        try {
            postProcessedFOS = FileOutputStream(targetFile)
            // Use the compress method on the BitMap object to write image to the OutputStream
            processedBitmap.compress(Bitmap.CompressFormat.PNG, 100, postProcessedFOS)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        } finally {
            try {
                postProcessedFOS?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return true
    }

    // Get the X-axis Coordinates of the image within the image guide box shown on screen
    private fun findImageXOrigin(imageWidth: Int): Int {
        return (imageWidth / 2) - (cropWidth!! / 2)
    }

    // Get the Y-axis Coordinates of the image within the image guide box shown on screen
    private fun findImageYOrigin(imageHeight: Int): Int {
        return (imageHeight / 2) - (cropHeight!! / 2)
    }

    // Get the points/edges of the image guide box shown on screen
    private fun getCropPoints():FloatArray {
        val itemGuide = findViewById<View>(R.id.cameraprev_itemGuide) as ImageView
        cropWidth = (itemGuide.width* reductionFactor!!).toInt()
        cropHeight = (itemGuide.height* reductionFactor!!).toInt()
        //Log.i(TAG,"Crop Width is: $cropWidth")
        //Log.i(TAG,"Crop Height is: $cropHeight")
        return floatArrayOf(
            itemGuide.left.toFloat(),
            itemGuide.top.toFloat(),
            itemGuide.right.toFloat(),
            itemGuide.top.toFloat(),
            itemGuide.right.toFloat(),
            itemGuide.bottom.toFloat(),
            itemGuide.left.toFloat(),
            itemGuide.bottom.toFloat()
        )
    }

    // Crop the captured image to the edges of the item guide box
    private fun cropBitmap(
        bitmap: Bitmap,
        degreesRotated: Int
    ): Bitmap {
        var result: Bitmap? = null
        // get the points of the crop rectangle adjusted to source bitmap
        val points = getCropPoints()
        try {
            try {
                result = cropBitmapObjectWithScale(
                    bitmap,
                    degreesRotated,
                    points
                )
            } finally {
                if (result != bitmap) {
                    bitmap.recycle()
                }
            }
        } catch (e: OutOfMemoryError) {
            result?.recycle()
            throw e
        } catch (e: java.lang.Exception) {
            throw RuntimeException(
                """
                Failed to load bitmap: $bitmap
                ${e.message}
                """.trimIndent(), e
            )
        }
        return result!!
    }

    // Crop the captured image to the edges of the item guide box with image scaling
    private fun cropBitmapObjectWithScale(
        bitmap: Bitmap,
        degreesRotated: Int,
        points:FloatArray
    ): Bitmap {

        // get the rectangle in original image that contains the required cropped area (larger for non
        // rectangular crop)
        val rect: Rect = getRectFromPoints(
            points,
            bitmap.width,
            bitmap.height
        )

        // crop and rotate the cropped image in one operation
        val matrix = Matrix()
        matrix.setRotate(
            degreesRotated.toFloat(),
            (bitmap.width / 2).toFloat(),
            (bitmap.height / 2).toFloat()
        )
        matrix.postScale(
            1f,
            1f
        )
        var result = Bitmap.createBitmap(
            bitmap,
            findImageXOrigin(bitmap.width),
            findImageYOrigin(bitmap.height),
            cropWidth!!,
            cropHeight!!,
            matrix,
            true
        )
        if (result == bitmap) {
            // corner case when all bitmap is selected, no worth optimizing for it
            result = bitmap.copy(bitmap.config, false)
        }

        // rotating by 0, 90, 180 or 270 degrees doesn't require extra cropping
        if (degreesRotated % 90 != 0) {
            // extra crop because non rectangular crop cannot be done directly on the image without
            // rotating first
            result = cropForRotatedImage(result, points, rect, degreesRotated)
        }
        return result
    }

    // Crop the captured image to the edges of the item guide box even if the device is rotated
    private fun cropForRotatedImage(
        bitmap: Bitmap,
        points: FloatArray,
        rect: Rect,
        degreesRotated: Int
    ): Bitmap {
        var bm = bitmap
        if (degreesRotated % 90 != 0) {
            var adjLeft = 0
            var adjTop = 0
            var width = 0
            var height = 0
            val rads = Math.toRadians(degreesRotated.toDouble())
            val compareTo =
                if (degreesRotated < 90 || degreesRotated > 180 && degreesRotated < 270) rect.left else rect.right
            var i = 0
            while (i < points.size) {
                if (points[i] >= compareTo - 1 && points[i] <= compareTo + 1) {
                    adjLeft = Math.abs(Math.sin(rads) * (rect.bottom - points[i + 1])).toInt()
                    adjTop = Math.abs(Math.cos(rads) * (points[i + 1] - rect.top)).toInt()
                    width = Math.abs((points[i + 1] - rect.top) / Math.sin(rads)).toInt()
                    height = Math.abs((rect.bottom - points[i + 1]) / Math.cos(rads)).toInt()
                    break
                }
                i += 2
            }
            rect[adjLeft, adjTop, adjLeft + width] = adjTop + height
            fixRectForAspectRatio(rect)
            val bitmapTmp = bm
            bm = Bitmap.createBitmap(bm, rect.left, rect.top, rect.width(), rect.height())
            if (bitmapTmp != bm) {
                bitmapTmp.recycle()
            }
        }
        return bm
    }

    // Create a Rectangle object from the item guide box shown on the UI
    private fun getRectFromPoints(
        points: FloatArray,
        imageWidth: Int,
        imageHeight: Int
    ): Rect {
        val left = 0f.coerceAtLeast(getRectLeft(points)).toDouble().roundToInt()
        val top = 0f.coerceAtLeast(getRectTop(points)).toDouble().roundToInt()
        val right = imageWidth.toFloat().coerceAtLeast(getRectRight(points)).toDouble().roundToInt()
        val bottom = imageHeight.toFloat().coerceAtMost(getRectBottom(points)).toDouble().roundToInt()
        val rect = Rect(left, top, right, bottom)
        fixRectForAspectRatio(rect)
        return rect
    }

    /** Get left value of the bounding rectangle of the given points.  */
    private fun getRectLeft(points: FloatArray): Float {
        return Math.min(
            Math.min(
                Math.min(points[0], points[2]),
                points[4]
            ), points[6]
        )
    }

    /** Get top value of the bounding rectangle of the given points.  */
    private fun getRectTop(points: FloatArray): Float {
        return Math.min(
            Math.min(
                Math.min(points[1], points[3]),
                points[5]
            ), points[7]
        )
    }

    /** Get right value of the bounding rectangle of the given points.  */
    private fun getRectRight(points: FloatArray): Float {
        return Math.max(
            Math.max(
                Math.max(points[0], points[2]),
                points[4]
            ), points[6]
        )
    }

    /** Get bottom value of the bounding rectangle of the given points.  */
    private fun getRectBottom(points: FloatArray): Float {
        return Math.max(
            Math.max(
                Math.max(points[1], points[3]),
                points[5]
            ), points[7]
        )
    }

    //
    private fun fixRectForAspectRatio(rect: Rect) {
        if (rect.width() != rect.height()) {
            if (rect.height() > rect.width()) {
                rect.bottom -= rect.height() - rect.width()
            } else {
                rect.right -= rect.width() - rect.height()
            }
        }
    }

    // Start taking the picture
    private fun captureImage() {
        if (cropWidth == null) {
            getCropPoints()
        }
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


    // Clear the camera related variables
    private fun releaseCamera() {
        camera?.release() // release the camera for other applications
        camera = null
    }


    // Finish the Camera Activity
    private fun stopCamera() {
        setResult(RESULT_CANCELED)
        finish()
    }

    /** A safe way to get an instance of the Camera object. */
    private fun getCameraInstance(): Camera? {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            setResult(RESULT_FAILED)
            finish()
            // Camera is not available (in use or does not exist)
            null // returns null if camera is unavailable
        }
    }

    // Check if the device has a camera
    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
}