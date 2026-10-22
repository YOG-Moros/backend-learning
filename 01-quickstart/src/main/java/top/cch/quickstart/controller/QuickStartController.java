package top.cch.quickstart.controller;

import top.cch.quickstart.entity.message;
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
