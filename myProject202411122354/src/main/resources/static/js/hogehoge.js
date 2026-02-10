/* =========================================================
   hogehoge.js
   - Season Trend Graph (April)
   - Month Calendar Navigation
========================================================= */

document.addEventListener("DOMContentLoaded", () => {

/* =========================
   ① Season 旬平均グラフ（4月〜今ある月まで）
========================= */

const canvas = document.getElementById("seasonTrendChart");

if (
  canvas &&
  Array.isArray(window.LABELS) &&
  Array.isArray(window.VALUES) &&
  window.LABELS.length > 0
) {

  const ctx = canvas.getContext("2d");

  new Chart(ctx, {
    type: "line",
    data: {
      labels: window.LABELS,
      datasets: [{
        label: "Season Form Trend",
        data: window.VALUES,

        borderColor: "#c83c3c",
        backgroundColor: "rgba(200, 60, 60, 0.15)",
        borderWidth: 2,
        tension: 0.35,

        pointRadius: 5,
        pointHoverRadius: 7,
        pointBackgroundColor: "#c83c3c",
        pointBorderColor: "#ffffff",
        pointBorderWidth: 2,

        fill: true,

        // ★ null は欠損点として扱う
        spanGaps: true
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false }
      },
      scales: {
        y: {
          min: -2,
          max: 2,
          ticks: {
            stepSize: 1,
            callback: v => (
              { 2: "S", 1: "A", 0: "B", "-1": "C", "-2": "D" }[v] ?? ""
            )
          }
        }
      }
    }
  });
}

  /* =========================
     ② カレンダー → hogehoge_02
  ========================= */
  document.querySelectorAll(".month-calendar").forEach(calendar => {
    calendar.addEventListener("click", () => {
      const month = calendar.dataset.month;
      window.location.href = `/hogehoge_02?month=${month}`;
    });
  });


});
