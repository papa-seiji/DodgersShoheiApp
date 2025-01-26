document.addEventListener('DOMContentLoaded', () => {
    const newsForm = document.getElementById('newsForm');
    const newsList = document.getElementById('news-list');

    // フォーム送信処理
    newsForm.addEventListener('submit', (event) => {
        event.preventDefault();

        const formData = {
            date: document.getElementById('date').value,
            content: document.getElementById('content').value,
            details: document.getElementById('details').value,
            imageUrl: document.getElementById('imageUrl').value,
            link: document.getElementById('link').value,
        };

        fetch('/admin/news/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData),
        })
            .then((response) => response.text())
            .then((data) => {
                alert(data);
                fetchNews(); // ニュースを再取得して更新
            })
            .catch((error) => console.error('Error:', error));
    });

    // ニュースデータを取得して表示
    function fetchNews() {
        fetch('/api/news')
            .then((response) => {
                if (!response.ok) {
                    throw new Error('Failed to fetch news updates');
                }
                return response.json();
            })
            .then((data) => {
                newsList.innerHTML = '';
                data.forEach((news) => {
                    const newsHTML = `
                        <div class="news-item">
                            <p><strong>日付:</strong> ${news.date}</p>
                            <p><strong>コンテンツ:</strong> ${news.content}</p>
                            <p><strong>リンク:</strong> ${news.link || 'なし'}</p>
                            <p><strong>画像URL:</strong> ${news.imageUrl || 'なし'}</p>
                            <p><strong>詳細:</strong> ${news.details || 'なし'}</p>
                        </div>
                    `;
                    newsList.insertAdjacentHTML('beforeend', newsHTML);
                });
            })
            .catch((error) => console.error('Error fetching news:', error));
    }

    // 初期ロード時にニュースを取得
    fetchNews();
});
