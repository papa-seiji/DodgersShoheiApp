document.getElementById('commentForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    const formData = new FormData(event.target);
    const content = formData.get('content');

    try {
        const response = await fetch('/comments', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams({ content })
        });

        if (response.ok) {
            const result = await response.text();
            alert(result);
        } else {
            alert('Failed to post comment.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error occurred while posting comment.');
    }


    // スクロールイベントのリスナー
    window.addEventListener("scroll", () => {
        if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 200 && !loading) {
            fetchAndDisplayComments();
        }
    });

    // 初期表示
    fetchAndDisplayComments();
});
