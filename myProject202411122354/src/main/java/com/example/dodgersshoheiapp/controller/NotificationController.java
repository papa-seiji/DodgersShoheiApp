package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.Subscription;
import com.example.dodgersshoheiapp.repository.SubscriptionRepository;
import com.example.dodgersshoheiapp.service.PushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PushNotificationService pushNotificationService;

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody String message) {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        for (Subscription subscription : subscriptions) {
            try {
                pushNotificationService.sendPushNotification(subscription, "新しい通知", message);
            } catch (Exception e) {
                System.err.println("プッシュ通知の送信に失敗しました: " + e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body("通知を送信しました");
    }
}
