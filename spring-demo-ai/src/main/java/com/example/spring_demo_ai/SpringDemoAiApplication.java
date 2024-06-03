package com.example.spring_demo_ai;

import java.util.ArrayList;
import java.util.List;

import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.*;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.dnn.*;
import org.opencv.utils.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringDemoAiApplication {
	public static void main(String[] args) {
		// Load the OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		String modelWeights = "weights/facedetection/yolov8n.onnx"; 
		Net net = Dnn.readNetFromONNX(modelWeights); 
        String imgPath = "images/yolo.png";
		Mat img = Imgcodecs.imread(imgPath);
		if (img.empty()) {
            System.out.println("Can not read img.");
            return;
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

        HighGui.imshow("img", img);
		HighGui.resizeWindow("img", 600, 600);
        HighGui.waitKey();

		SpringApplication.run(SpringDemoAiApplication.class, args);
	}
}
