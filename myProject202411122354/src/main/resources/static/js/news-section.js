document.addEventListener('DOMContentLoaded', () => {
    const newsList = document.querySelector('.news-list');

    // APIからニュースデータを取得
    fetch('/api/news')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch news updates');
            }
            return response.json();
        })
        .then(data => {
            newsList.innerHTML = ''; // リストをクリア

            // ニュースデータを反映
            data.forEach((news, index) => {
                const isNew = index === 0; // 最初の項目を「NEW!」に設定

                const newsHTML = `
                    <div class="news-item">
                        <span class="news-date">${news.date}</span>
                        <span class="news-link">${news.content}</span>
                        ${isNew ? '<span class="news-new">NEW!</span>' : ''} <!-- NEW!を追加 -->
                    </div>
                `;
                newsList.insertAdjacentHTML('beforeend', newsHTML);
            });
        })
        .catch(error => console.error('Error fetching news updates:', error));
});
