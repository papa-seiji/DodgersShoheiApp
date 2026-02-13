/* =========================================================
   Monthly Daily Trend Chart
   hogehoge_02.js
   ※ Controller 側で formValue を 1〜5（D〜S）に正規化済
   ※ window.CHART_LABELS / window.CHART_VALUES 使用版
========================================================= */

document.addEventListener("DOMContentLoaded", () => {

  // ===== 安全チェック（window版） =====
  if (!window.CHART_LABELS || !window.CHART_VALUES) {
    return;
  }

  const canvas = document.getElementById("aprilChart");
  if (!canvas) return;

  const ctx = canvas.getContext("2d");

  // Controller から来た値（1〜5）をそのまま使用
  const values = window.CHART_VALUES;

  new Chart(ctx, {
    type: "line",
    data: {
      labels: window.CHART_LABELS,
      datasets: [{
        data: values,
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
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: (context) => {
              const formMap = {
                1: "D",
                2: "C",
                3: "B",
                4: "A",
                5: "S"
              };
              return `Form: ${formMap[context.raw] ?? "-"}`;
            }
          }
        }
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

});
