document.addEventListener("DOMContentLoaded", async () => {
    console.log("📢 updateOtaniBavgChart.js ロード完了");

    const yosouType = "OTANI_BAVG";  // ✅ 大谷翔平 打率予想
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

    // ✅ 打率予想の選択肢
    const bavgOptions = [
        "1割台", "2割台", "3割台", "4割以上"
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
            console.log("📊 打率予想データ取得成功:", data);

            // ✅ 現在のユーザーの投票を取得
            currentVote = data.find(vote => vote.votedBy === currentUser) || null;
            console.log("✅ 現在の投票データ:", currentVote);

            // ✅ モーダルに既存の投票情報を表示
            const voteMessage = document.getElementById("vote-message-bavg");
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
    
        const ctx = document.getElementById("chart-otani-bavg").getContext("2d");
    
        if (chartInstance) {
            chartInstance.destroy();
        }
    
        chartInstance = new Chart(ctx, {
            type: "pie",  // ✅ 3D円グラフ
            data: {
                labels: labels,
                datasets: [
                    {
                        data: values,
                        backgroundColor: [
                            "rgba(255, 99, 132, 0.8)",
                            "rgba(54, 162, 235, 0.8)",
                            "rgba(255, 206, 86, 0.8)",
                            "rgba(75, 192, 192, 0.8)",
                            "rgba(153, 102, 255, 0.8)"
                        ],
                        borderColor: "#222",
                        borderWidth: 1,
                    },
                ],
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: "top", // 位置（"top" もOK）
                        align: "center",
                        labels: {
                            boxWidth: 5, // ✅ 色付きのボックスを小さく
                            padding: 5, // ✅ ラベルの余白を狭く
                            color: "white",
                            font: {
                                size: 11 // ✅ フォントを小さく
                            }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function (tooltipItem) {
                                let sum = values.reduce((a, b) => a + b, 0);
                                let percentage = (tooltipItem.raw * 100 / sum).toFixed(1) + "%";
                                return tooltipItem.label + ": " + percentage;
                            }
                        }
                    }
                }
            },
        });
    }
    
    // ✅ モーダル処理
    const modal = document.getElementById("vote-modal-bavg");
    const modalSelect = document.getElementById("bavg-select");
    const voteButton = document.getElementById("vote-button-bavg");

    function openModal() {
        modal.style.display = "block";

        if (currentVote) {
            document.getElementById("vote-message-bavg").innerText = `現在の投票: ${currentVote.yosouValue}`;
        } else {
            document.getElementById("vote-message-bavg").innerText = "未投票";
        }
    }

    function closeModal() {
        modal.style.display = "none";
    }

    document.getElementById("otani-bavg").addEventListener("click", openModal);
    document.getElementById("close-modal-bavg").addEventListener("click", closeModal);

    // ✅ 追加: モーダルの外側をクリックしたら閉じる処理
    modal.addEventListener("click", (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });

    voteButton.addEventListener("click", () => {
        const selectedValue = modalSelect.value;
        if (selectedValue) {
            sendVote(selectedValue);
        }
    });

    await fetchCurrentUser();
    fetchYosouData();
});
