document.addEventListener("DOMContentLoaded", async () => {
    console.log("📢 updateSasakiWinsChart.js ロード完了");

    const yosouType = "SASAKI_WINS";  // ✅ 佐々木朗希 投手勝ち星予想
    let chartInstance = null;
    let currentUser = "ゲスト";
    let currentVote = null;

    // ✅ WebSocket 接続
    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        console.log("🔗 WebSocket 接続成功");

        // ✅ 投票データをリアルタイム受信
        stompClient.subscribe("/topic/yosou", (message) => {
            console.log("📩 リアルタイム更新:", JSON.parse(message.body));
            fetchYosouData(); // ✅ グラフを更新
        });
    });

    // ✅ 投手勝ち星予想の選択肢
    const winsOptions = [
        "0-4勝", "5-9勝", "10-14勝", "15-19勝", "20勝以上"
    ];

    // ✅ 現在のログインユーザーを取得
    async function fetchCurrentUser() {
        try {
            const response = await fetch("/api/yosou/user");
            if (!response.ok) throw new Error("ユーザー情報取得エラー: " + response.status);
            const data = await response.json();
            currentUser = data.username;
            console.log("👤 現在のユーザー:", currentUser);
        } catch (error) {
            console.error("❌ ユーザー情報取得エラー:", error);
        }
    }

    // ✅ 予想データを取得
    async function fetchYosouData() {
        try {
            const response = await fetch(`/api/yosou/data?yosouType=${encodeURIComponent(yosouType)}`);
            if (!response.ok) throw new Error("データ取得エラー: " + response.status);
            const data = await response.json();
            console.log("📊 佐々木朗希 投手勝ち星予想データ取得成功:", data);

            // ✅ 現在のユーザーの投票を取得
            currentVote = data.find(vote => vote.votedBy === currentUser) || null;
            console.log("✅ 現在の投票データ:", currentVote);

            // ✅ モーダルに既存の投票情報を表示
            const voteMessage = document.getElementById("vote-message-sasaki");
            voteMessage.innerText = currentVote ? `現在の投票: ${currentVote.yosouValue}` : "未投票";

            // ✅ グラフを更新
            updateChart(data);
        } catch (error) {
            console.error("❌ データ取得エラー:", error);
        }
    }

    async function sendVote(yosouValue) {
        try {
            let confirmMessage = currentVote
                ? `現在「${currentVote.yosouValue}」に投票済です。\n「${yosouValue}」に変更しますか？`
                : `「${yosouValue}」で投票していいですか？`;

            if (!confirm(confirmMessage)) return;

            const voteData = { yosouType, yosouValue, votedBy: currentUser };

            // ✅ WebSocket 経由でサーバーに送信
            stompClient.send("/app/vote", {}, JSON.stringify(voteData));

            alert(`✅ 「${yosouValue}」に投票しました！`);

            closeModal();
        } catch (error) {
            alert("❌ 投票に失敗しました。");
            console.error("❌ 投票エラー:", error);
        }
    }

    function updateChart(data) {
        const counts = {};
        data.forEach(item => {
            counts[item.yosouValue] = (counts[item.yosouValue] || 0) + 1;
        });
    
        const labels = Object.keys(counts);
        const values = Object.values(counts);
        const cumulative = values.map((sum => value => sum += value)(0)); // 累積値
    
        const ctx = document.getElementById("chart-sasaki-wins").getContext("2d");
    
        if (chartInstance) {
            chartInstance.destroy();
        }
    
        chartInstance = new Chart(ctx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [
                    {
                        type: "bar",
                        label: "項目",
                        data: values,
                        backgroundColor: "rgb(255, 128, 0)",
                        barThickness: 9, // ✅ 棒の太さを固定
                        categoryPercentage: 0.5, // ✅ 各カテゴリの幅
                    },
                    {
                        type: "line",
                        label: "人数",
                        data: cumulative,
                        borderColor: "rgb(0, 255, 238)",
                        borderWidth: 1,
                        fill: false,
                        tension: 0.2
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        labels: { color: "white",
                            font: { size: 10 }, // ✅ フォントサイズを小さく
                            boxWidth: 10 // ✅ 色付き四角のサイズを小さく
                        }
                    },
                    datalabels: {
                        color: "white",
                        anchor: "end",
                        align: "end",
                        formatter: (value, ctx) => {
                            let sum = ctx.dataset.data.reduce((a, b) => a + b, 0);
                            let percentage = (value * 100 / sum).toFixed(1) + "%";
                            return percentage;
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            color: "white",
                            stepSize: 1 // ✅ y軸を1刻みにする
                        }
                    },
                    x: {
                        ticks: { color: "white" }
                    }
                }
            }
        });
    }


    // ✅ モーダル処理
    const modal = document.getElementById("vote-modal-sasaki");
    const modalSelect = document.getElementById("sasaki-select");
    const voteButton = document.getElementById("vote-button-sasaki");

    function openModal() {
        modal.style.display = "block";

        if (currentVote) {
            document.getElementById("vote-message-sasaki").innerText = `現在の投票: ${currentVote.yosouValue}`;
        } else {
            document.getElementById("vote-message-sasaki").innerText = "未投票";
        }
    }

    function closeModal() {
        modal.style.display = "none";
    }

    document.getElementById("sasaki-wins").addEventListener("click", openModal);
    document.getElementById("close-modal-sasaki").addEventListener("click", closeModal);

    voteButton.addEventListener("click", () => {
        const selectedValue = modalSelect.value;
        if (selectedValue) {
            sendVote(selectedValue);
        }
    });

    await fetchCurrentUser();
    fetchYosouData();
});
