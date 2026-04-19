import 'dart:io';

import 'package:flutter/material.dart';import 'package:flutter/services.dart';
import 'package:pickupimage/pickup_image_from_gallery.dart';
typedef ImageCallback = void Function(File? imageFile);
void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: HomeScreen(),
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  Uint8List? _processedImage;
  File? _selectedImage;
  bool _isLoading = false;

  Future<void> _pickAndProcessImage() async {
    setState(() {
      _isLoading = true;
    });
    final File? image = await PickupImageGromGallery.pickImageFromGallery();

    if (image != null) {
      setState(() {
        _selectedImage = image;
      });

      final Uint8List imageBytes = await image.readAsBytes();
      final Uint8List? processed = await _processImage(imageBytes);
      setState(() {
        _processedImage = processed;
      });
    } else {
      print("No image selected.");
    }
    setState(() {
      _isLoading = false;
    });
  }

  Future<Uint8List?> _processImage(Uint8List imageBytes) async {
    const platform = MethodChannel('opencv_channel');
    try {
      final Uint8List? result = await platform.invokeMethod<Uint8List>(
        'processImage',
        imageBytes,
      );
      return result;
    } on PlatformException catch (e) {
      print("Failed to process image: '${e.message}'.");
      return null;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(title: Text('Grayscale Editor',style: TextStyle(fontSize: 24,color: Colors.white),),backgroundColor: Colors.black,),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            if (_isLoading)
              CircularProgressIndicator(color: Colors.white)
            else if (_processedImage != null)
              Image.memory(_processedImage!)
            else
              Text(
                'Pick an image to process',
                style: TextStyle(fontSize: 20, color: Colors.white),
              ),
            SizedBox(height: 60),
            ElevatedButton(
              style: ButtonStyle(
                shape: WidgetStateProperty.all(
                             RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                    )),
                backgroundColor: WidgetStateProperty.all(Colors.blueGrey),
                padding: WidgetStateProperty.all(
                    EdgeInsets.symmetric(horizontal: 20, vertical: 10)),
              ),
              onPressed: _pickAndProcessImage,
              child: Text('Pick and Process Image', style: TextStyle(fontSize: 16,color: Colors.white),)
            ),
          ],
        ),
      ),
    );
  }
  void pickFileFromGallery(
      BuildContext context,
      ImageCallback onSelect,
      ) {
    PickupImageGromGallery.pickImageFromGallery().then((oneItem) {
      if (oneItem != null) {
        onSelect(oneItem);
      } else {
        onSelect(null);
      }
    });
  }
}