package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.Subscription;
import com.example.dodgersshoheiapp.repository.SubscriptionRepository;
import com.example.dodgersshoheiapp.service.PushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PushNotificationService pushNotificationService;

    // サブスクリプションデータの取得
    @GetMapping("/subscriptions")
    public ResponseEntity<List<Subscription>> getSubscriptions() {
        return ResponseEntity.ok(subscriptionRepository.findAll());
    }

    // 通知を送信
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody String message) {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        for (Subscription subscription : subscriptions) {
            try {
                pushNotificationService.sendPushNotification(subscription, message);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("Failed to send notification: " + e.getMessage());
            }
        }
        return ResponseEntity.ok("Notification sent to all subscribers.");
    }
}
