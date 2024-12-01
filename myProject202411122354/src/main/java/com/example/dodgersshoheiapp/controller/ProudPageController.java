package com.example.dodgersshoheiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProudPageController {

    @GetMapping("/proud")
    public String showProudPage() {
        return "proud"; // templates/proud.html を返す
    }
}
