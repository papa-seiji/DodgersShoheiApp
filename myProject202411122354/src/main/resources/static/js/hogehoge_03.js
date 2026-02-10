/* =========================================================
   Pitching Season Average Chart
   hogehoge_03.js
   - 投手：月平均評価（4月〜11月）
========================================================= */
document.addEventListener("DOMContentLoaded", () => {

  // Controller から値が来ていなければ何もしない
  if (
    typeof PITCHING_MONTH_LABELS === "undefined" ||
    typeof PITCHING_MONTH_AVERAGES === "undefined"
  ) {
    return;
  }

  const canvas = document.getElementById("pitchingSeasonChart");
  if (!canvas) return;

  const ctx = canvas.getContext("2d");

  new Chart(ctx, {
    type: "line",
    data: {
      labels: PITCHING_MONTH_LABELS,
      datasets: [{
        data: PITCHING_MONTH_AVERAGES,
        borderColor: "#2e7d32",
        backgroundColor: "rgba(46,125,50,0.18)",
        tension: 0.3,
        fill: true,
        pointRadius: 6,
        pointHoverRadius: 8
      }]
    },
    options: {
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          callbacks: {
            label: (context) => {
              const v = context.raw;
              if (v == null) {
                return "登板なし";
              }
              const map = {1:"D",2:"C",3:"B",4:"A",5:"S"};
              return `平均評価：${map[Math.round(v)]}（${v.toFixed(2)}）`;
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
