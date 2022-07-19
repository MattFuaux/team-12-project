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
import android.media.ExifInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
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
    private var cropWidth: Int? = null
    private var cropHeight: Int? = null
    private var cropPoints: FloatArray? = null
    private var reductionFactor: Double? = null

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

    /** Called when the activity is first created.  */
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
        capture!!.setOnClickListener { // TODO Auto-generated method stub
            captureImage()
        }
        camera = getCameraInstance()
        var parameters = camera!!.parameters
        if (parameters.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE

        } else {
            Log.i(TAG, "Continuous Focus Mode is not supported!")
        }


        var foundOptSize = false
        for(size in  parameters.supportedPictureSizes){
            if(size.width == 1280 && size.height == 720){
                Log.i(TAG,"Setting Picture Size to preferred size of: ${size.width}X${size.height}")
                parameters.setPictureSize(size.width,size.height)
                if(display!!.rotation == 2 || display!!.rotation == 0) {
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
            Log.i(TAG,"Setting Picture Size to: ${sortedPicturesSizes.last().width}X${sortedPicturesSizes.last().height}")
            parameters.setPictureSize(sortedPicturesSizes.last().width,sortedPicturesSizes.last().height)
            if(display!!.rotation == 2 || display!!.rotation == 0) {
                reductionFactor = sortedPicturesSizes.last().width.toDouble()/resources.displayMetrics.heightPixels.toDouble()
            }else{
                reductionFactor = sortedPicturesSizes.last().width.toDouble()/resources.displayMetrics.widthPixels.toDouble()
            }

        }
        camera!!.setParameters(parameters);
        parameters.supportedPictureSizes

        Log.i(TAG,"ReductionFactor is: $reductionFactor")

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


        Log.d(TAG, "Orientation: $orientation")
        var processedBitmap: Bitmap? = null
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> processedBitmap = cropBitmap(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> processedBitmap =                 cropBitmap(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> processedBitmap =                 cropBitmap(bitmap, 270)
            ExifInterface.ORIENTATION_NORMAL -> processedBitmap = cropBitmap(bitmap, 0)
//                Bitmap.createBitmap(
//                bitmap!!,
//                findImageXOrigin(bitmap.width),
//                findImageYOrigin(bitmap.height),
//                cropWidth!!,
//                cropHeight!!
//            )
            else -> processedBitmap = cropBitmap(bitmap, 0)
//            Bitmap.createBitmap(
//                bitmap!!,
//                findImageXOrigin(bitmap.width),
//                findImageYOrigin(bitmap.height),
//                cropWidth!!,
//                cropHeight!!
            //)
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

    private fun findImageXOrigin(imageWidth: Int): Int {
        return (imageWidth / 2) - (cropWidth!! / 2)
    }

    private fun findImageYOrigin(imageHeight: Int): Int {
        return (imageHeight / 2) - (cropHeight!! / 2)
    }

    private fun getCropPoints():FloatArray {
        val itemGuide = findViewById<View>(R.id.cameraprev_itemGuide) as ImageView

        cropWidth = (itemGuide.width* reductionFactor!!).toInt()
        cropHeight = (itemGuide.height* reductionFactor!!).toInt()
        Log.i(TAG,"Crop Width is: $cropWidth")
        Log.i(TAG,"Crop Height is: $cropHeight")
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

    private fun cropBitmap(
        bitmap: Bitmap,
        degreesRotated: Int
    ): Bitmap {
        var result: Bitmap? = null
        // get the points of the crop rectangle adjusted to source bitmap
        val points = getCropPoints()
        val loadedSampleSize = 2
        val orgWidth: Int = bitmap.width * loadedSampleSize;
        val orgHeight: Int = bitmap.height * loadedSampleSize;

        // get the rectangle for the points (it may be larger than original if rotation is not stright)
        val rect = getRectFromPoints(
            points,
            orgWidth,
            orgHeight,
            false,
            1,
            1
        );

        val sampleSize: Int
        try {
            val options = BitmapFactory.Options()
            sampleSize = (1
                    * calculateInSampleSizeByReqestedSize(
                rect.width(),
                rect.height(),
                orgWidth,
                orgHeight
            ))
            //options.inSampleSize = sampleSize

            try {
                // adjust crop points by the sampling because the image is smaller
//                val points2 = FloatArray(points.size)
//                System.arraycopy(points, 0, points2, 0, points.size)
//                for (i in points2.indices) {
//                    points2[i] = points2[i] / sampleSize
//                }
                result = cropBitmapObjectWithScale(
                    bitmap,
                    degreesRotated,
                    points,
                    true,
                    1,
                    1,
                    1f,
                    false,
                    false
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

    private fun cropBitmapObjectWithScale(
        bitmap: Bitmap,
        degreesRotated: Int,
        points:FloatArray,
        fixAspectRatio: Boolean,
        aspectRatioX: Int,
        aspectRatioY: Int,
        scale: Float,
        flipHorizontally: Boolean,
        flipVertically: Boolean
    ): Bitmap? {

        // get the rectangle in original image that contains the required cropped area (larger for non
        // rectangular crop)
        val rect: Rect = getRectFromPoints(
            points,
            bitmap.width,
            bitmap.height,
            fixAspectRatio,
            aspectRatioX,
            aspectRatioY
        )

        // crop and rotate the cropped image in one operation
        val matrix = Matrix()
        matrix.setRotate(
            degreesRotated.toFloat(),
            (bitmap.width / 2).toFloat(),
            (bitmap.height / 2).toFloat()
        )
        matrix.postScale(
            if (flipHorizontally) -scale else scale,
            if (flipVertically) -scale else scale
        )
        val itemGuide = findViewById<View>(R.id.cameraprev_itemGuide) as ImageView
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
            result = cropForRotatedImage(
                result, points, rect, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY
            )
        }
        return result
    }

    private fun cropForRotatedImage(
        bitmap: Bitmap,
        points: FloatArray,
        rect: Rect,
        degreesRotated: Int,
        fixAspectRatio: Boolean,
        aspectRatioX: Int,
        aspectRatioY: Int
    ): Bitmap? {
        var bitmap = bitmap
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
            if (fixAspectRatio) {
                fixRectForAspectRatio(rect, aspectRatioX, aspectRatioY)
            }
            val bitmapTmp = bitmap
            bitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
            if (bitmapTmp != bitmap) {
                bitmapTmp.recycle()
            }
        }
        return bitmap
    }

    private fun calculateInSampleSizeByReqestedSize(
        width: Int, height: Int, reqWidth: Int, reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            while (height / 2 / inSampleSize > reqHeight && width / 2 / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    // 0,1,2,3,4,5,6,7
    // L,T,R,T,R,B,L,B
    fun getRectFromPoints(
        points: FloatArray,
        imageWidth: Int,
        imageHeight: Int,
        fixAspectRatio: Boolean,
        aspectRatioX: Int,
        aspectRatioY: Int
    ): Rect {
        var left = Math.round(Math.max(0f, getRectLeft(points)).toDouble()).toInt()
        var top = Math.round(Math.max(0f, getRectTop(points)).toDouble()).toInt()
        var right = Math.round(Math.max(imageWidth.toFloat(), getRectRight(points)).toDouble()).toInt()
        var bottom = Math.round(Math.min(imageHeight.toFloat(), getRectBottom(points)).toDouble()).toInt()

        val rect = Rect(left, top, right, bottom)
        //val rect = Rect(points[0].toInt(), points[1].toInt(), points[2].toInt(), points[5].toInt())
        if (fixAspectRatio) {
            fixRectForAspectRatio(rect, aspectRatioX, aspectRatioY)
        }
        return rect
    }

    /** Get left value of the bounding rectangle of the given points.  */
    fun getRectLeft(points: FloatArray): Float {
        return Math.min(
            Math.min(
                Math.min(points[0], points[2]),
                points[4]
            ), points[6]
        )
    }

    /** Get top value of the bounding rectangle of the given points.  */
    fun getRectTop(points: FloatArray): Float {
        return Math.min(
            Math.min(
                Math.min(points[1], points[3]),
                points[5]
            ), points[7]
        )
    }

    /** Get right value of the bounding rectangle of the given points.  */
    fun getRectRight(points: FloatArray): Float {
        return Math.max(
            Math.max(
                Math.max(points[0], points[2]),
                points[4]
            ), points[6]
        )
    }

    /** Get bottom value of the bounding rectangle of the given points.  */
    fun getRectBottom(points: FloatArray): Float {
        return Math.max(
            Math.max(
                Math.max(points[1], points[3]),
                points[5]
            ), points[7]
        )
    }

    private fun fixRectForAspectRatio(rect: Rect, aspectRatioX: Int, aspectRatioY: Int) {
        if (aspectRatioX == aspectRatioY && rect.width() != rect.height()) {
            if (rect.height() > rect.width()) {
                rect.bottom -= rect.height() - rect.width()
            } else {
                rect.right -= rect.width() - rect.height()
            }
        }
    }

    private fun captureImage() {
        // TODO Auto-generated method stub
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