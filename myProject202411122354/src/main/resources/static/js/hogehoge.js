/* =========================================================
   hogehoge.js
   - Season Trend Graph (April)
   - Month Calendar Navigation
========================================================= */

document.addEventListener("DOMContentLoaded", () => {

  /* =========================
     ① April 旬平均グラフ
  ========================= */
  const canvas = document.getElementById("seasonTrendChart");

  if (canvas && window.APRIL_LABELS && window.APRIL_VALUES) {

    const ctx = canvas.getContext("2d");

    new Chart(ctx, {
      type: "line",
      data: {
        labels: window.APRIL_LABELS,
        datasets: [{
          label: "April Form",
          data: window.APRIL_VALUES,

          borderColor: "#c83c3c",
          backgroundColor: "rgba(200, 60, 60, 0.15)",
          borderWidth: 2,
          tension: 0.35,

          pointRadius: 5,
          pointHoverRadius: 7,
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
          legend: { display: false }
        },
        scales: {
          y: {
            min: 1,
            max: 5,
            ticks: {
              stepSize: 1,
              callback: v => ({1:"D",2:"C",3:"B",4:"A",5:"S"}[v] || v)
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
