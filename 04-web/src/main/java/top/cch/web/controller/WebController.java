package top.cch.web.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web")
public class WebController {
    @GetMapping("/hello")
    public String hello(){
        return "web 模块";
    }
}
