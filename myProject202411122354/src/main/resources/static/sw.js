self.addEventListener('push', function(event) {
    console.log('Pushイベントがトリガーされました:', event);
    const data = event.data ? event.data.json() : {};
    console.log('Pushデータ:', data);

    const title = data.title || 'No title';
    const options = {
        body: data.body || 'No body',
        icon: data.icon || '/icon.png'
    };

    event.waitUntil(self.registration.showNotification(title, options));
});
