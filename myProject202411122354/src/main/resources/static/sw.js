self.addEventListener('push', function(event) {
    const data = event.data.json(); // サーバーからのペイロードをJSONとしてパース
    const title = data.title || "No title";
    const options = {
        body: data.body || "No body",
        icon: data.icon || "/default-icon.png"
    };
    event.waitUntil(self.registration.showNotification(title, options));
});
