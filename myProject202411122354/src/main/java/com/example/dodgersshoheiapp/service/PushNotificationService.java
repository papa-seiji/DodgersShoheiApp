package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.Comment;
import com.example.dodgersshoheiapp.model.Subscription;
import com.example.dodgersshoheiapp.payload.NotificationPayload; // 必要に応じてインポート
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class PushNotificationService {

    private final String publicKey = "BP77hN9sATJT092ZSUUpgBBNPVh-nWJcTe2P66wTrH42wD2oBsN0LrcVQx_QxCrF_L7WQMczTi_86e6uQmqyibI";
    private final String privateKey = "vszYD36X9XZCuHQHWSQR-VsD_5toM4XodF-DOAWHUPI";

    private final PushService pushService;

    public PushNotificationService() throws GeneralSecurityException {
        this.pushService = new PushService();
        this.pushService.setPublicKey(publicKey);
        this.pushService.setPrivateKey(privateKey);
    }

    /**
     * 単一のサブスクリプションに対してプッシュ通知を送信
     *
     * @param subscription サブスクリプション情報
     * @param title        プッシュ通知のタイトル
     * @param body         プッシュ通知の本文
     */
    public void sendPushNotification(Subscription subscription, String title, String body) {
        try {
            String payload = "{\"title\":\"" + title + "\", \"body\":\"" + body + "\"}";
            Notification notification = new Notification(
                    subscription.getEndpoint(),
                    subscription.getP256dh(),
                    subscription.getAuth(),
                    payload);
            pushService.send(notification);
            System.out.println("Push notification sent to: " + subscription.getEndpoint());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(
                    "Failed to send push notification to " + subscription.getEndpoint() + ": " + e.getMessage());
        }
    }

    /**
     * 複数のサブスクリプションに対してプッシュ通知を送信
     *
     * @param subscriptions サブスクリプションのリスト
     * @param title         プッシュ通知のタイトル
     * @param body          プッシュ通知の本文
     */
    public void sendPushNotifications(List<Subscription> subscriptions, String title, String body) {
        for (Subscription subscription : subscriptions) {
            sendPushNotification(subscription, title, body);
        }
    }

    /**
     * コメントに基づいたプッシュ通知の送信
     *
     * @param subscriptions サブスクリプションのリスト
     * @param comment       新しいコメント
     */
    public void sendNotification(List<Subscription> subscriptions, Comment comment) {
        String title = "新しいコメント";
        String body = "ユーザ " + comment.getUsername() + " がコメントしました: " + comment.getContent();
        sendPushNotifications(subscriptions, title, body);
    }
}
