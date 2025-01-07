self.addEventListener('push', function(event) {
    console.log('Pushイベントがトリガーされました:', event);

    let data = {};
    try {
        // データをパース
        data = event.data ? event.data.json() : {};
        console.log('Pushデータ:', data);
    } catch (error) {
        console.error('Pushデータのパースに失敗しました:', error);
    }

    const title = data.title || '新しい通知';
    const options = {
        body: data.body || '詳細がありません。',
        icon: data.icon || '/icon.png',
        badge: data.badge || '/badge.png', // 小さいアイコン（Android向け）
        actions: data.actions || [], // アクションボタン
        data: data.url || '/' // 通知クリック時のURL
    };

    event.waitUntil(self.registration.showNotification(title, options));
});

// 通知クリック時のイベントを処理
self.addEventListener('notificationclick', function(event) {
    console.log('通知がクリックされました:', event.notification);

    const url = event.notification.data || '/';
    event.notification.close(); // 通知を閉じる

    event.waitUntil(
        clients.matchAll({ type: 'window', includeUncontrolled: true }).then(windowClients => {
            // 既存のウィンドウがある場合はフォーカスする
            for (const client of windowClients) {
                if (client.url === url && 'focus' in client) {
                    return client.focus();
                }
            }
            // 既存のウィンドウがない場合は新しいタブを開く
            if (clients.openWindow) {
                return clients.openWindow(url);
            }
        })
    );
});
