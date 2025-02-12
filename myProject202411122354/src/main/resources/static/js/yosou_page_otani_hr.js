document.addEventListener("DOMContentLoaded", async () => {
    console.log("📢 yosou_page_otani_hr.js ロード完了");

    const yosouType = "OTANI_HR"; // 大谷翔平の本塁打予想
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

    // ✅ 本塁打予想の選択肢
    const homeRunOptions = [
        "10本台", "20本台", "30本台",
        "40本台", "50本台", "60本台", "70本台"
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
            console.log("📊 本塁打予想データ取得成功:", data);

            // ✅ 現在のユーザーの投票を取得
            currentVote = data.find(vote => vote.votedBy === currentUser) || null;
            console.log("✅ 現在の投票データ:", currentVote);

            // ✅ モーダルに既存の投票情報を表示
            const voteMessage = document.getElementById("vote-message-hr");
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
    const colors = ["#FF6384", "#36A2EB", "#FFCE56", "#4CAF50", "#9966FF", "#FF4500", "#FFD700"];

    const ctx = document.getElementById("chart-otani-hr").getContext("2d");

    if (chartInstance) {
        chartInstance.destroy();
    }

    chartInstance = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: colors
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,  // ✅ CSSサイズを優先
            cutout: "55%",  // ✅ 内部の穴を少し大きく調整
            plugins: {
                legend: {
                    position: "bottom",
                    labels: {
                        color: "white",
                        font: { size: 12 },  // ✅ 凡例のフォントサイズ
                        boxWidth: 9,  // ✅ カラーバーのサイズ調整
                        padding: 10  // ✅ 凡例とグラフの間隔
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
            animation: {
                onComplete: function () {
                    drawDodgersLogo();  // ✅ グラフ描画完了時にロゴを中央に描画
                }
            }
        }
    });

    // ✅ **ロゴを中央に適切なサイズで描画する関数**
    function drawDodgersLogo() {
        const centerX = ctx.canvas.width / 2;
        const centerY = ctx.canvas.height / 2;
        const imageSize = 70;  // ✅ ロゴのサイズ（適宜調整）
    
        const img = new Image();
        img.src = "https://www.mlbstatic.com/team-logos/119.svg";  // ✅ ドジャースのロゴ
        img.onload = function () {
            const offsetX = 25;  // ✅ 右に寄せるオフセット値（適宜調整）
            const offsetY = 15;  // ✅ 上にずらすオフセット値（適宜調整）
            ctx.drawImage(img, centerX - imageSize / 2 + offsetX, centerY - imageSize / 2, imageSize, imageSize);
        };
    }
}
        




                
    // ✅ モーダル処理
    const modal = document.getElementById("vote-modal-hr");
    const modalSelect = document.getElementById("team-select-hr");
    const voteButton = document.getElementById("vote-button-hr");

    function openModal() {
        modal.style.display = "block";

        if (currentVote) {
            document.getElementById("vote-message-hr").innerText = `現在の投票: ${currentVote.yosouValue}`;
        } else {
            document.getElementById("vote-message-hr").innerText = "未投票";
        }
    }

    function closeModal() {
        modal.style.display = "none";
    }

    document.getElementById("otani-hr").addEventListener("click", openModal);
    document.getElementById("close-modal-hr").addEventListener("click", closeModal);

    voteButton.addEventListener("click", () => {
        const selectedValue = modalSelect.value;
        if (selectedValue) {
            sendVote(selectedValue);
        }
    });

    await fetchCurrentUser();
    fetchYosouData();
});
