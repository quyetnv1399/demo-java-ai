package com.example.spring_demo_ai.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.spring_demo_ai.models.Product;
import com.example.spring_demo_ai.services.ProductService;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public List<Product> getAllProduct() {
        List<Product> listOfProducts = productService.findAll();
        return listOfProducts;
    }

    @PostMapping("/product")
    public ResponseEntity<Product> createProduct(@RequestBody Product input) {
        return ResponseEntity.ok(productService.save(input));
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product input) {
        input.setId(id);
        Product updatedProduct = productService.updateProduct(id, input);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

}
