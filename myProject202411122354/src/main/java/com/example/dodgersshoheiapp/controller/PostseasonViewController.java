package com.example.dodgersshoheiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostseasonViewController {

    @GetMapping("/postseason")
    public String showPostseasonPage() {
        return "postseason"; // → templates/postseason.html
    }
}
