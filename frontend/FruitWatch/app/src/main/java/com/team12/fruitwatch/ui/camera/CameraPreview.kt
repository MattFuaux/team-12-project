package com.team12.fruitwatch.ui.camera


import android.content.Context
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

/** A basic Camera preview class */
class CameraPreview(
        context: Context,
        private val camera: Camera
    ) : SurfaceView(context), SurfaceHolder.Callback {
        val TAG = "CameraPreview"
        private val mHolder: SurfaceHolder = holder.apply {
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            addCallback(this@CameraPreview)
            // deprecated setting, but required on Android versions prior to 3.0
            setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            camera.apply {
                try {
                    setCameraDisplayOrientation()
                    setPreviewDisplay(holder)
                    startPreview()
                } catch (e: IOException) {
                    Log.d(TAG, "Error setting camera preview: ${e.message}")
                }
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.
            if (mHolder.surface == null) {
                // preview surface does not exist
                Log.d(TAG,"preview surface does not exist")
                return
            }

            // stop preview before making changes
            try {
                camera.stopPreview()
            } catch (e: Exception) {
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            camera.apply {
                try {
                    setCameraDisplayOrientation()
                    setPreviewDisplay(mHolder)
                    startPreview()
                } catch (e: Exception) {
                    Log.d(TAG, "Error starting camera preview: ${e.message}")
                }
            }
        }

    private fun setCameraDisplayOrientation() {
        val info = CameraInfo()
        Camera.getCameraInfo(0, info)
        val rotation = context.display!!.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int = (info.orientation - degrees + 360) % 360
        Log.d(TAG,"Rotation: $rotation")
        Log.d(TAG,"Display Orientation: $result")
        val parameters = camera.parameters
        parameters.setRotation(result)
        camera.parameters = parameters
        camera.setDisplayOrientation(result)
    }
}