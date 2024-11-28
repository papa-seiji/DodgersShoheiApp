document.addEventListener("DOMContentLoaded", function () {
    const counters = {
        MainCounter: document.getElementById("main-counter-value"),
        SecondaryCounter: document.getElementById("secondary-counter-value"),
        VisitorCounter: document.getElementById("visitor-counter-value"), // VisitorCounterを追加
    };

    // WebSocket初期化
    let stompClient = null;

    function initializeWebSocket() {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

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
    fetchCounterValues();         // MainCounterとSecondaryCounterを取得
    fetchVisitorCounter();        // VisitorCounterを取得して初期表示

    // WebSocketを初期化して購読
    initializeWebSocket();
});
