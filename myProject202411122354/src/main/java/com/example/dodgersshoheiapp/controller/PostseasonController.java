package com.example.dodgersshoheiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostseasonController {

    @GetMapping("/postseason")
    public String postseason() {
        return "postseason"; // postseason.html
    }
}
