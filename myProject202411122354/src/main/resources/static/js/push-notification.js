const vapidPublicKey = 'BP77hN9sATJT092ZSUUpgBBNPVh-nWJcTe2P66wTrH42wD2oBsN0LrcVQx_QxCrF_L7WQMczTi_86e6uQmqyibI';

// Base64をUint8Arrayに変換する関数
function urlBase64ToUint8Array(base64String) {
    const padding = '='.repeat((4 - base64String.length % 4) % 4);
    const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
    const rawData = window.atob(base64);
    return new Uint8Array([...rawData].map((char) => char.charCodeAt(0)));
}

// サービスワーカーの登録
if ('serviceWorker' in navigator && 'PushManager' in window) {
    navigator.serviceWorker.register('/sw.js').then((registration) => {
        console.log('Service Worker successfully registered:', registration);

        // サブスクリプションの確認
        registration.pushManager.getSubscription().then((subscription) => {
            if (subscription) {
                console.log('Existing subscription found. Unsubscribing...');
                // 既存のサブスクリプションを解除して再登録
                subscription.unsubscribe().then(() => {
                    console.log('Existing subscription unsubscribed.');
                    subscribeUserToPush(registration); // 新しいサブスクリプションの登録
                }).catch((error) => {
                    console.error('Error unsubscribing from existing subscription:', error);
                    console.warn('Continuing with new subscription registration...');
                    subscribeUserToPush(registration);
                });
            } else {
                console.log('No existing subscription found. Subscribing user...');
                subscribeUserToPush(registration); // 新しいサブスクリプションの登録
            }
        }).catch((error) => {
            console.error('Failed to retrieve subscription:', error);
        });
    }).catch((error) => {
        console.error('Service Worker registration failed:', error);
    });
} else {
    console.error('Service Worker or PushManager not supported in this browser.');
}

// 新しいサブスクリプションを登録
function subscribeUserToPush(registration) {
    registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(vapidPublicKey)
    }).then((subscription) => {
        console.log('New subscription created:', subscription);

        // サーバーにサブスクリプション情報を送信
        fetch('/notifications/subscribe', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(subscription)
        }).then((response) => {
            if (response.ok) {
                console.log('Subscription successfully sent to server.');
            } else {
                console.error('Failed to send subscription to server. Response status:', response.status);
            }
        }).catch((error) => {
            console.error('Error sending subscription to server:', error);
        });
    }).catch((error) => {
        console.error('Failed to subscribe user to push notifications:', error);
    });
}
