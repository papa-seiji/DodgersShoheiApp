/* =========================================================
   April Daily Trend (Controller Sync)
   hogehoge_02.js
========================================================= */
document.addEventListener("DOMContentLoaded", () => {

  if (typeof APRIL_LABELS === "undefined") return;

  const ctx = document.getElementById("aprilChart").getContext("2d");

  // ★ Controller の value(1〜5)をそのまま使う
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
            label: (c) => {
              const map = {1:"D",2:"C",3:"B",4:"A",5:"S"};
              return `Form: ${map[c.raw]}`;
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
            callback: v => ({1:"D",2:"C",3:"B",4:"A",5:"S"}[v])
          }
        }
      }
    }
  });
});
