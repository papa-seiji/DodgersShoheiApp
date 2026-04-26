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

  const zone = document.getElementById("pitch-zone");

  // 🔥 描画前にクリア
  if (zone) {
    zone.querySelectorAll(".pitch").forEach(e => e.remove());
  }

  window.PITCH_DATA.forEach((p, i) => {
    if (p.pX != null && p.pZ != null) {
      plotPitch(p.pX, p.pZ, p.type, i);
    }
  });

  // =========================
  // 🎯 spin MAX/MIN 集計
  // =========================

  const spinStats = {};

  window.PITCH_DATA.forEach(p => {

    if (!p.type || !p.spinRate) return;

    if (!spinStats[p.type]) {
      spinStats[p.type] = {
        max: p.spinRate,
        min: p.spinRate
      };
    } else {
      spinStats[p.type].max = Math.max(spinStats[p.type].max, p.spinRate);
      spinStats[p.type].min = Math.min(spinStats[p.type].min, p.spinRate);
    }

  });

  console.log("🔥 spinStats =", spinStats);


// =========================
// 🎯 Usage集計（球種割合）
// =========================

// ① 先にカウント作る
const usageStats = {};
const totalPitches = window.PITCH_DATA.length;

window.PITCH_DATA.forEach(p => {
  if (!p.type) return;

  if (!usageStats[p.type]) {
    usageStats[p.type] = 1;
  } else {
    usageStats[p.type]++;
  }
});

console.log("🔥 usageStats =", usageStats);

// ② そのあと％計算
const usagePercent = {};

Object.keys(usageStats).forEach(type => {
  usagePercent[type] = Math.round((usageStats[type] / totalPitches) * 100);
});

console.log("🔥 usagePercent =", usagePercent);

// =========================
// 🚀 Velocity Avg 集計
// =========================

  const velocityStats = {};
  const velocityCount = {};

  window.PITCH_DATA.forEach(p => {
    if (!p.type || !p.velocity) return;

    const velo = p.velocity;

    if (!velocityStats[p.type]) {
      velocityStats[p.type] = 0;
      velocityCount[p.type] = 0;
    }

    velocityStats[p.type] += velo;
    velocityCount[p.type] += 1;
  });

  const velocityAvg = {};

  Object.keys(velocityStats).forEach(type => {
    velocityAvg[type] = Math.round(
      velocityStats[type] / velocityCount[type]
    );
  });

  // 👇これ追加
  window.velocityAvg = velocityAvg;

  console.log("🔥 velocityAvg =", velocityAvg);

  // =========================
  // 🚀 Velocity MAX 集計（🔥追加）
  // =========================

  const velocityMax = {};

  window.PITCH_DATA.forEach(p => {
    if (!p.type || !p.velocity) return;

    if (!velocityMax[p.type]) {
      velocityMax[p.type] = p.velocity;
    } else {
      velocityMax[p.type] = Math.max(velocityMax[p.type], p.velocity);
    }
  });

console.log("🔥 velocityMax =", velocityMax);


const spinContainer = document.getElementById("spin-stats");
const usageContainer = document.getElementById("usage-stats");
const velocityContainer = document.getElementById("velocity-stats");


// =========================
// 🎯 表示ループ（ここが核心）
// =========================

const ORDER = ["FF","SI","FC","SL","ST","CU","KC","CH","FS"];

ORDER.forEach(type => {

  const color = getColor(type);

  // ================= RPM =================
  if (spinStats[type] !== undefined){
    const row = document.createElement("div");
    row.className = "spin-row";
    row.dataset.type = type;  // ←🔥これ追加

    row.innerHTML = `
      <span class="spin-dot" style="background:${color.bg};"></span>
      MAX:${spinStats[type].max}rpm / MIN:${spinStats[type].min}rpm
    `;

    spinContainer.appendChild(row);
  }

  // ================= Usage =================
  if (usagePercent[type]) {
    const row = document.createElement("div");
    row.className = "usage-row";
    row.dataset.type = type;  // ←🔥これ追加

    const bar = document.createElement("div");
    bar.className = "usage-bar";
    bar.style.width = usagePercent[type] + "%";
    bar.style.background = color.bg;

    const wrapper = document.createElement("div");
    wrapper.className = "usage-bar-wrapper";
    wrapper.appendChild(bar);

    const value = document.createElement("span");
    value.className = "usage-value";
    value.textContent = usagePercent[type] + "%";

    const dot = document.createElement("span");
    dot.className = "usage-dot";
    dot.style.background = color.bg;

    row.appendChild(dot);       // ← 追加
    row.appendChild(wrapper);
    row.appendChild(value);

    usageContainer.appendChild(row);
  }

// ================= Velocity 平均速度 Avg=================
  if (velocityAvg[type] !== undefined) {
    const row = document.createElement("div");
    row.className = "velocity-row";
    row.dataset.type = type;  // ←🔥これ追加

    // 🔥 ドット追加
    const dot = document.createElement("span");
    dot.className = "usage-dot";  // ← 既存CSS流用
    dot.style.background = color.bg;

    // 🔥 テキスト部分
    const text = document.createElement("span");
    text.textContent = velocityAvg[type] + " mph";

    // 🔥 順番重要
    row.appendChild(dot);
    row.appendChild(text);

    velocityContainer.appendChild(row);
  }

  // ================= Velocity MAX 最高速 =================
    if (velocityMax[type] !== undefined) {
      const row = document.createElement("div");
      row.className = "velocity-row";
      row.dataset.type = type;

      const dot = document.createElement("span");
      dot.className = "usage-dot";
      dot.style.background = color.bg;

      const text = document.createElement("span");
      text.textContent = velocityMax[type] + " mph";

      row.appendChild(dot);
      row.appendChild(text);

      document.getElementById("velocity-max-stats").appendChild(row);
    }
});


    // =========================
    // 🎯 表のハイライト制御（STEP3）
    // =========================
    function updateTableHighlight() {

      // RPM
      document.querySelectorAll(".spin-row").forEach(row => {
        const type = row.dataset.type;

        row.style.opacity =
          (!activePitchType || type === activePitchType) ? "1" : "0.05";
      });

      // Usage
      document.querySelectorAll(".usage-row").forEach(row => {
        const type = row.dataset.type;

        row.style.opacity =
          (!activePitchType || type === activePitchType) ? "1" : "0.05";
      });

      // Velocity
      document.querySelectorAll(".velocity-row").forEach(row => {
        const type = row.dataset.type;

        row.style.opacity =
          (!activePitchType || type === activePitchType) ? "1" : "0.05";
      });

    }


// =========================
// 🎯 フィルター機能（🔥ここが今回の修正ポイント）
// =========================

document.querySelectorAll(".legend-item").forEach(item => {

  item.addEventListener("click", () => {

    const type = item.dataset.type;

    // 🔥 STEP2：選択状態管理
    if (activePitchType === type) {
      activePitchType = null; // 解除
    } else {
      activePitchType = type;
    }

    document.querySelectorAll(".pitch").forEach(dot => {

      if (!activePitchType) {
        dot.style.display = "block";
        dot.style.opacity = "1"; // ←🔥これ追加（リセット）
      } else {
        if (dot.dataset.type === activePitchType) {
          dot.style.display = "block";
          dot.style.opacity = "1"; // ←🔥これ追加
        } else {
          dot.style.display = "block";
          dot.style.opacity = "0.1";
        }
      }

    });

    // 🔥🔥🔥 ここを追加（STEP3）
    updateTableHighlight();

      // 🔥 legendの見た目制御
      document.querySelectorAll(".legend-item").forEach(el => {

        if (!activePitchType) {
          el.style.opacity = "1";
          el.style.fontWeight = "normal";
        } else {
          if (el.dataset.type === activePitchType) {
            el.style.opacity = "1";
            el.style.fontWeight = "bold";   // ←ここ重要
          } else {
            el.style.opacity = "0.3";
            el.style.fontWeight = "normal";
          }
        }
      });
    });
  });
});


  // =========================
  // 🎯 選択中の球種（STEP2）
  // =========================
  let activePitchType = null;

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
  // 🔥 クランプ処理（ゾーン外 → 内側に収める）
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

  // 🔥 フィルター用データ埋め込み
  dot.dataset.type = type;

  const color = getColor(type);

  dot.style.left = x + "px";
  dot.style.top = y + "px";
  dot.style.background = color.bg;
  dot.style.color = color.text;

  // 🔥 番号表示
  dot.innerText = index + 1;

  zone.appendChild(dot);
}


// =========================
// 🎨 球種カラー（完全版）
// =========================
function getColor(type) {
  switch(type) {

    // 🔴 速球系
    case "FF": return { bg: "red", text: "white" };
    case "SI": return { bg: "orange", text: "white" };
    case "FC": return { bg: "darkred", text: "white" };

    // 🔵 横変化
    case "SL": return { bg: "blue", text: "white" };
    case "ST": return { bg: "deepskyblue", text: "white" };

    // 🟡 カーブ系
    case "CU": return { bg: "yellow", text: "black" };
    case "KC": return { bg: "gold", text: "black" };

    // 🟢 落ちる系
    case "CH": return { bg: "green", text: "white" };
    case "FS": return { bg: "limegreen", text: "black" };

    default:
      console.warn("未対応球種:", type);
      return { bg: "white", text: "black" };
  }
}