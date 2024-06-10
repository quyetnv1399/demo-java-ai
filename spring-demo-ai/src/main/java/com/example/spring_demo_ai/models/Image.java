package com.example.spring_demo_ai.models;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.Table;

@Entity
@Table(name = "images")
public class Image {

    @Id
    @UuidGenerator
    @Column(name = "id", unique = true)
    private String id;

    private String name;
    private String type;
    private String recognizeName;

    @Column(name = "minioUrl", length = 1000)
    private String minioUrl;
    
    @Column(name = "embedVector", columnDefinition = "jsonb")
    private String embedVector; 

    public Image(){

    }

    public Image(String name, String type, String recognizeName, String minioUrl, String embedVector) {
        this.name = name;
        this.type = type;
        this.recognizeName = recognizeName;
        this.minioUrl = minioUrl;
        this.embedVector = embedVector;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRecognizeName() {
        return recognizeName;
    }

    public void setRecognizeName(String recognizeName) {
        this.recognizeName = recognizeName;
    }

    public String getMinioUrl() {
        return minioUrl;
    }

    public void setMinioUrl(String minioUrl) {
        this.minioUrl = minioUrl;
    }

    public String getEmbedVector() {
        return embedVector;
    }

    public void setEmbedVector(String embedVector) {
        this.embedVector = embedVector;
    }

}
