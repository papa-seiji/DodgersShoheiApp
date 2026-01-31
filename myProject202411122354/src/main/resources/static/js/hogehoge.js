/* =========================================================
   Season Form Trend（ダミー）
   hogehoge.js
========================================================= */

document.addEventListener("DOMContentLoaded", () => {

  const canvas = document.getElementById("seasonTrendChart");
  if (!canvas) return;

  const ctx = canvas.getContext("2d");

  // ダミーデータ（Apr–Nov）
  const labels = ["4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月"];
  const formData = [3, 4, 5, 4, 4, 5, 6, 5]; // 仮の調子推移

  new Chart(ctx, {
    type: "line",
    data: {
      labels: labels,
      datasets: [{
        label: "Season Form",
        data: formData,

        /* 見た目 */
        borderColor: "#c83c3c",
        backgroundColor: "rgba(200, 60, 60, 0.15)",
        borderWidth: 2,
        tension: 0.35,

        /* 点 */
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBackgroundColor: "#c83c3c",
        pointBorderColor: "#ffffff",
        pointBorderWidth: 2,

        fill: true
      }]
    },

    options: {
      responsive: true,
      maintainAspectRatio: false,

      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          callbacks: {
            label: (ctx) => {
              const rank = ["D","C","B","A","S","S+"];
              return `Form: ${rank[ctx.raw - 1] || ctx.raw}`;
            }
          }
        }
      },

      scales: {
        x: {
          grid: {
            display: false
          }
        },
        y: {
          min: 1,
          max: 6,
          ticks: {
            stepSize: 1,
            callback: (value) => {
              const map = {
                1: "D",
                2: "C",
                3: "B",
                4: "A",
                5: "S",
                6: "S+"
              };
              return map[value] || value;
            }
          }
        }
      }
    }
  });

});


// hogehoge.js

document.addEventListener("DOMContentLoaded", () => {

  const calendars = document.querySelectorAll(".month-calendar");

  calendars.forEach(calendar => {
    calendar.addEventListener("click", () => {
      const month = calendar.dataset.month;

      // ✅ 将来拡張を考慮して month を付与
      window.location.href = `hogehoge_02?month=${month}`;
    });
  });

});
