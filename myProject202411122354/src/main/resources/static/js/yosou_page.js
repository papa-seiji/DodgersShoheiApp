document.addEventListener("DOMContentLoaded", () => {
    const socket = new SockJS("/ws");
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, () => {
        console.log("✅ WebSocket 接続完了");

        // 🎯 投票データのリアルタイム更新
        stompClient.subscribe("/topic/yosou", (message) => {
            const data = JSON.parse(message.body);
            updateChart(data);
        });

        // 🎯 初回データ取得（NL西1位）
        fetchYosouData("NL_WEST_1位");
    });

    // 🎯 投票ボタンのクリックイベント
    document.getElementById("confirm-nl-west").addEventListener("click", () => {
        const selectedTeam1 = document.getElementById("team-select-1").value;
        const selectedTeam2 = document.getElementById("team-select-2").value;
        const username = sessionStorage.getItem("username") || "anonymous";

        if (selectedTeam1) sendVote("NL_WEST_1位", selectedTeam1, username);
        if (selectedTeam2) sendVote("NL_WEST_1位", selectedTeam2, username);
    });

    // 🎯 投票データ送信（WebSocket & API）
    function sendVote(yosouType, yosouValue, votedBy) {
        stompClient.send("/app/vote", {}, JSON.stringify({ yosouType, yosouValue, votedBy }));

        fetch("/api/yosou/vote", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ yosouType, yosouValue, votedBy })
        })
        .then(response => response.json())
        .then(data => {
            console.log("投票完了:", data);
            fetchYosouData(yosouType);
        });
    }

    // 🎯 APIから予想データを取得
    function fetchYosouData(yosouType) {
        fetch(`/api/yosou/${yosouType}`)
            .then(response => response.json())
            .then(data => {
                console.log("取得データ:", data);
                updateChart(data);
            });
    }

    // 🎯 グラフの更新
    function updateChart(yosouData) {
        const ctx = document.getElementById("nl-west-chart").getContext("2d");

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
    }
});
