package com.example.spring_demo_ai.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring_demo_ai.services.ImageService;
import com.example.spring_demo_ai.utils.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;

import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")
public class ImageController {

    @Autowired
    private ImageService imageService;

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
            String outputDirectory = "images/output/";
            String outputFileName = ImageUtils.generateRandomFileName(5) + ".jpg";
            String outputPath = outputDirectory + outputFileName;

            // Export decoded image to file
            Imgcodecs.imwrite(outputPath, decodedImage);

            return ResponseEntity.ok().body(imageByteArray);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

    }

    @PostMapping("/detect-face")
    public ResponseEntity<String> detectFace(@RequestParam("file") MultipartFile file) {
        try {
            // Convert MultipartFile to Mat
            Path tempFile = Files.createTempFile(null, null);
            Files.write(tempFile, file.getBytes());
            Mat image = Imgcodecs.imread(tempFile.toString());
            Files.delete(tempFile);

            if (image.empty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Hình ảnh không hợp lệ");
            }

            // Xử lý logic
            imageService.detectFace(image);

            return ResponseEntity.ok().body("Phân tích khuôn mặt thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi không thể phân tích hình ảnh");
        }
    }

}
