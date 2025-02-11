document.addEventListener("DOMContentLoaded", async () => {
    console.log("📢 yosou_page.js ロード完了");

    const yosouType = "NL_WEST_yuusho";
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
            console.log("📊 予想データ取得成功:", data);

            // ✅ 現在のユーザーの投票を取得
            currentVote = data.find(vote => vote.votedBy === currentUser) || null;
            console.log("✅ 現在の投票データ:", currentVote);

            // ✅ モーダルに既存の投票情報を表示
            const voteMessage = document.getElementById("vote-message");
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

    // ✅ チームごとに色を設定
    const teamColors = {
        "ドジャース": "rgba(0, 85, 165, 0.8)",
        "パドレス": "rgba(189, 155, 96, 0.8)",
        "Dバックス": "rgba(167, 25, 48, 0.8)",
        "ジャイアンツ": "rgba(235, 97, 35, 0.8)",
        "ロッキーズ": "rgba(70, 70, 150, 0.8)"
    };

    function updateChart(data) {
        const counts = {};
        data.forEach(item => {
            counts[item.yosouValue] = (counts[item.yosouValue] || 0) + 1;
        });

        const labels = Object.keys(counts);
        const values = Object.values(counts);
        const colors = labels.map(label => teamColors[label] || "rgba(54, 162, 235, 0.6)");

        const ctx = document.getElementById("chart-nl-west").getContext("2d");

        if (chartInstance) {
            chartInstance.destroy();
        }

        chartInstance = new Chart(ctx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [{
                    label: "投票数",
                    data: values,
                    backgroundColor: colors,
                    borderColor: colors.map(c => c.replace("0.8", "1")),
                    borderWidth: 1,
                    barThickness: 40
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                plugins: {
                    legend: {
                        display: true,
                        position: "top",
                        labels: {
                            color: "white" // ✅ フォントカラーを白に
                        }
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        max: Math.max(...values) + 2,
                        ticks: {
                            font: { size: 14 },
                            color: "white" // ✅ X軸ラベルを白に
                        },
                        grid: {
                            color: "rgba(255, 255, 255, 0.2)" // ✅ X軸のグリッド線を薄い白に
                        }
                    },
                    y: {
                        ticks: {
                            font: { size: 14 },
                            color: "white" // ✅ Y軸ラベルを白に
                        },
                        grid: {
                            color: "rgba(255, 255, 255, 0.2)" // ✅ Y軸のグリッド線を薄い白に
                        }
                    }
                }
            }
        });
    }

    // ✅ モーダル処理
    const modal = document.getElementById("vote-modal");
    const modalSelect = document.getElementById("team-select");
    const voteButton = document.getElementById("vote-button");

    function openModal() {
        modal.style.display = "block";
        document.getElementById("vote-message").innerText = currentVote ? `現在の投票: ${currentVote.yosouValue}` : "未投票";
    }

    function closeModal() {
        modal.style.display = "none";
    }

    document.getElementById("nl-west").addEventListener("click", openModal);
    document.getElementById("close-modal").addEventListener("click", closeModal);

    voteButton.addEventListener("click", () => {
        const selectedTeam = modalSelect.value;
        if (selectedTeam) {
            sendVote(selectedTeam);
        }
    });

    await fetchCurrentUser();
    fetchYosouData();
});
