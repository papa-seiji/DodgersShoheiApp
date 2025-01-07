package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.Subscription;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;

@Service
public class PushNotificationService {

    private final String publicKey = "BP77hN9sATJT092ZSUUpgBBNPVh-nWJcTe2P66wTrH42wD2oBsN0LrcVQx_QxCrF_L7WQMczTi_86e6uQmqyibI";
    private final String privateKey = "vszYD36X9XZCuHQHWSQR-VsD_5toM4XodF-DOAWHUPI";

    public void sendPushNotification(Subscription subscription, String payload) throws GeneralSecurityException {
        try {
            PushService pushService = new PushService();
            pushService.setPublicKey(publicKey);
            pushService.setPrivateKey(privateKey);

            Notification notification = new Notification(
                    subscription.getEndpoint(),
                    subscription.getP256dh(),
                    subscription.getAuth(),
                    payload);

            pushService.send(notification);
            System.out.println("Push notification sent to: " + subscription.getEndpoint());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send push notification: " + e.getMessage());
        }
    }
}
