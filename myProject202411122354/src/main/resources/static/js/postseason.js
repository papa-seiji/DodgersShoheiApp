document.addEventListener("DOMContentLoaded", async () => {
    try {
        const res = await fetch("/api/mlb/series-results");
        const data = await res.json();

        // シリーズ結果を画面に反映（Reds vs Dodgers / Cubs vs Padres）
        for (const key in data) {
            const el = document.getElementById(key);
            if (el) {
                el.textContent = data[key];
            }
        }

        // --- 動作確認ログ（開発時のみ）---
        console.log("シリーズデータを取得しました:", data);

    } catch (e) {
        console.error("Error fetching series results:", e);
    }
});
