document.addEventListener("DOMContentLoaded", function () {
    const modal = document.getElementById("dodgers-wins-modal");
    const openModalBtn = document.getElementById("open-dodgers-wins-modal");
    const closeModal = document.getElementById("close-dodgers-wins-modal");
    const confirmBtn = document.getElementById("confirm-dodgers-wins");
    const winsSelect = document.getElementById("dodgers-wins-select");

    let winsCounts = {};
    
    // 🎯 WebSocket接続
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        console.log("✅ Dodgers 勝利数予想 - WebSocket 接続完了");

        // 🎯 WebSocketで投票データを受信
        stompClient.subscribe("/topic/dodgers-wins", function (message) {
            const newVotes = JSON.parse(message.body);

            Object.keys(newVotes).forEach(winRange => {
                winsCounts[winRange] = (winsCounts[winRange] || 0) + newVotes[winRange];
            });

            updateChart();
        });

        // 🎯 サーバーから既存の投票データを取得
        fetch("/api/dodgers-wins")
            .then(response => response.json())
            .then(predictions => {
                predictions.forEach(pred => {
                    winsCounts[pred.winRange] = (winsCounts[pred.winRange] || 0) + 1;
                });

                updateChart();
            })
            .catch(error => console.error("APIエラー:", error));
    });

    openModalBtn.addEventListener("click", function () {
        modal.style.display = "block";
    });

    closeModal.addEventListener("click", function () {
        modal.style.display = "none";
    });

    confirmBtn.addEventListener("click", function () {
        const selectedWins = winsSelect.value;

        if (!selectedWins) {
            alert("勝利数を選択してください！");
            return;
        }

        // 🎯 WebSocket & APIでサーバーに投票データを送信
        const voteData = { "wins": selectedWins };

        stompClient.send("/app/dodgers-wins-vote", {}, JSON.stringify(voteData));

        fetch("/api/dodgers-wins", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                predictionType: "DODGERS_WINS",  // ✅ 追加
                winRange: selectedWins,
                userId: "guest",
                sessionId: sessionStorage.getItem("sessionId") || "anonymous"
            })
        })
        .then(response => response.json())
        .then(data => console.log("APIレスポンス:", data))
        .catch(error => console.error("APIエラー:", error));

        modal.style.display = "none";
    });

// 🎯 勝利数円グラフを更新
function updateChart() {
    const ctx = document.getElementById("dodgers-wins-chart").getContext("2d");

    if (window.dodgersWinsChart) {
        window.dodgersWinsChart.destroy();
    }

    const winRanges = ["50", "60", "70", "80", "90", "100"];
    const colors = ["blue", "green", "orange", "red", "gold", "purple"];
    const winVotes = winRanges.map(range => winsCounts[range] || 0);
    const totalVotes = winVotes.reduce((sum, value) => sum + value, 0);

    window.dodgersWinsChart = new Chart(ctx, {
        type: "pie",
        data: {
            labels: ["50W台", "60W台", "70W台", "80W台", "90W台", "100W以上"],
            datasets: [{
                data: winVotes,
                backgroundColor: colors,
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { 
                    position: "bottom",
                    labels: {
                        color: "black", // 🎯 ✅ 凡例のフォントを黒に設定
                        font: {
                            size: 9, // 🎯 少し大きめに調整
                            weight: "bold"
                        }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (tooltipItem) {
                            const value = winVotes[tooltipItem.dataIndex];
                            const percentage = totalVotes > 0 ? ((value / totalVotes) * 100).toFixed(1) + "%" : "0%";
                            return ` ${tooltipItem.label}: ${percentage}`;
                        }
                    }
                },
                datalabels: {
                    color: "black", // 🎯 ✅ グラフ内の%表示のフォントを黒に変更
                    font: {
                        weight: "bold",
                        size: 14
                    },
                    formatter: function (value, context) {
                        const percentage = totalVotes > 0 ? ((value / totalVotes) * 100).toFixed(1) + "%" : "";
                        return percentage;
                    }
                }
            }
        },
        plugins: [ChartDataLabels] // 🎯 データラベルを適用
    });
}

    // 🎯 カスタム凡例を作成
    const legendContainer = document.getElementById("custom-legend");
    legendContainer.innerHTML = winRanges.map((range, index) =>
        `<div style="display: flex; align-items: center; font-size: 10px; margin: 2px 0;">
            <span style="width: 10px; height: 10px; background-color: ${colors[index]}; display: inline-block; margin-right: 5px;"></span>
            ${range}W台
        </div>`
    ).join("");
}
);
