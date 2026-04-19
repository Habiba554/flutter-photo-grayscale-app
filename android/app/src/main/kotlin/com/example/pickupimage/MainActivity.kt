package com.example.pickupimage

import io.flutter.embedding.android.FlutterActivity

import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.os.Bundle
import com.example.pickupimage.ImagePicker
import com.example.pickupimage.OpenCV
import androidx.annotation.NonNull



class MainActivity : FlutterFragmentActivity() {
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        flutterEngine.plugins.add(ImagePicker())
        flutterEngine.plugins.add(OpenCV())

    }
}
