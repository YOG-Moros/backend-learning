package com.example.quickstart.controller;

import com.example.quickstart.entity.message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GreetingController {

    @GetMapping("/hello")
    public message hello() {
        return new message("Hello, Spring Boot!", "01-quickstart");
    }
}
