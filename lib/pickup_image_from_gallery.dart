import 'dart:io';

import 'package:pickupimage/Image_picker_native.dart' show ImagePickerNative;

class PickupImageGromGallery {
  static Future<File?> pickImageFromGallery({
    double? maxWidth,
    double? maxHeight,
    int? imageQuality,
  }) async {
    final file = await ImagePickerNative.pickImageFromGallery(
        maxHeight: maxHeight, maxWidth: maxWidth, quality: imageQuality);

    if (file != null) {
      print("file sisze = ${file.lengthSync()}");
      return file;
    }
    return null;
  }
}