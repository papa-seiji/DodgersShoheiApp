// ====================📜 JavaScript（リアルタイム同期 & WebSocket対応）==================== //
document.addEventListener("DOMContentLoaded", function () {
    const modal = document.getElementById("nl-west-modal");
    const openModalBtn = document.getElementById("open-nl-west-modal");
    const closeModal = document.getElementById("close-nl-west-modal");
    const confirmBtn = document.getElementById("confirm-nl-west");
    const resultText = document.getElementById("nl-west-result");

    const teamSelect1 = document.getElementById("team-select-1");
    const teamSelect2 = document.getElementById("team-select-2");

    // 🎯 チームデータ
    const teams = [
        { id: "119", name: "ドジャース", color: "blue", logo: "https://www.mlbstatic.com/team-logos/119.svg" },
        { id: "135", name: "パドレス", color: "#FFCC00", logo: "https://www.mlbstatic.com/team-logos/135.svg" },
        { id: "109", name: "Dバックス", color: "#A71930", logo: "https://www.mlbstatic.com/team-logos/109.svg" },
        { id: "137", name: "ジャイアンツ", color: "orange", logo: "https://www.mlbstatic.com/team-logos/137.svg" },
        { id: "115", name: "ロッキーズ", color: "purple", logo: "https://www.mlbstatic.com/team-logos/115.svg" }
    ];

    let voteCounts = {};
    let selectedTeams = JSON.parse(localStorage.getItem("selectedTeams")) || [];

    // 🎯 WebSocket接続
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        console.log("✅ WebSocket 接続完了");

        // 🎯 WebSocketで投票データを受信
        stompClient.subscribe("/topic/nl-west", function (message) {
            const newVotes = JSON.parse(message.body);

            Object.keys(newVotes).forEach(teamId => {
                voteCounts[teamId] = (voteCounts[teamId] || 0) + newVotes[teamId];
            });

            updateChart();
            updatePredictionCard();
        });

        // 🎯 サーバーから既存の投票データを取得
        fetch("/api/predictions/NL_WEST_1ST")
            .then(response => response.json())
            .then(predictions => {
                predictions.forEach(pred => {
                    voteCounts[pred.teamId] = (voteCounts[pred.teamId] || 0) + 1;
                });

                updateChart();
            });

        if (selectedTeams.length > 0) {
            stompClient.send("/app/vote", {}, JSON.stringify({ "team1": selectedTeams[0], "team2": selectedTeams[1] }));
        }
    });

    openModalBtn.addEventListener("click", function () {
        modal.style.display = "block";
    });

    closeModal.addEventListener("click", function () {
        modal.style.display = "none";
    });

    confirmBtn.addEventListener("click", function () {
        selectedTeams = [teamSelect1.value, teamSelect2.value].filter(Boolean);

        if (selectedTeams.length === 0) {
            alert("少なくとも1チームを選択してください！");
            return;
        }

        if (selectedTeams.length > 2) {
            alert("最大2チームまで選択できます！");
            return;
        }

        localStorage.setItem("selectedTeams", JSON.stringify(selectedTeams));

        // 🎯 WebSocket & APIでサーバーに投票データを送信
        const voteData = { "team1": selectedTeams[0], "team2": selectedTeams[1] };

        stompClient.send("/app/vote", {}, JSON.stringify(voteData));

        fetch("/api/predictions", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                predictionType: "NL_WEST_1ST",
                userId: "guest", 
                sessionId: sessionStorage.getItem("sessionId") || "anonymous",
                teamId: selectedTeams[0],
                teamName: teams.find(t => t.id === selectedTeams[0]).name
            })
        });

        modal.style.display = "none";
        updatePredictionCard();
    });

    // 🎯 カスタムプラグイン（ロゴをラベルとして表示）
    const customLabelsPlugin = {
        id: 'customLabels',
        afterDatasetsDraw(chart) {
            const ctx = chart.ctx;
            chart.data.labels.forEach((_, i) => {
                const team = teams[i];
                if (team) {
                    const meta = chart.getDatasetMeta(0);
                    const y = meta.data[i].y;
                    const x = chart.chartArea.left - 10; // 🎯 左端調整

                    const img = new Image();
                    img.src = team.logo;
                    img.onload = function () {
                        ctx.drawImage(img, x, y - 9, 9, 18); // 🎯 ロゴサイズ調整
                    };
                }
            });
        }
    };

// 🎯 グラフを更新
function updateChart() {
    const ctx = document.getElementById("nl-west-chart").getContext("2d");

    if (window.nlWestChart) {
        window.nlWestChart.destroy();
    }

    const teamVotes = teams.map(team => voteCounts[team.id] || 0);

    window.nlWestChart = new Chart(ctx, {
        type: "bar",
        data: {
            labels: teams.map(team => team.name), // 🎯 チーム名を全表示
            datasets: [{
                label: "2025 NL西地区 1位予想",
                data: teamVotes,
                backgroundColor: teams.map(team => team.color),
                borderWidth: 1
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            aspectRatio: 3,
            scales: { 
                x: { beginAtZero: true },
                y: {
                    ticks: {
                        font: {
                            size: 7,  // 🎯 チーム名のフォントサイズを小さく
                            weight: "bold", // 🎯 チーム名を太字に
                            family: "Arial" // 🎯 より読みやすいフォントを指定
                        },
                        color: "#333" // 🎯 文字色を濃いグレーに
                    }
                }
            },
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: function (tooltipItem) {
                            return teams[tooltipItem.dataIndex].name + ": " + tooltipItem.raw;
                        }
                    }
                }
            }
        }
    });
}

    function updatePredictionCard() {
        if (selectedTeams.length === 0) {
            resultText.innerHTML = "未選択";
            return;
        }

        resultText.innerHTML = selectedTeams.map(teamId => {
            const team = teams.find(t => t.id === teamId);
            return `<img src="${team.logo}" width="30" alt="${team.name}">`;
        }).join(" ") + "<br>" + selectedTeams.map(teamId => teams.find(t => t.id === teamId).name).join(" & ");
    }

    updatePredictionCard();
});
