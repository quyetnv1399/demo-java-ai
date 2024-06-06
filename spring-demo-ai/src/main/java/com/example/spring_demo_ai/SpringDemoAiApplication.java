package com.example.spring_demo_ai;

import java.util.ArrayList;
import java.util.List;

import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
// import org.nd4j.linalg.util.Paths;
import org.opencv.core.*;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.dnn.*;
import org.opencv.utils.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import org.slf4j.LoggerFactory;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// import ai.djl.ModelException;
// import ai.djl.inference.Predictor;
// import ai.djl.modality.cv.Image;
// import ai.djl.modality.cv.ImageFactory;
// import ai.djl.modality.cv.output.DetectedObjects;
// import ai.djl.repository.zoo.Criteria;
// import ai.djl.repository.zoo.ZooModel;
// import ai.djl.training.util.ProgressBar;
// import ai.djl.translate.TranslateException;

// import org.slf4j.Logger;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
@SpringBootApplication
public class SpringDemoAiApplication {
	public static void main(String[] args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME ); 
		int absoluteFaceSize = 0;
		// // Load the OpenCV library
		MatOfRect faces = new MatOfRect();
		Mat grayFrame = new Mat();

		Mat frame = Imgcodecs.imread("C:/Users/Admin/Downloads/archive/val/elton_john/httpcdncdnjustjaredcomwpcontentuploadsheadlineseltonjohnstillstandingbrooklynnewyearsjpg.jpg");
		if (frame.empty()) {
            System.out.println("Can not read img.");
            return;
        }
		
		//convert the frame in gray scale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(grayFrame, grayFrame);
		
		if(absoluteFaceSize==0) {
			int height = grayFrame.height();
			if(Math.round(height * 0.2f) > 0) {
				absoluteFaceSize = Math.round(height * 0.2f);
			}
		}
		
		//detect faces
		CascadeClassifier faceCascade = new CascadeClassifier();
		faceCascade.load("spring-demo-ai/data/haarcascade_frontalface_alt2.xml");
		faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0|Objdetect.CASCADE_SCALE_IMAGE, 
									new Size(absoluteFaceSize,absoluteFaceSize), new Size()	);
		// System.err.println(faces);
		Rect[] faceArray = faces.toArray();
		System.out.println(faceArray.length);
		for(int i = 0; i < faceArray.length; i++) {
			Imgproc.rectangle(frame, faceArray[i], new Scalar(0,0,255), 3);
		}
		// Imgcodecs.imwrite("out.jpg",grayFrame);

        HighGui.imshow("img", frame);
		HighGui.resizeWindow("img", 600, 600);
        HighGui.waitKey();

		// SpringApplication.run(SpringDemoAiApplication.class, args);
	}
}
