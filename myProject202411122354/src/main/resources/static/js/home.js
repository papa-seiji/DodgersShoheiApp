document.addEventListener("DOMContentLoaded", function () {
    const counters = {
        MainCounter: document.getElementById("main-counter-value"),
        SecondaryCounter: document.getElementById("secondary-counter-value"),
        VisitorCounter: document.getElementById("visitor-counter-value"), // VisitorCounterを追加
    };

    // WebSocket初期化
    // ❌ 今
    // let stompClient = null;

    // ✅ 修正
    window.stompClient = null;

    function initializeWebSocket() {
        const socket = new SockJS('/ws');
        // ❌ 今
        // stompClient = Stomp.over(socket);

        // ✅ 修正
        window.stompClient = Stomp.over(socket);

        stompClient.connect({}, function () {
            console.log("WebSocket connected");

            // VisitorCounterのリアルタイム更新を購読
            stompClient.subscribe('/topic/visitorCounter', function (message) {
                const newValue = JSON.parse(message.body);
                counters.VisitorCounter.textContent = newValue;
            });
        });
    }

    // カウンター値をサーバーから取得
    function fetchCounterValues() {
        Object.keys(counters).forEach(counterName => {
            if (counterName !== "VisitorCounter") { // VisitorCounterは個別に取得
                fetch(`/counter/${counterName}`)
                    .then(response => {
                        if (!response.ok) {
                            throw new Error(`Failed to fetch ${counterName} value`);
                        }
                        return response.json();
                    })
                    .then(data => {
                        counters[counterName].textContent = data.value;
                    })
                    .catch(error => console.error(`Error fetching ${counterName}:`, error));
            }
        });
    }

    // VisitorCounterを取得して表示
    function fetchVisitorCounter() {
        fetch('/api/visitorCounter')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch visitor counter');
                }
                return response.json();
            })
            .then(data => {
                counters.VisitorCounter.textContent = data; // 初期値を更新
            })
            .catch(error => console.error('Error fetching visitor counter:', error));
    }

    // VisitorCounterをインクリメント
    function incrementVisitorCounter() {
        fetch('/api/visitorCounter/increment', { method: 'POST' })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to increment visitor counter');
                }
                return response.json();
            })
            .then(data => {
                counters.VisitorCounter.textContent = data; // 値を更新
                // WebSocketで通知 (サーバー側が行うため、ここは省略可能)
            })
            .catch(error => console.error('Error incrementing visitor counter:', error));
    }

    
    // Event Listeners
    if (document.getElementById("main-increment-button")) {
        document.getElementById("main-increment-button").addEventListener("click", () => updateCounter("MainCounter", 1));
    }
    if (document.getElementById("main-decrement-button")) {
        document.getElementById("main-decrement-button").addEventListener("click", () => updateCounter("MainCounter", -1));
    }
    if (document.getElementById("secondary-increment-button")) {
        document.getElementById("secondary-increment-button").addEventListener("click", () => updateCounter("SecondaryCounter", 1));
    }
    if (document.getElementById("secondary-decrement-button")) {
        document.getElementById("secondary-decrement-button").addEventListener("click", () => updateCounter("SecondaryCounter", -1));
    }

    // 初期化
    // fetchCounterValues();         // MainCounterとSecondaryCounterを取得
    // fetchVisitorCounter();        // VisitorCounterを取得して初期表示
    //✅ 修正
    setTimeout(fetchCounterValues, 1000);
    setTimeout(fetchVisitorCounter, 1200);


    // WebSocketを初期化して購読
    // initializeWebSocket();

    // ❌ 削除 or コメントアウト
    // initializeWebSocket();

    // ✅ クリック後に起動
    // document.addEventListener("click", () => {
    //     if (!window.__wsStarted) {
    //         window.__wsStarted = true;
    //         initializeWebSocket();
    //         console.log("WebSocket遅延起動");
    //     }
    // }, { once: true });

        document.addEventListener("click", (e) => {

        // 🔥 WBCリンクなら何もしない
            if (e.target.closest("a[href='/WorldBaseballClassic']")) {
                return;
            }

            if (!window.__wsStarted) {
                window.__wsStarted = true;
                initializeWebSocket();
                console.log("WebSocket遅延起動");
            }

            }, { once: true });

});


