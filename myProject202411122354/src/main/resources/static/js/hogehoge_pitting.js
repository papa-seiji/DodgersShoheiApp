document.addEventListener("DOMContentLoaded", () => {

  document.querySelectorAll(".month-calendar").forEach(calendar => {
    calendar.addEventListener("click", () => {
      const month = calendar.dataset.month;
      window.location.href = `/hogehoge_04?month=${month}`;
    });
  });

});
