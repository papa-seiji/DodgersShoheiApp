document.addEventListener("DOMContentLoaded", async () => {
    console.log("📢 yosou_page.js ロード完了");

    const yosouType = "NL_WEST_yuusho";
    let chartInstance = null;
    let currentUser = "ゲスト"; // ✅ デフォルト値

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

    async function fetchYosouData() {
        try {
            const response = await fetch(`/api/yosou/data?yosouType=${encodeURIComponent(yosouType)}`);
            if (!response.ok) throw new Error("データ取得エラー: " + response.status);
            const data = await response.json();
            console.log("📊 予想データ取得成功:", data);

            // ✅ 横棒グラフを更新
            updateChart(data);
        } catch (error) {
            console.error("❌ データ取得エラー:", error);
        }
    }

    async function sendVote(yosouValue) {
        try {
            const response = await fetch("/api/yosou/vote", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ yosouType, yosouValue, votedBy: currentUser }) // ✅ ユーザー名を送信
            });
            if (!response.ok) throw new Error("投票エラー: " + response.status);
            console.log("✅ 投票成功");
            closeModal(); // ✅ 投票後にモーダルを閉じる
            fetchYosouData(); // ✅ 投票後にグラフ更新
        } catch (error) {
            console.error("❌ 投票エラー:", error);
        }
    }

    // ✅ チームごとに色を設定
    const teamColors = {
        "ドジャース": "rgba(0, 85, 165, 0.8)",   // ドジャーブルー
        "パドレス": "rgba(189, 155, 96, 0.8)",  // パドレスブラウン
        "Dバックス": "rgba(167, 25, 48, 0.8)",  // Dバックスレッド
        "ジャイアンツ": "rgba(235, 97, 35, 0.8)", // ジャイアンツオレンジ
        "ロッキーズ": "rgba(70, 70, 150, 0.8)" // ロッキーズパープル
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
                        position: "top"
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        max: Math.max(...values) + 2,
                        ticks: { font: { size: 14 } }
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

    await fetchCurrentUser(); // ✅ ユーザー情報を先に取得
    fetchYosouData();
});
