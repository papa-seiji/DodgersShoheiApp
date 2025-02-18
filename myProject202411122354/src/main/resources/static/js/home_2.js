document.addEventListener("DOMContentLoaded", function () {
    const counters = {
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

    
    fetchVisitorCounter();        // VisitorCounterを取得して初期表示

    // WebSocketを初期化して購読
    initializeWebSocket();
});

        // ユーザー情報を取得してヘッダーに表示
        function fetchUserInfo() {
            fetch('/auth/userinfo')
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Failed to fetch user info');
                    }
                    return response.json();
                })
                .then(data => {
                    const userInfoElement = document.getElementById('user-info');
                    userInfoElement.textContent = `${data.number}番 ${data.position}_${data.username}さん`;
                })
                .catch(error => console.error('Error fetching user info:', error));
        }

