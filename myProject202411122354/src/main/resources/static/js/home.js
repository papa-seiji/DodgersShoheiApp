document.addEventListener("DOMContentLoaded", function () {
    const counterDisplay = document.getElementById("counter-value");
    const incrementButton = document.getElementById("increment-button");
    const decrementButton = document.getElementById("decrement-button");

    // サーバーから現在のカウンター値を取得して表示
    function fetchCounterValue() {
        fetch("/counter")
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to fetch counter value");
                }
                return response.json();
            })
            .then(data => {
                counterDisplay.textContent = data.value;
            })
            .catch(error => console.error("Error fetching counter value:", error));
    }

    // カウンター値を増加
    incrementButton.addEventListener("click", function () {
        updateCounter(1);
    });

    // カウンター値を減少
    decrementButton.addEventListener("click", function () {
        updateCounter(-1);
    });

    // サーバーにリクエストを送信してカウンター値を更新
    function updateCounter(increment) {
        fetch(`/counter/update?increment=${increment}`, {
            method: "POST",
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to update counter");
                }
                return response.json();
            })
            .then(data => {
                counterDisplay.textContent = data.value; // 更新された値を表示
            })
            .catch(error => console.error("Error updating counter:", error));
    }

    // 初期値を取得して表示
    fetchCounterValue();
});
