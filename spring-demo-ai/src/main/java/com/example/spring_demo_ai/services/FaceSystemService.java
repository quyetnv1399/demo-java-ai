package com.example.spring_demo_ai.services;

import java.util.*;
import org.opencv.objdetect.FaceDetectorYN;
import org.opencv.objdetect.FaceRecognizerSF;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.Imgproc;

public class FaceSystemService {
    private FaceDetectorYN detectModel;
    private FaceRecognizerSF recognModel;
    private Mat inputImage;
    private Mat imageRectangle;
    private List<Rect> bboxes;
    private List<Mat> faceCroped;
    private List<Mat> faceFeature;
    private List<Map<String, Double>> featureMap;

    public FaceSystemService(Mat inputImagePath, String detectModelPath, String recognModelPath) {
        this.inputImage = inputImagePath;
        if (this.inputImage.empty()) {
            System.out.println("Can not read img.");
        }
        this.imageRectangle = this.inputImage.clone();

        this.detectModel = FaceDetectorYN.create(detectModelPath, "", new Size(320, 320));
        this.recognModel = FaceRecognizerSF.create(recognModelPath, "");
        this.bboxes = new ArrayList<Rect>();
        this.faceCroped = new ArrayList<Mat>();
        this.faceFeature = new ArrayList<Mat>();
        this.featureMap = new ArrayList<Map<String, Double>>();
    }

    public Mat getImageRectangle() {
        return this.imageRectangle;
    }

    public List<Mat> getFaceCroped() {
        return this.faceCroped;
    }

    public List<Mat> getFaceFeature() {
        return this.faceFeature;
    }

    public List<Map<String, Double>> getFeatureMap() {
        return this.featureMap;
    }

    public void faceDetection() {
        Rect rect_Crop = null;
        this.detectModel.setInputSize(this.inputImage.size());
        Mat faces = new Mat();
        this.detectModel.detect(this.inputImage, faces);

        if (faces.rows() < 1) {
                System.out.println("Cannot find a face in ");
                System.exit(0);
        }

        // System.err.println(faces);
		for(int i = 0; i < faces.rows(); i++) {
            double[] x_tl = faces.row(i).get(0, 0);
            double[] y_tl = faces.row(i).get(0, 1);
            double[] w = faces.row(i).get(0, 2);
            double[] h = faces.row(i).get(0, 3);

            rect_Crop = new Rect(((int)x_tl[0]), ((int)y_tl[0]), ((int)w[0]), ((int)h[0]));
            this.faceCroped.add(new Mat(this.inputImage, rect_Crop));
            this.bboxes.add(new Rect(new Point(((int)x_tl[0]), ((int)y_tl[0])), new Size(((int)w[0]), ((int)h[0]))));
			Imgproc.rectangle(this.imageRectangle, new Rect(new Point(((int)x_tl[0]), ((int)y_tl[0])), new Size(((int)w[0]), ((int)h[0]))), 
                            new Scalar(0,0,255), 1);
		}
    }

    public Mat faceAlignment(Mat faceCroped) {
		Mat aligned_face = new Mat();
        this.recognModel.alignCrop(faceCroped, faceCroped.row(0), aligned_face);

        return aligned_face;
    }

    public void faceEmbedding() {
        for(Mat face:this.faceCroped) {
            Mat feature = new Mat();
            Mat faceAligned = this.faceAlignment(face);
            this.recognModel.feature(faceAligned, feature);
            Mat feature_current = feature.clone();
            this.faceFeature.add(feature_current);
        }
    }

    public void faceRecognition(Map<String, Mat> featureList) {
        for(Mat feature:this.faceFeature){
            Mat feature_current = feature.clone();
            double tmpCosScore = 0.363;
            Map<String, Double> cosScoreMap = Map.of("Guess", 1.0);
            for(String name:featureList.keySet()) {
                double cosScore = this.recognModel.match(feature_current, featureList.get(name), FaceRecognizerSF.FR_COSINE);
                System.out.println(Map.of(name, cosScore));
                if(tmpCosScore < cosScore) {
                    cosScoreMap = Map.of(name, cosScore);
                    tmpCosScore = cosScore;
                }
            }

            this.featureMap.add(cosScoreMap);
        }
    }

    public void visualize() {
        for(int i = 0; i < this.featureMap.size(); i++) {
            for(String name:this.featureMap.get(i).keySet()){
                double score = (double)(Math.round(this.featureMap.get(i).get(name)*100))/100;
                String text = name + ": " + score;
                double position_x = this.bboxes.get(i).tl().x - 10;
                double position_y = this.bboxes.get(i).tl().y - 10;
                Imgproc.putText(this.imageRectangle, text, new Point(position_x, position_y), 
                                Imgproc.FONT_ITALIC, 0.4, new Scalar(0, 0, 255), 1); 
            }
        }
        HighGui.imshow("img", this.imageRectangle);
        HighGui.resizeWindow("img", 600, 600);
        HighGui.waitKey();
    }

    public void visualize2() {
        for(int i = 0; i < this.featureMap.size(); i++) {
            for(String name:this.featureMap.get(i).keySet()){
                double score = (double)(Math.round(this.featureMap.get(i).get(name)*100))/100;
                String text = name + ": " + score;
                double position_x = this.bboxes.get(i).tl().x - 10;
                double position_y = this.bboxes.get(i).tl().y - 10;
                Imgproc.putText(this.imageRectangle, text, new Point(position_x, position_y), 
                                Imgproc.FONT_ITALIC, 0.4, new Scalar(0, 0, 255), 1); 
            }
        }
    }
}
