package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.Counter;
import com.example.dodgersshoheiapp.service.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/counter")
public class CounterController {

    @Autowired
    private CounterService counterService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Counter情報取得
    @GetMapping("/{counterName}")
    public ResponseEntity<Counter> getCounter(@PathVariable String counterName) {
        Counter counter = counterService.getCounterByName(counterName);
        return counter != null ? ResponseEntity.ok(counter) : ResponseEntity.notFound().build();
    }

    // Counter値更新
    @PostMapping("/update")
    public ResponseEntity<?> updateCounter(@RequestParam String counterName, @RequestParam int increment) {
        try {
            Counter updatedCounter = counterService.updateCounterValue(counterName, increment);

            // WebSocketで通知
            messagingTemplate.convertAndSend("/topic/" + counterName, updatedCounter);

            return ResponseEntity.ok(updatedCounter);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}
