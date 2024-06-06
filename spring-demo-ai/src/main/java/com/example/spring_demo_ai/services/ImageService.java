package com.example.spring_demo_ai.services;

import java.io.InputStream;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.spring_demo_ai.models.Image;
import com.example.spring_demo_ai.repositories.ImageRepository;
import com.example.spring_demo_ai.utils.ImageUtils;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;

@Service
public class ImageService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ImageRepository imageRepository;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public byte[] getImage(String id) throws Exception {
        Image image = imageRepository.findById(id).orElseThrow(() -> new RuntimeException("Image not found"));

        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(image.getName())
                        .build());
        return stream.readAllBytes();
    }

    public Image saveImage(MultipartFile file) throws Exception {
        String objectName = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();

        minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(
                        inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());

        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucketName).object(objectName).build());

        Image image = new Image();
        image.setName(objectName);
        image.setType(file.getContentType());
        image.setMinioUrl(url);

        return imageRepository.save(image);
    }

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

    public void detectFace2(Mat image) {
        String modelWeights = "weights/facedetection/yolov8n.onnx";
        Net net = Dnn.readNetFromONNX(modelWeights);

        // Create a blob from the image
        Mat blob = Dnn.blobFromImage(image, 1 / 255.0, new Size(640, 640));
        net.setInput(blob);

        // Perform forward pass and get results
        Mat predict = net.forward();
        Mat mask = predict.reshape(0, 1).reshape(0, predict.size(1));

        double width = image.cols() / 640.0;
        double height = image.rows() / 640.0;
        Rect2d[] rect2d = new Rect2d[mask.cols()];
        float[] scoref = new float[mask.cols()];
        int[] classid = new int[mask.cols()];

        for (int i = 0; i < mask.cols(); i++) {
            Mat score = mask.col(i).submat(4, predict.size(1) - 1, 0, 1);
            MinMaxLocResult mmr = Core.minMaxLoc(score);
            scoref[i] = (float) mmr.maxVal;
            classid[i] = (int) mmr.maxLoc.y;
            double[] x = mask.col(i).get(0, 0);
            double[] y = mask.col(i).get(1, 0);
            double[] w = mask.col(i).get(2, 0);
            double[] h = mask.col(i).get(3, 0);
            rect2d[i] = new Rect2d((x[0] - w[0] / 2) * width, (y[0] - h[0] / 2) * height, w[0] * width, h[0] * height);
        }
        try {
            MatOfRect2d bboxes = new MatOfRect2d(rect2d);
            MatOfFloat scores = new MatOfFloat(scoref);
            MatOfInt indices = new MatOfInt();

            Dnn.NMSBoxes(bboxes, scores, 0.5f, 0.5f, indices);
            List<Integer> result = indices.toList();

            for (Integer integer : result) {
                if (classid[integer] == 0) {
                    Imgproc.rectangle(image, new Rect(rect2d[integer].tl(), rect2d[integer].size()),
                            new Scalar(255, 0, 0), 1);
                    Imgproc.putText(image, "person:" + scoref[integer], rect2d[integer].tl(),
                            Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0));
                }
            }

            String outputDirectory = "images/output/";
            String outputFileName = ImageUtils.generateRandomFileName(10) + ".jpg";
            String outputPath = outputDirectory + outputFileName;

            Imgcodecs.imwrite(outputPath, image);

        } catch (Exception e) {
            System.out.println("No person in image");
        }
    }
}
