document.addEventListener("DOMContentLoaded", async () => {
    console.log("📢 yosou_page.js ロード完了");

    // ✅ ユーザーログイン確認
    let username;
    try {
        const response = await fetch("/auth/user", { method: "GET", credentials: "include" });
        if (!response.ok) throw new Error("未認証");
        const userData = await response.json();
        username = userData.username;
        sessionStorage.setItem("username", username);
    } catch (error) {
        console.warn("❌ ログインしていません:", error);
        alert("ログインしてください");
        window.location.href = "/auth/login";
        return;
    }

    console.log("👤 現在のユーザー:", username);

    // ✅ WebSocket 設定
    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        console.log("✅ WebSocket 接続完了");

        // 🎯 予想データのリアルタイム更新
        stompClient.subscribe("/topic/yosou", (message) => {
            const data = JSON.parse(message.body);
            updateChart(data);
        });

        // 🎯 初回データ取得（NL西1位）
        fetchYosouData("NL_WEST_1位");
    });

    // ✅ モーダル要素の取得
    const modal = document.getElementById("nl-west-modal");
    const openButton = document.getElementById("nl-west");
    const closeButton = document.getElementById("close-nl-west");

    if (!modal || !openButton || !closeButton) {
        console.error("❌ モーダル要素が見つかりません！");
        return;
    }

    // ✅ モーダルの開閉処理
    function openModal() {
        console.log("📢 モーダルを開く");
        modal.style.display = "block";
    }

    function closeModal() {
        console.log("📢 モーダルを閉じる");
        modal.style.display = "none";
    }

    // 🎯 `onclick="openModal()"` のエラー対策
    window.openModal = openModal;
    window.closeModal = closeModal;

    openButton.addEventListener("click", openModal);
    closeButton.addEventListener("click", closeModal);

    window.addEventListener("click", (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });

    // ✅ 投票ボタンのクリックイベント
    document.getElementById("confirm-nl-west").addEventListener("click", () => {
        const selectedTeam1 = document.getElementById("team-select-1").value;
        const selectedTeam2 = document.getElementById("team-select-2").value;

        if (!selectedTeam1 && !selectedTeam2) {
            alert("チームを選択してください");
            return;
        }

        if (selectedTeam1) sendVote("NL_WEST_1位", selectedTeam1, username);
        if (selectedTeam2) sendVote("NL_WEST_1位", selectedTeam2, username);

        closeModal(); // 🎯 投票後モーダルを閉じる
    });

    // ✅ 投票データを送信
    function sendVote(yosouType, yosouValue, votedBy) {
        console.log(`📢 ${yosouType} に ${yosouValue} を投票 by ${votedBy}`);

        stompClient.send("/app/vote", {}, JSON.stringify({ yosouType, yosouValue, votedBy }));

        fetch("/api/yosou/vote", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ yosouType, yosouValue, votedBy })
        })
        .then(response => {
            if (!response.ok) throw new Error("サーバーエラー: " + response.status);
            return response.text();  // 文字列で処理し、エラーを防ぐ
        })
        .then(data => {
            console.log("✅ 投票成功:", data);
            fetchYosouData(yosouType);
        })
        .catch(error => console.error("❌ 投票エラー:", error));
    }

    // ✅ 予想データを取得
    function fetchYosouData(yosouType) {
        fetch(`/api/yosou/${encodeURIComponent(yosouType)}`)
            .then(response => {
                if (!response.ok) throw new Error("サーバーエラー: " + response.status);
                return response.json();
            })
            .then(data => updateChart(data))
            .catch(error => console.error("❌ データ取得エラー:", error));
    }

    // ✅ グラフの更新
    function updateChart(yosouData) {
        if (!yosouData || !Array.isArray(yosouData)) {
            console.warn("⚠ グラフ更新エラー: yosouData が無効");
            return;
        }

        const ctx = document.getElementById("nl-west-chart")?.getContext("2d");

        if (!ctx) {
            console.error("❌ Chart のコンテキスト取得失敗");
            return;
        }

        if (window.nlWestChart) {
            window.nlWestChart.destroy();
        }

        const labels = yosouData.map(item => item.yosouValue);
        const counts = yosouData.map(item => item.votedBy.split(",").length);

        window.nlWestChart = new Chart(ctx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [{
                    data: counts,
                    backgroundColor: ["blue", "yellow", "red", "orange", "purple"],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: { beginAtZero: true }
                }
            }
        });

        console.log("✅ グラフ更新完了");
    }
});
