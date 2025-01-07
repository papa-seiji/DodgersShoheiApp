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
        console.log('Service Worker registered:', registration);

        // サブスクリプションの解除と再登録
        registration.pushManager.getSubscription().then((subscription) => {
            if (subscription) {
                // 既存のサブスクリプションを解除
                subscription.unsubscribe().then(() => {
                    console.log('Existing subscription unsubscribed.');
                    subscribeUserToPush(registration); // 新しいサブスクリプションの登録
                }).catch((error) => {
                    console.error('Failed to unsubscribe:', error);
                });
            } else {
                subscribeUserToPush(registration); // 新しいサブスクリプションの登録
            }
        });
    }).catch((error) => {
        console.error('Service Worker registration failed:', error);
    });
} else {
    console.error('Service Worker or PushManager not supported.');
}

// 新しいサブスクリプションを登録
function subscribeUserToPush(registration) {
    registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(vapidPublicKey)
    }).then((subscription) => {
        console.log('New subscription:', subscription);

        // サーバーにサブスクリプション情報を送信
        fetch('/notifications/subscribe', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(subscription)
        }).then((response) => {
            if (response.ok) {
                console.log('Subscription sent to server.');
            } else {
                console.error('Failed to send subscription.');
            }
        });
    }).catch((error) => {
        console.error('Failed to subscribe:', error);
    });
}
