package com.example.quickstart.controller;

import com.example.quickstart.entity.message;
import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quickstart")
public class QuickStartController {

    @GetMapping("/hello")
    public message hello() {
        return new message("Hello, Spring Boot!", "01-quickstart");
    }
}
