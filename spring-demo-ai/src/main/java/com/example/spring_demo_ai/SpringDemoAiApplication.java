package com.example.spring_demo_ai;

import java.io.IOException;
import java.util.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.spring_demo_ai.services.FaceListService;
import com.example.spring_demo_ai.services.FaceSystemService;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;

@SpringBootApplication
public class SpringDemoAiApplication {
	public static void main(String[] args) throws StreamWriteException, DatabindException, IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // String folderPath = "spring-demo-ai/images/facelist/";
        // FaceListService frc = new FaceListService();

        // frc.createFaceList("faceList.json", folderPath, 
        //     "spring-demo-ai/weights/facedetection/face_detection_yunet.onnx",
        //     "spring-demo-ai/weights/facerecognition/face_recognition_sface.onnx");

        // Map<String, Mat> featureMap = frc.readFaceList("faceList.json");
        // FaceSystemService fss = new FaceSystemService("spring-demo-ai/images/facelist/madonna/madonna7.jpg", 
        //                                             "spring-demo-ai/weights/facedetection/face_detection_yunet.onnx",
        //                                             "spring-demo-ai/weights/facerecognition/face_recognition_sface.onnx");
        // fss.faceDetection();
        // fss.faceEmbedding();
        // fss.faceRecognition(featureMap);
        // System.err.println(fss.getFeatureMap());
        // fss.visualize();
		SpringApplication.run(SpringDemoAiApplication.class, args);
	}
}
