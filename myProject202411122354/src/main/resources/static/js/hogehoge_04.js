/* =========================================================
   hogehoge_04.js（完成版）
========================================================= */
document.addEventListener("DOMContentLoaded", () => {

  // =========================
  // 📊 グラフ描画
  // =========================

  if (window.CHART_LABELS && window.CHART_VALUES) {

    const canvas = document.getElementById("hogehoge04Chart");
    if (canvas) {
      const ctx = canvas.getContext("2d");

      new Chart(ctx, {
        type: "line",
        data: {
          labels: window.CHART_LABELS,
          datasets: [{
            data: window.CHART_VALUES,
            borderColor: "#1565c0",
            backgroundColor: "rgba(21,101,192,0.15)",
            tension: 0.35,
            fill: true,
            pointRadius: 4,
            pointHoverRadius: 6
          }]
        },
        options: {
          plugins: {
            legend: { display: false }
          },
          scales: {
            y: {
              min: 1,
              max: 5,
              ticks: {
                stepSize: 1,
                callback: (value) => ({
                  1: "D",
                  2: "C",
                  3: "B",
                  4: "A",
                  5: "S"
                })[value] ?? ""
              }
            }
          }
        }
      });
    }
  }

  // =========================
  // 🔥 Savant分布描画
  // =========================

  if (!window.PITCH_DATA || window.PITCH_DATA.length === 0) {
    console.warn("⚠️ PITCH_DATAなし");
    return;
  }

  console.log("PITCH_DATA =", window.PITCH_DATA.length);

  // 🔥 描画前にクリア（重要）
  const zone = document.getElementById("pitch-zone");
  if (zone) zone.querySelectorAll(".pitch").forEach(e => e.remove());

  window.PITCH_DATA.forEach((p, i) => {
    if (p.pX != null && p.pZ != null) {
      plotPitch(p.pX, p.pZ, p.type, i);
    }
  });

});


// =========================
// 🎯 描画関数
// =========================

function plotPitch(pX, pZ, type, index) {

  const zone = document.getElementById("pitch-zone");
  if (!zone) return;

  const centerX = 165;
  const centerY = 200;
  const scale = 76;

  let x = centerX + (pX * scale);
  let y = centerY - ((pZ - 2.4) * scale);


  // =========================
  // 🔥 クランプ処理（ここを追加）pitch-zone外に出る球を→ 内側に収める（クランプ）
  // =========================

  const padding = 10;

  const minX = padding;
  const maxX = zone.clientWidth - padding;

  const minY = padding;
  const maxY = zone.clientHeight - padding;

x = Math.max(minX, Math.min(x, maxX));
y = Math.max(minY, Math.min(y, maxY));



  // 🔵 ドット生成
  const dot = document.createElement("div");
  dot.className = "pitch";

  const color = getColor(type);

  dot.style.left = x + "px";
  dot.style.top = y + "px";
  dot.style.background = color.bg;
  dot.style.color = color.text;

  // 🔥 番号表示（1〜87）
  dot.innerText = index + 1;

  zone.appendChild(dot);
}


// =========================
// 🎨 球種カラー（完全版）
// =========================

function getColor(type) {
  switch(type) {

    // 🔴 速球系
    case "FF": return { bg: "red", text: "white" };        // フォーシーム
    case "SI": return { bg: "orange", text: "white" };     // シンカー
    case "FC": return { bg: "darkred", text: "white" };    // カット

    // 🔵 横変化
    case "SL": return { bg: "blue", text: "white" };       // スライダー
    case "ST": return { bg: "deepskyblue", text: "white" };// スイーパー

    // 🟡 カーブ系
    case "CU": return { bg: "yellow", text: "black" };     // カーブ
    case "KC": return { bg: "gold", text: "black" };       // ナックルカーブ

    // 🟢 落ちる系
    case "CH": return { bg: "green", text: "white" };      // チェンジアップ
    case "FS": return { bg: "limegreen", text: "black" };  // スプリット

    default:
      console.warn("未対応球種:", type);
      return { bg: "white", text: "black" };
  }
}