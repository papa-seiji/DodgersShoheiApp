/* =========================================================
   Monthly Daily Trend Chart
   hogehoge_02.js
   ※ Controller 側で formValue を 1〜5（D〜S）に正規化済
========================================================= */
document.addEventListener("DOMContentLoaded", () => {

  // Controller から未連携の場合は何もしない
  if (typeof APRIL_LABELS === "undefined" || typeof APRIL_VALUES === "undefined") {
    return;
  }

  const canvas = document.getElementById("aprilChart");
  if (!canvas) return;

  const ctx = canvas.getContext("2d");

  // ★ Controller から来た値（1〜5）をそのまま使用
  const values = APRIL_VALUES;

  new Chart(ctx, {
    type: "line",
    data: {
      labels: APRIL_LABELS,
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
