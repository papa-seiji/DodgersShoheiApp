package com.example.dodgersshoheiapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dodgersshoheiapp.model.NewsUpdate;
import com.example.dodgersshoheiapp.repository.NewsUpdateRepository;

@RestController
@RequestMapping("/api/news")
public class NewsUpdateController {

    @Autowired
    private NewsUpdateRepository newsUpdateRepository;

    @GetMapping
    public List<NewsUpdate> getAllNewsUpdates() {
        return newsUpdateRepository.findAllByOrderByCreatedAtDesc();
    }
}
