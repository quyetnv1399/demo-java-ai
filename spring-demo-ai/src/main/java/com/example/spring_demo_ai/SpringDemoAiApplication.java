package com.example.spring_demo_ai;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringDemoAiApplication {

    public static void main(String[] args) {
        // Load the OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // imageDecode();
        // detectFace();
        // testCvtColor();

        SpringApplication.run(SpringDemoAiApplication.class, args);

    }

    public static void testCvtColor() {
        // Đọc hình ảnh gốc
        Mat image = Imgcodecs.imread("images/face-image.jpg");

        // Kiểm tra nếu hình ảnh không được tải thành công
        if (image.empty()) {
            System.out.println("Không thể tải hình ảnh");
            return;
        }

        // Tạo một ma trận mới để chứa hình ảnh xám
        Mat grayFrame = new Mat();

        // Chuyển đổi hình ảnh từ BGR sang xám
        Imgproc.cvtColor(image, grayFrame, Imgproc.COLOR_BGR2GRAY);

        // Cân bằng histogram
        Mat equalizedFrame = new Mat();
        Imgproc.equalizeHist(grayFrame, equalizedFrame);

        // Hiển thị hình ảnh gốc và hình ảnh xám
        HighGui.imshow("Original Image", image);
        HighGui.imshow("Grayscale Image", grayFrame);
        HighGui.imshow("Equalized Image", equalizedFrame);

        // Chờ người dùng nhấn phím để đóng cửa sổ
        HighGui.waitKey(0);
        System.exit(0);
    }

    public static void imageDecode() {

        // Đường dẫn đến file ảnh
        String imagePath = "images/saitama.jpg";

        // // Đọc file ảnh vào một đối tượng Mat
        Mat image = Imgcodecs.imread(imagePath);

        // // Kiểm tra xem ảnh có được đọc thành công hay không
        if (image.empty()) {
            System.out.println("Không thể đọc file ảnh.");
            return;
        }

        // // Hiển thị kích thước của ảnh
        int width = image.cols();
        int height = image.rows();
        System.out.println("Kích thước của ảnh: " + width + "x" + height);

        // // Convert Mat to byte array
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", image, matOfByte);
        byte[] imageByteArray = matOfByte.toArray();
        System.out.println(imageByteArray);

        // // Decode byte array back to image
        Mat decodedImage = Imgcodecs.imdecode(new MatOfByte(imageByteArray), Imgcodecs.IMREAD_COLOR);

        // // Ví dụ: Hiển thị ảnh
        HighGui.imshow("Image", decodedImage);
        HighGui.waitKey();
    }

    public static void detectFace() {
        // Đường dẫn đến file ảnh
        String imagePath = "images/face-image.jpg";
        // Đọc file ảnh vào một đối tượng Mat
        Mat image = Imgcodecs.imread(imagePath);

        // Create method for dectec and save
        detectAndSave(image);
    }

    private static void detectAndSave(Mat image) {
        // Chuyển đổi hình ảnh sang xám
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(image, grayFrame, Imgproc.COLOR_BGR2GRAY);

        // Cân bằng histogram
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // Tính chiều cao của hình ảnh xám
        int height = grayFrame.height();
        int absoluteFaceSize = 0;

        // Xác định kích thước tối thiểu của khuôn mặt
        if (Math.round(height * 0.2f) > 0) {
            absoluteFaceSize = Math.round(height * 0.2f);
        }

        // Tạo CascadeClassifier để nhận diện khuôn mặt
        CascadeClassifier faceCascade = new CascadeClassifier();
        faceCascade.load("data/haarcascade_frontalface_alt2.xml");

        // Phát hiện khuôn mặt với kích thước tối thiểu
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 3, 0, new Size(absoluteFaceSize, absoluteFaceSize), new Size());

        // faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 |
        // Objdetect.CASCADE_SCALE_IMAGE,
        // new Size(absoluteFaceSize, absoluteFaceSize),
        // new Size());
        
        // Vẽ hình chữ nhật quanh các khuôn mặt được phát hiện

        Rect[] faceArray = faces.toArray();
        for (int i = 0; i < faceArray.length; i++) {
            Imgproc.rectangle(image, faceArray[i], new Scalar(0, 0, 255), 2);
        }

        HighGui.imshow("Detected Faces", image);

        // Chờ người dùng nhấn phím để đóng cửa sổ
        HighGui.waitKey(0);
        System.exit(0);
        
    }
}
