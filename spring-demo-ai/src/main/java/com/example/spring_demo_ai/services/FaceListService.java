package com.example.spring_demo_ai.services;

import org.apache.commons.compress.utils.IOUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.Item;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import java.text.DecimalFormat;

@Service
public class FaceListService {
    public void createFaceList(String savePath, String folderPath, String detectModelPath, String recognModelPath)
            throws StreamWriteException, DatabindException, IOException {
        File folder = new File(folderPath);
        File[] listPeople = folder.listFiles();
        Map<String, Mat> featureMap = new HashMap<String, Mat>();

        for (File people : listPeople) {
            File[] listImage = people.listFiles();
            List<List<Double>> listFeature = new ArrayList<List<Double>>();

            for (File image : listImage) {
                Mat imageAbsolute = Imgcodecs.imread(image.getAbsolutePath());
                FaceSystemService fss = new FaceSystemService(imageAbsolute, detectModelPath, recognModelPath);
                fss.faceDetection();
                fss.faceEmbedding();
                List<Mat> faceFeature = fss.getFaceFeature();

                List<Double> list = new ArrayList<Double>();
                for (int i = 0; i < 128; i++) {
                    list.add(faceFeature.get(0).row(0).get(0, i)[0]);
                }
                listFeature.add(list);
            }

            List<Double> avgFeature = new ArrayList<Double>();
            for (int i = 0; i < 128; i++) {
                double tmp = 0.0;
                for (List<Double> feature : listFeature) {
                    tmp += feature.get(i);
                }

                avgFeature.add(tmp / listFeature.size());
            }

            Mat avgFeatureMat = new Mat(1, 128, CvType.CV_32FC1);
            for (int i = 0; i < avgFeature.size(); i++) {
                avgFeatureMat.put(0, i, avgFeature.get(i));
            }
            featureMap.put(people.getName(), avgFeatureMat);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonNode = objectMapper.createObjectNode();

        for (String name : featureMap.keySet()) {
            Double[] featureArray = new Double[128];
            for (int i = 0; i < 128; i++) {
                DecimalFormat decimalFormat = new DecimalFormat("#.#######");
                featureArray[i] = Double.parseDouble(decimalFormat.format(featureMap.get(name).row(0).get(0, i)[0]));
            }
            jsonNode.put(name, Arrays.toString(featureArray));
        }

        objectMapper.writeValue(new File(savePath), jsonNode);
    }

    public void createFaceList2(String detectModelPath, String recognModelPath)
            throws StreamWriteException, DatabindException, IOException, MinioException,
            InvalidKeyException, NoSuchAlgorithmException {

        String endPoint = "http://localhost:9000";
        String key = "minioadmin";
        String bucket = "data";
        String prefix = "images/facelist/";

        MinioClient minioClient = MinioClient.builder()
                .endpoint(endPoint)
                .credentials(key, key)
                .build();

        Map<String, Mat> featureMap = new HashMap<String, Mat>();

        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucket)
                        .prefix(prefix)
                        .delimiter("/")
                        .build());

        for (Result<Item> result : results) {
            Item item = result.get();

            if (item.isDir()) {
                String groupName = item.objectName().substring(prefix.length(), item.objectName().length() - 1);

                System.out.println(groupName);
                Iterable<Result<Item>> subResults = minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(bucket)
                                .prefix(item.objectName())
                                .build());

                List<List<Double>> listFeature = new ArrayList<List<Double>>();
                for (Result<Item> subResult : subResults) {
                    Item subItem = subResult.get();
                    if (!subItem.isDir()) {
                        String objectName = subItem.objectName();
                        System.out.println("Reading image: " + objectName);
                        InputStream stream = minioClient.getObject(
                                GetObjectArgs.builder()
                                        .bucket(bucket)
                                        .object(objectName)
                                        .build());

                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        byte[] tmp = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = stream.read(tmp)) != -1) {
                            buffer.write(tmp, 0, bytesRead);
                        }
                        byte[] imageData = buffer.toByteArray();
                        stream.close();

                        Mat image = Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_UNCHANGED);

                        int numChannels = image.channels();

                        if (numChannels == 4) {
                            // Nếu hình ảnh có 4 kênh, chuyển đổi nó thành hình ảnh RGB bằng cách loại bỏ
                            // kênh alpha
                            Mat rgbImage = new Mat();
                            Imgproc.cvtColor(image, rgbImage, Imgproc.COLOR_BGRA2BGR);

                            // Chuyển đổi kết quả thành mảng byte
                            MatOfByte mob = new MatOfByte();
                            Imgcodecs.imencode(".jpg", rgbImage, mob);
                            imageData = mob.toArray();
                        }

                        MatOfByte mob = new MatOfByte(imageData);
                        Mat finalImage = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_UNCHANGED);

                        FaceSystemService fss = new FaceSystemService(finalImage, detectModelPath, recognModelPath);
                        fss.faceDetection();
                        fss.faceEmbedding();

                        List<Mat> faceFeature = fss.getFaceFeature();
                        List<Double> list = new ArrayList<Double>();
                        for (int i = 0; i < 128; i++) {
                            list.add(faceFeature.get(0).row(0).get(0, i)[0]);
                        }
                        listFeature.add(list);
                    }
                }

                List<Double> avgFeature = new ArrayList<Double>();
                for (int i = 0; i < 128; i++) {
                double tmp = 0.0;
                for (List<Double> feature : listFeature) {
                tmp += feature.get(i);
                }
                avgFeature.add(tmp / listFeature.size());
                }

                Mat avgFeatureMat = new Mat(1, 128, CvType.CV_32FC1);
                for (int i = 0; i < avgFeature.size(); i++) {
                avgFeatureMat.put(0, i, avgFeature.get(i));
                }
                featureMap.put(groupName, avgFeatureMat);

            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonNode = objectMapper.createObjectNode();

        for (String name : featureMap.keySet()) {
            Double[] featureArray = new Double[128];
            for (int i = 0; i < 128; i++) {
                DecimalFormat decimalFormat = new DecimalFormat("#.#######");
                featureArray[i] = Double.parseDouble(decimalFormat.format(featureMap.get(name).row(0).get(0, i)[0]));
            }
            jsonNode.put(name, Arrays.toString(featureArray));
        }

        objectMapper.writeValue(new File("faceList.json"), jsonNode);

    }

    public Map<String, Mat> readFaceList(String faceListPath) throws IOException {
        Map<String, Mat> featureMap = new HashMap<String, Mat>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(new File(faceListPath));

        List<String> names = new ArrayList<String>();
        jsonNode.fieldNames().forEachRemaining(item -> names.add(item));
        for (String name : names) {
            String value1 = jsonNode.get(name).asText().replace("[", "").replace("]", "");
            String[] value2 = value1.split(", ");
            Mat featureMat = new Mat(1, 128, CvType.CV_32FC1);
            for (int i = 0; i < value2.length; i++) {
                featureMat.put(0, i, Double.parseDouble(value2[i]));
            }

            featureMap.put(name, featureMat);
        }

        return featureMap;
    }
}
