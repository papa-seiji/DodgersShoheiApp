package com.example.dodgersshoheiapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dodgersshoheiapp.service.VisitorCounterService;

@RestController
@RequestMapping("/api")
public class VisitorCounterController {

    private final VisitorCounterService visitorCounterService;

    public VisitorCounterController(VisitorCounterService visitorCounterService) {
        this.visitorCounterService = visitorCounterService;
    }

    @GetMapping("/visitorCounter")
    public int getVisitorCounter() {
        return visitorCounterService.getCurrentValue();
    }

    @PostMapping("/visitorCounter/increment")
    public ResponseEntity<Integer> incrementVisitorCounter() {
        int newValue = visitorCounterService.incrementVisitorCounter();
        System.out.println("Incremented Visitor Counter: " + newValue); // デバッグ用
        return ResponseEntity.ok(newValue);
    }
}