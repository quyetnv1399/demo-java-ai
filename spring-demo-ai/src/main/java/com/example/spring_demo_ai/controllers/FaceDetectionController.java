package com.example.spring_demo_ai.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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

    public Mat faceDetection(String imgPath, String modelWeight) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Net net = Dnn.readNetFromONNX(modelWeight); 
		Mat img = Imgcodecs.imread(imgPath);
		if (img.empty()) {
            System.out.println("Can not read img.");
            return null;
        }

		Mat blob = Dnn.blobFromImage(img, 1/255.0, new Size(640, 640));
		net.setInput(blob);
		Mat predict = net.forward();
		Mat mask = predict.reshape(0, 1).reshape(0, predict.size(1));

		double width = img.cols()/640.0;
		double height = img.rows()/640.0;
		Rect2d[] rect2d = new Rect2d[mask.cols()];
		float[] scoref = new float[mask.cols()];
		int[] classid = new int[mask.cols()];
		
		for(int i = 0; i < mask.cols(); i++) {
			Mat score = mask.col(i).submat(4, predict.size(1) - 1, 0, 1);
			MinMaxLocResult mmr = Core.minMaxLoc(score);
			scoref[i] = (float) mmr.maxVal;
			classid[i] = (int) mmr.maxLoc.y;
			double[] x = mask.col(i).get(0, 0);
			double[] y = mask.col(i).get(1, 0);
			double[] w = mask.col(i).get(2, 0);
			double[] h = mask.col(i).get(3, 0);
			rect2d[i] = new Rect2d((x[0]-w[0]/2)*width, (y[0]-h[0]/2)*height, w[0]*width, h[0]*height);
		}
		try {
			MatOfRect2d bboxes = new MatOfRect2d(rect2d);
			MatOfFloat scores = new MatOfFloat(scoref);
			MatOfInt indices = new MatOfInt();
			
			Dnn.NMSBoxes(bboxes, scores, 0.5f,0.5f, indices);
			List<Integer> result = indices.toList();
			
			for (Integer integer : result) {
				if(classid[integer] == 0) { 
					Imgproc.rectangle(img, new Rect(rect2d[integer].tl(), rect2d[integer].size()),
								new Scalar(255, 0, 0), 1);
					Imgproc.putText(img, "person:" + scoref[integer], rect2d[integer].tl(), 
							Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0));
				}
			}
		} catch (Exception e) {
			System.out.println("No person in image");
		}

        return img;
    }

    public void visualizeFaceDetection(Mat img) {
        HighGui.imshow("Image", img);
		HighGui.resizeWindow("Image", 600, 600);
        HighGui.waitKey();
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

    public void main(String args[]) {
        String imgPath = "spring-demo-ai/images/yolo.png";
        String modelWeight = "spring-demo-ai/weights/facedetection/yolov8n.onnx";
        
        Mat img = this.faceDetection(imgPath, modelWeight);
        this.visualizeFaceDetection(img);
    }

}
