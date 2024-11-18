package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.Counter;
import com.example.dodgersshoheiapp.service.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/counter")
public class CounterController {

    @Autowired
    private CounterService counterService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public Counter getCounter() {
        return counterService.getCounter();
    }

    @PostMapping("/update")
    public Counter updateCounter(@RequestParam int increment) {
        Counter updatedCounter = counterService.updateCounterValue(increment);
        messagingTemplate.convertAndSend("/topic/counter", updatedCounter); // カウンターの更新を通知
        return updatedCounter;
    }
}
