package top.cch.config.controller;

import top.cch.config.properties.StudentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentProperties studentProperties;

    @GetMapping("/info")
    public StudentProperties getStudentInfo() {
        return studentProperties;
    }
}
