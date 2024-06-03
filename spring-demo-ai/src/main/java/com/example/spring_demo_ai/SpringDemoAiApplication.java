package com.example.spring_demo_ai;

import org.opencv.core.Core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringDemoAiApplication {
	public static void main(String[] args) {
		// Load the OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // String imgPath = "images/yolo.png";
		// Mat img = Imgcodecs.imread(imgPath);
		// if (img.empty()) {
        //     System.out.println("Can not read img.");
        //     return;
        // }

        // HighGui.imshow("img", img);
		// HighGui.resizeWindow("img", 600, 600);
        // HighGui.waitKey();

		SpringApplication.run(SpringDemoAiApplication.class, args);
	}
}
