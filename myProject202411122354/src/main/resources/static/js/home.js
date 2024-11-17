document.addEventListener("DOMContentLoaded", () => {
    const commentList = document.getElementById("commentList");

    // コメントを描画する関数
    function appendComment(comment) {
        const li = document.createElement("li");
        li.textContent = `${comment.username}: ${comment.content} (${comment.createdAt})`;
        commentList.appendChild(li);
    }

    // コメントを取得して描画
    async function fetchAndDisplayComments() {
        try {
            const response = await fetch("/comments");
            if (!response.ok) {
                throw new Error("Failed to fetch comments");
            }
            const comments = await response.json();
            commentList.innerHTML = ""; // リストをクリア
            comments.forEach(comment => appendComment(comment));
        } catch (error) {
            console.error("Error fetching comments:", error);
        }
    }

    // 初期表示
    fetchAndDisplayComments();
});
