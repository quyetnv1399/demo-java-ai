package com.example.spring_demo_ai.services;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.stereotype.Service;

import com.example.spring_demo_ai.utils.ImageUtils;

@Service
public class ImageService {
    public void detectFace(Mat image) {
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
        faceCascade.detectMultiScale(grayFrame, faces, 1.1, 3, 0, new Size(absoluteFaceSize, absoluteFaceSize),
                new Size());

        // Vẽ hình chữ nhật quanh các khuôn mặt được phát hiện
        Rect[] faceArray = faces.toArray();
        for (int i = 0; i < faceArray.length; i++) {
            Imgproc.rectangle(image, faceArray[i], new Scalar(0, 0, 255), 2);
        }

        String outputDirectory = "images/output/";
        String outputFileName = ImageUtils.generateRandomFileName(10) + ".jpg";
        String outputPath = outputDirectory + outputFileName;
        
        Imgcodecs.imwrite(outputPath, image);
    }
}
