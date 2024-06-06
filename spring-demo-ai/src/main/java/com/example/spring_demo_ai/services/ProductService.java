package com.example.spring_demo_ai.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.spring_demo_ai.controllers.ResourceNotFoundException;
import com.example.spring_demo_ai.models.Product;
import com.example.spring_demo_ai.repositories.ProductsRepository;

@Service
public class ProductService {

    @Autowired
    private ProductsRepository productsRepository;

    public List<Product> findAll() {
        return productsRepository.findAll();
    }

    public Optional<Product> findById(String id) {
        return productsRepository.findById(id);
    }

    public Product save(Product user) {
        return productsRepository.save(user);
    }

    public Product updateProduct(String id, Product product) {
        return productsRepository.findById(id)
        .map(p -> {
            p.setName(product.getName());
            
            // Cập nhật các trường khác nếu cần
            return productsRepository.save(product);
        })
        .orElseThrow(() -> new ResourceNotFoundException());
    }

    public void delete(Product product) {
        productsRepository.delete(product);
    }
}
