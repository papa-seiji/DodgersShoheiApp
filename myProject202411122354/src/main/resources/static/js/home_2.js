document.addEventListener("DOMContentLoaded", function () {
    const menuToggle = document.getElementById("menu-toggle");
    const sideMenu = document.getElementById("side-menu");
    const menuClose = document.getElementById("menu-close");
    const overlay = document.getElementById("overlay");
    let isMenuOpen = false;

    function toggleMenu() {
        isMenuOpen = !isMenuOpen;
        sideMenu.style.left = isMenuOpen ? "0" : "-250px";
        overlay.style.display = isMenuOpen ? "block" : "none";
        menuToggle.textContent = isMenuOpen ? "×" : "☰";
    }

    menuToggle.addEventListener("click", toggleMenu);
    menuClose.addEventListener("click", toggleMenu);
    overlay.addEventListener("click", toggleMenu);

    // WebSocketで訪問者カウンターをリアルタイム更新
    const counters = {
        VisitorCounter: document.getElementById("visitor-counter-value"),
    };

    let stompClient = null;

    function initializeWebSocket() {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function () {
            console.log("WebSocket connected");
            stompClient.subscribe('/topic/visitorCounter', function (message) {
                const newValue = JSON.parse(message.body);
                counters.VisitorCounter.textContent = newValue;
            });
        });
    }

    // 初期訪問者カウンターの取得
    function fetchVisitorCounter() {
        fetch('/api/visitorCounter')
            .then(response => response.json())
            .then(data => { counters.VisitorCounter.textContent = data; })
            .catch(error => console.error('Error fetching visitor counter:', error));
    }

    fetchVisitorCounter();
    initializeWebSocket();
});
