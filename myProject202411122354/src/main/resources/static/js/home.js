document.addEventListener("DOMContentLoaded", function () {
    const commentList = document.getElementById("commentList");
    const counterDisplay = document.getElementById("counter-value");
    const incrementButton = document.getElementById("increment-button");
    const decrementButton = document.getElementById("decrement-button");
    const commentForm = document.getElementById("commentForm");
    const commentContent = document.getElementById("commentContent");

    let stompClient = null;

    function connectWebSocket() {
        const socket = new SockJS("/ws");
        stompClient = Stomp.over(socket);

        stompClient.connect({}, () => {
            console.log("WebSocket connected");

            // コメント購読
            stompClient.subscribe("/topic/comments", (message) => {
                const comment = JSON.parse(message.body);
                addComment(comment.content);
            });

            // カウンター購読
            stompClient.subscribe("/topic/counter", (message) => {
                const counterData = JSON.parse(message.body);
                updateCounterDisplay(counterData.value);
            });
        });
    }

    function addComment(content) {
        const li = document.createElement("li");
        li.textContent = content;
        commentList.appendChild(li);
    }

    function updateCounterDisplay(value) {
        counterDisplay.textContent = value;
    }

    function fetchCounterValue() {
        fetch("/counter")
            .then(response => response.json())
            .then(data => updateCounterDisplay(data.value))
            .catch(error => console.error("Error fetching counter value:", error));
    }

    function updateCounter(increment) {
        fetch(`/counter/update?increment=${increment}`, { method: "POST" })
            .catch(error => console.error("Error updating counter:", error));
    }

    incrementButton.addEventListener("click", () => updateCounter(1));
    decrementButton.addEventListener("click", () => updateCounter(-1));

    commentForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const content = commentContent.value;
        fetch("/comments/add", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ content }),
        })
            .then(response => response.json())
            .catch(error => console.error("Error adding comment:", error));
    });

    connectWebSocket();
    fetchCounterValue();
});
