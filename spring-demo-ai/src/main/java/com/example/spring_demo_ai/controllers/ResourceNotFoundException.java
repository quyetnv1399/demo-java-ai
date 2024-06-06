package com.example.spring_demo_ai.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "User not found", code = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

}
