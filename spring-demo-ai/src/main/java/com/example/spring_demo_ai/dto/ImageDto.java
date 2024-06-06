package com.example.spring_demo_ai.dto;

public class ImageDto {

    private String id;
    private String name;
    private String type;

    public ImageDto(){

    }

    public ImageDto(String id ,String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
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
    
}
