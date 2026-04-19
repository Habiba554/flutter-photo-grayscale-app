package com.example.pickupimage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.android.Utils
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream

class OpenCV : FlutterPlugin, MethodCallHandler {
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "opencv_channel")
        channel.setMethodCallHandler(this)

        // Initialize OpenCV
        OpenCVLoader.initLocal()
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "processImage" -> {
                val imageBytes = call.arguments as? ByteArray
                if (imageBytes != null) {
                    processImage(imageBytes, result)
                } else {
                    result.error("INVALID_ARGUMENT", "Image bytes required", null)
                }
            }
            else -> result.notImplemented()
        }
    }

    private fun processImage(imageBytes: ByteArray, result: Result) {
        try {
            val inputMat = Imgcodecs.imdecode(MatOfByte(*imageBytes), Imgcodecs.IMREAD_COLOR)

            if (inputMat.empty()) {
                result.error("PROCESSING_ERROR", "Failed to decode image", null)
                return
            }

            // Apply grayscale filter
            val grayMat = Mat()
            Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY)

            // Convert back to bytes
            val processedBitmap = Bitmap.createBitmap(
                grayMat.cols(), grayMat.rows(), Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(grayMat, processedBitmap)

            val stream = ByteArrayOutputStream()
            processedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)

            result.success(stream.toByteArray())

            inputMat.release()
            grayMat.release()
        } catch (e: Exception) {
            result.error("PROCESSING_ERROR", e.message, null)
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}