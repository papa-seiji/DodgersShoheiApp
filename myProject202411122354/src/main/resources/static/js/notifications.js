// notifications.js
document.addEventListener('DOMContentLoaded', async () => {
    if ('Notification' in window) {
        const permission = await Notification.requestPermission();
        if (permission === 'granted') {
            console.log('通知が許可されました');
            // サービスワーカーの登録
            if ('serviceWorker' in navigator) {
                navigator.serviceWorker.register('/sw.js')
                    .then(registration => {
                        console.log('Service Worker registered:', registration);
                        registration.pushManager.subscribe({
                            userVisibleOnly: true,
                            applicationServerKey: 'BP77hN9sATJT092ZSUUpgBBNPVh-nWJcTe2P66wTrH42wD2oBsN0LrcVQx_QxCrF_L7WQMczTi_86e6uQmqyibI'
                        }).then(subscription => {
                            console.log('Push subscription:', subscription);
                            // サーバーにサブスクリプションを送信
                            fetch('/subscriptions', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(subscription)
                            });
                        });
                    }).catch(error => {
                        console.error('Service Worker registration failed:', error);
                    });
            }
        } else if (permission === 'denied') {
            console.log('通知が拒否されました');
        } else {
            console.log('通知の権限が未決定です');
        }
    } else {
        console.warn('このブラウザは通知をサポートしていません');
    }
});
