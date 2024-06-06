package com.example.spring_demo_ai.services;

import org.opencv.core.*;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

// @RestController
public class FaceListService {
    public void createFaceList(String savePath, String folderPath, String detectModelPath, String recognModelPath) 
                throws StreamWriteException, DatabindException, IOException {
        File folder = new File(folderPath);
        File[] listPeople = folder.listFiles();
        Map<String, Mat> featureMap = new HashMap<String, Mat>();

        for(File people:listPeople) {
            File[] listImage = people.listFiles();
            List<List<Double>> listFeature = new ArrayList<List<Double>>();

            for(File image:listImage) {
                FaceSystemService fss = new FaceSystemService(image.getAbsolutePath(), 
                                                            detectModelPath, recognModelPath);
                fss.faceDetection();
                fss.faceEmbedding();
                List<Mat> faceFeature = fss.getFaceFeature();

                List<Double> list = new ArrayList<Double>();
                for(int i = 0; i < 128; i++) {
                    list.add(faceFeature.get(0).row(0).get(0, i)[0]);
                }
                listFeature.add(list);
            }

            List<Double> avgFeature = new ArrayList<Double>();
            for(int i = 0; i < 128; i++) {
                double tmp = 0.0;
                for(List<Double> feature:listFeature) {
                    tmp += feature.get(i);
                }

                avgFeature.add(tmp/listFeature.size());
            }

            Mat avgFeatureMat = new Mat(1, 128, CvType.CV_32FC1);
            for(int i = 0; i < avgFeature.size(); i++) {
                avgFeatureMat.put(0, i, avgFeature.get(i));
            }
            featureMap.put(people.getName(), avgFeatureMat);
        }

        ObjectMapper objectMapper = new ObjectMapper(); 
        ObjectNode jsonNode = objectMapper.createObjectNode();

        for(String name:featureMap.keySet()) {
            Double[] featureArray = new Double[128];
            for(int i = 0; i < 128; i++) {
                DecimalFormat decimalFormat = new DecimalFormat("#.#######");
                featureArray[i] = Double.parseDouble(decimalFormat.format(featureMap.get(name).row(0).get(0, i)[0]));
            }
            jsonNode.put(name, Arrays.toString(featureArray));
        }
        
        objectMapper.writeValue(new File(savePath), jsonNode); 
    }

    public Map<String, Mat> readFaceList(String faceListPath) throws IOException {
        Map<String, Mat> featureMap = new HashMap<String, Mat>();
        ObjectMapper objectMapper = new ObjectMapper(); 
        JsonNode jsonNode = objectMapper.readTree(new File(faceListPath));
        
        List<String> names = new ArrayList<String>();
        jsonNode.fieldNames().forEachRemaining(item -> names.add(item));
        for(String name:names) {
            String value1 = jsonNode.get(name).asText().replace("[", "").replace("]", "");
            String[] value2 = value1.split(", ");
            Mat featureMat = new Mat(1, 128, CvType.CV_32FC1);
            for(int i = 0; i < value2.length; i++) {
                featureMat.put(0, i, Double.parseDouble(value2[i]));
            }

            featureMap.put(name, featureMat);
        }

        return featureMap;
    }
}
