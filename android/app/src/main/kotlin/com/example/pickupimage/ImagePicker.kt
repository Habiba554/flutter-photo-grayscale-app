package com.example.pickupimage

import android.app.Activity
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class ImagePicker : FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware {

    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private var pendingResult: MethodChannel.Result? = null

    private var pickImageLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null

    private val scope = CoroutineScope(Dispatchers.Main + Job())



    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "com.example.app/image_picker")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method == "pickImageFromGallery") {

            if (pendingResult != null) {
                result.error("ALREADY_ACTIVE", "Active", null)
                return
            }

            pendingResult = result
            openGallery()

        } else {
            result.notImplemented()
        }
    }



    private fun openGallery() {
        val launcher = pickImageLauncher

        if (launcher == null) {
            pendingResult?.error("NOT_READY", "Launcher not ready", null)
            pendingResult = null
            return
        }

        try {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (e: Exception) {
            pendingResult?.error("LAUNCH_ERROR", e.message, null)
            pendingResult = null
        }
    }



    private fun registerLauncher(binding: ActivityPluginBinding) {
        val caller = binding.activity as? ActivityResultCaller ?: return

        pickImageLauncher = caller.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            handleResult(uri)
        }
    }

    private fun handleResult(uri: Uri?) {
        val result = pendingResult ?: return
        pendingResult = null

        if (uri == null) {
            result.success(null)
            return
        }

        scope.launch {
            try {
                val path = withContext(Dispatchers.IO) {
                    saveImage(uri)
                }
                result.success(path)
            } catch (e: Exception) {
                result.error("SAVE_ERROR", e.message, null)
            }
        }
    }

    private fun saveImage(uri: Uri): String {
        val context = activity ?: throw Exception("Activity lost")

        val file = File(
            context.cacheDir,
            "picked_${System.currentTimeMillis()}.jpg"
        )

        context.contentResolver.openInputStream(uri).use { input ->
            FileOutputStream(file).use { output ->
                input?.copyTo(output) ?: throw Exception("فشل قراءة الصورة")
            }
        }

        return file.absolutePath
    }

    // -------------------- Lifecycle --------------------

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        registerLauncher(binding)
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        registerLauncher(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onDetachedFromActivity() {
        activity = null
        pickImageLauncher = null
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        scope.cancel()
    }
}