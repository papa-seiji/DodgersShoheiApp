document.addEventListener("DOMContentLoaded", function () {
    const counters = {
        MainCounter: document.getElementById("main-counter-value"),
        SecondaryCounter: document.getElementById("secondary-counter-value"),
    };

    function fetchCounterValues() {
        Object.keys(counters).forEach(counterName => {
            fetch(`/counter/${counterName}`)
                .then(response => response.json())
                .then(data => {
                    counters[counterName].textContent = data.value;
                })
                .catch(error => console.error(`Error fetching ${counterName}:`, error));
        });
    }

    function updateCounter(counterName, increment) {
        fetch(`/counter/${counterName}/update?increment=${increment}`, { method: "POST" })
            .then(response => response.json())
            .then(data => {
                counters[counterName].textContent = data.value;
            })
            .catch(error => console.error(`Error updating ${counterName}:`, error));
    }

    // Event Listeners
    document.getElementById("main-increment-button").addEventListener("click", () => updateCounter("MainCounter", 1));
    document.getElementById("main-decrement-button").addEventListener("click", () => updateCounter("MainCounter", -1));

    document.getElementById("secondary-increment-button").addEventListener("click", () => updateCounter("SecondaryCounter", 1));
    document.getElementById("secondary-decrement-button").addEventListener("click", () => updateCounter("SecondaryCounter", -1));

    // Initialize counters
    fetchCounterValues();
});
