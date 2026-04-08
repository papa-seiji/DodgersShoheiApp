// 🔥 初期ロードでは何もしない（超重要）
document.addEventListener('DOMContentLoaded', () => {
    console.log('Push初期化はユーザー操作待ち');
});

// 🔥 ユーザー操作時に初期化（1回だけ）
document.addEventListener('click', async () => {

    // 2重実行防止
    if (window.__pushInitDone) return;
    window.__pushInitDone = true;

    if ('Notification' in window) {
        try {
            const permission = await Notification.requestPermission();

            if (permission === 'granted') {
                console.log('通知が許可されました');

                if ('serviceWorker' in navigator) {
                    try {
                        const registration = await navigator.serviceWorker.register('/sw.js');
                        console.log('Service Worker registered:', registration);

                        const subscription = await registerPushSubscription(registration);
                        if (subscription) {
                            await sendSubscriptionToServer(subscription);
                        }

                    } catch (error) {
                        console.error('Service Worker registration or subscription failed:', error);
                    }
                }

            } else if (permission === 'denied') {
                console.log('通知が拒否されました');
            } else {
                console.log('通知の権限が未決定です');
            }

        } catch (error) {
            console.error('通知の権限リクエスト中にエラーが発生しました:', error);
        }

    } else {
        console.warn('このブラウザは通知をサポートしていません');
    }

}, { once: true });


// Push Subscriptionを登録
async function registerPushSubscription(registration) {
    try {
        const applicationServerKey = urlBase64ToUint8Array(
            'BP77hN9sATJT092ZSUUpgBBNPVh-nWJcTe2P66wTrH42wD2oBsN0LrcVQx_QxCrF_L7WQMczTi_86e6uQmqyibI'
        );
        const subscription = await registration.pushManager.subscribe({
            userVisibleOnly: true,
            applicationServerKey: applicationServerKey
        });
        console.log('Push subscription successfully created:', subscription);
        return subscription;
    } catch (error) {
        console.error('Failed to create Push subscription:', error);
        return null;
    }
}

// サブスクリプション情報をサーバーに送信
async function sendSubscriptionToServer(subscription) {
    try {
        const response = await fetch('/notifications/subscribe', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(subscription),
            credentials: 'include'
        });

        if (response.ok) {
            console.log('Subscription successfully sent to server.');
        } else {
            console.error('Failed to send subscription to server. Response status:', response.status);
        }
    } catch (error) {
        console.error('Error sending subscription to server:', error);
    }
}

// Base64変換
function urlBase64ToUint8Array(base64String) {
    const padding = '='.repeat((4 - base64String.length % 4) % 4);
    const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
    const rawData = window.atob(base64);
    return new Uint8Array([...rawData].map((char) => char.charCodeAt(0)));
}