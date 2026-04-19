import 'dart:io';
import 'package:flutter/services.dart';

class ImagePickerNative {
  static const MethodChannel _channel =
  MethodChannel('com.example.app/image_picker');

  static Future<File?> pickImageFromGallery({
    double? maxWidth,
    double? maxHeight,
    int? quality,
  }) async {
    try {
      final Map<String, dynamic> arguments = {
        'maxWidth': maxWidth,
        'maxHeight': maxHeight,
        'quality': quality ?? 100,
      };

      final String? imagePath = await _channel.invokeMethod<String>(
          'pickImageFromGallery', arguments);

      if (imagePath != null) {
        return File(imagePath);
      }
      return null;
    } on PlatformException catch (e) {
      print('Error picking image from gallery: ${e.message}');
      return null;
    }
  }
}
