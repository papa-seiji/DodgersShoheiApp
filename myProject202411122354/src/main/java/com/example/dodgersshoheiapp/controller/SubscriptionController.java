package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.model.Subscription;
import com.example.dodgersshoheiapp.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class SubscriptionController {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody Map<String, Object> subscriptionData) {
        try {
            // サブスクリプションデータの抽出
            String endpoint = (String) subscriptionData.get("endpoint");
            Map<String, String> keys = (Map<String, String>) subscriptionData.get("keys");
            String p256dh = keys.get("p256dh");
            String auth = keys.get("auth");

            // サブスクリプションエンティティの作成
            Subscription subscription = new Subscription();
            subscription.setEndpoint(endpoint);
            subscription.setP256dh(p256dh);
            subscription.setAuth(auth);

            // データベースに保存
            subscriptionRepository.save(subscription);

            return ResponseEntity.ok("Subscription saved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to save subscription");
        }
    }
}
