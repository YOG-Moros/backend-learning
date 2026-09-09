package com.example.quickstart.controller;

import com.example.quickstart.model.GreetingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GreetingController {

    @GetMapping("/hello")
    public GreetingResponse hello() {
        return new GreetingResponse("Hello, Spring Boot!", "01-quickstart");
    }
}
