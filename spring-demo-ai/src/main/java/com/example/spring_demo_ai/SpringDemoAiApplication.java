package com.example.spring_demo_ai;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringDemoAiApplication {

	public static void main(String[] args) {
		// Load the OpenCV library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		//  // Đường dẫn đến file ảnh
        // String imagePath = "images/saitama.jpg";

        // // Đọc file ảnh vào một đối tượng Mat
        // Mat image = Imgcodecs.imread(imagePath);

        // // Kiểm tra xem ảnh có được đọc thành công hay không
        // if (image.empty()) {
        //     System.out.println("Không thể đọc file ảnh.");
        //     return;
        // }

        // // Hiển thị kích thước của ảnh
        // int width = image.cols();
        // int height = image.rows();
        // System.out.println("Kích thước của ảnh: " + width + "x" + height);

		// // Convert Mat to byte array
		// MatOfByte matOfByte = new MatOfByte();
		// Imgcodecs.imencode(".jpg", image, matOfByte);
		// byte[] imageByteArray = matOfByte.toArray();
		// System.out.println(imageByteArray);

		// // Decode byte array back to image
		// Mat decodedImage = Imgcodecs.imdecode(new MatOfByte(imageByteArray), Imgcodecs.IMREAD_COLOR);

        // // Ví dụ: Hiển thị ảnh
        // HighGui.imshow("Image", decodedImage);
        // HighGui.waitKey();

		SpringApplication.run(SpringDemoAiApplication.class, args);

	}

}
