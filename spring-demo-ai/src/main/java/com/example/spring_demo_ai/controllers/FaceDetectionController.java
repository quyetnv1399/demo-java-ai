package com.example.spring_demo_ai.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class FaceDetectionController {

    @PostMapping("/analyze-image")
    public ResponseEntity<byte[]> analyzeImage(@RequestParam("file") MultipartFile file) {
        try {
            // Convert MultipartFile to Mat
            byte[] imageData = file.getBytes();
            Mat imageMat = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);

            // Check if image is successfully decoded
            if (imageMat.empty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Convert Mat to byte array
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", imageMat, matOfByte);
            byte[] imageByteArray = matOfByte.toArray();

            // Get image size
            int width = imageMat.cols();
            int height = imageMat.rows();

            // Decode byte array back to image
            Mat decodedImage = Imgcodecs.imdecode(new MatOfByte(imageByteArray), Imgcodecs.IMREAD_COLOR);

            // Generate random file name
            String outputDirectory = "spring-demo-ai/images/output/";
            String outputFileName = generateRandomFileName() + ".jpg";
            String outputPath = outputDirectory + outputFileName;

            // Export decoded image to file
            Imgcodecs.imwrite(outputPath, decodedImage);

            return ResponseEntity.ok().body(imageByteArray);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    private String generateRandomFileName() {
        int length = 10;
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

}
