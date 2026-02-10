/* =========================================================
   Monthly Daily Trend Chart
   hogehoge_04.js
========================================================= */
document.addEventListener("DOMContentLoaded", () => {

  if (
    typeof HOGEHOGE04_LABELS === "undefined" ||
    typeof HOGEHOGE04_VALUES === "undefined"
  ) {
    return;
  }

  const canvas = document.getElementById("hogehoge04Chart");
  if (!canvas) return;

  const ctx = canvas.getContext("2d");

  const values = HOGEHOGE04_VALUES;

  new Chart(ctx, {
    type: "line",
    data: {
      labels: HOGEHOGE04_LABELS,
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
