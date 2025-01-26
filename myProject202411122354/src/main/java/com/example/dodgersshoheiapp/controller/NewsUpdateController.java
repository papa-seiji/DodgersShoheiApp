package com.example.dodgersshoheiapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dodgersshoheiapp.model.NewsUpdate;
import com.example.dodgersshoheiapp.service.NewsUpdateService;

@RestController
@RequestMapping("/api/news")
public class NewsUpdateController {

    @Autowired
    private NewsUpdateService newsUpdateService;

    @GetMapping
    public List<NewsUpdate> getAllNewsUpdates() {
        // Serviceクラス経由で処理を行う
        return newsUpdateService.getAllNewsUpdates();
    }
}
