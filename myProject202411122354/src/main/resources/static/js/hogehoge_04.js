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
// 📊 Usage 表示（赤枠部分）
// =========================

const usageContainer = document.getElementById("usage-stats");
if (usageContainer) {
  usageContainer.innerHTML = ""; // 初期化

  // 👇🔥これ追加（Usageタイトル）
  const usageTitle = document.createElement("div");
  usageTitle.className = "spin-title";
  usageTitle.textContent = "Usage（使用率）";
  usageContainer.appendChild(usageTitle);

  const order = ["FF","SI","FC","SL","ST","CU","KC","CH","FS"];

  order.forEach(type => {
    if (!usagePercent[type]) return;

    const row = document.createElement("div");
    row.className = "usage-row";

    const dot = document.createElement("span");
    dot.className = "usage-dot";

    const color = getColor(type);
    dot.style.background = color.bg;

    const text = document.createElement("span");
    text.textContent = `${usagePercent[type]}%`;

    row.appendChild(dot);
    row.appendChild(text);

    usageContainer.appendChild(row);
  });
}

// =========================
// 🎯 spin表示
// =========================

const container = document.getElementById("spin-stats");
if (container) {

  container.innerHTML = "";

  // 👇🔥これ追加（タイトル復活）
  const title = document.createElement("div");
  title.className = "spin-title";
  title.textContent = "RPM Spin（回転数）";
  container.appendChild(title);

  const ORDER = ["FF","SI","FC","SL","ST","CU","KC","CH","FS"];

    ORDER.forEach(type => {

      const stat = spinStats[type];
      if (!stat) return;

      const color = getColor(type);

      const row = document.createElement("div");
      row.className = "spin-row";

      row.innerHTML = `
        <span class="spin-dot" style="background:${color.bg};"></span>
        MAX: ${stat.max} / MIN: ${stat.min}
      `;

      container.appendChild(row);

    });
}



  // =========================
  // 🎯 フィルター機能（🔥ここが今回の修正ポイント）
  // =========================
  document.querySelectorAll(".legend-item").forEach(item => {

    item.addEventListener("click", () => {

      const type = item.dataset.type;

      // ON/OFFトグル
      item.classList.toggle("active");

      const activeTypes = Array.from(document.querySelectorAll(".legend-item.active"))
        .map(el => el.dataset.type);

      document.querySelectorAll(".pitch").forEach(dot => {

        if (activeTypes.length === 0) {
          // 全解除 → 全表示
          dot.style.display = "block";
        } else {
          // フィルター
          if (activeTypes.includes(dot.dataset.type)) {
            dot.style.display = "block";
          } else {
            dot.style.display = "none";
          }
        }

      });

    });

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