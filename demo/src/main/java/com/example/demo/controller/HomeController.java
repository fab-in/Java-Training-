package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @RequestMapping("/")
    public String home() {
        return "Hello and Welcome!";
    }

    @RequestMapping("/about")
    public String about() {
        return "This is a p10 project";
    }

}
