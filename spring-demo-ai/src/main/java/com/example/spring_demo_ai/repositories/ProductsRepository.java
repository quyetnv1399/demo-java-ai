package com.example.spring_demo_ai.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.spring_demo_ai.models.Product;

@Repository
public interface ProductsRepository extends JpaRepository<Product, String> {

}
