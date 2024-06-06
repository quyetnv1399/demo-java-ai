package com.example.spring_demo_ai.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.spring_demo_ai.models.Image;

public interface ImageRepository extends JpaRepository<Image, String> {

}
