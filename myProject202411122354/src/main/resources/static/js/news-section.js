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

            data.forEach((news, index) => {
                const isNew = index === 0;

                let newsContent;
                if (!news.link) {
                    newsContent = `<span class="news-text">${news.content}</span>`;
                } else if (news.link.startsWith('/')) {
                    newsContent = `<a href="${news.link}" class="news-link">${news.content}</a>`;
                } else if (news.link === 'モーダル') {
                    newsContent = `<span class="news-link" onclick="openModal('${news.content}', '${news.details}', '${news.imageUrl}')">${news.content}</span>`;
                }

                const newsHTML = `
                    <div class="news-item">
                        <span class="news-date">${news.date}</span>
                        ${newsContent}
                        ${isNew ? '<span class="news-new">NEW!</span>' : ''}
                    </div>
                `;
                newsList.insertAdjacentHTML('beforeend', newsHTML);
            });
        })
        .catch(error => console.error('Error fetching news updates:', error));
});

// モーダルを開く関数
function openModal(title, details, imageUrls) {
    const modal = document.getElementById("news-modal");
    const modalTitle = document.getElementById("modal-title");
    const modalContent = document.getElementById("modal-content");
    const modalImages = document.getElementById("modal-images");

    // モーダルのタイトルと詳細を設定
    modalTitle.textContent = title;
    modalContent.textContent = details || "詳細情報がありません。";

    // モーダルの画像をクリア
    modalImages.innerHTML = '';

    // カンマ区切りの画像URLを処理
    if (imageUrls) {
        const urls = imageUrls.split(','); // カンマで分割
        urls.forEach(url => {
            const imgElement = document.createElement("img");
            imgElement.src = url.trim(); // URLの余白を削除
            imgElement.alt = "News Image";
            imgElement.classList.add("modal-image");
            modalImages.appendChild(imgElement);
        });
    }

    modal.style.display = "block";

    window.addEventListener("click", handleOutsideClick);
}

function closeModal() {
    const modal = document.getElementById("news-modal");
    modal.style.display = "none";
    window.removeEventListener("click", handleOutsideClick);
}

function handleOutsideClick(event) {
    const modal = document.getElementById("news-modal");
    if (event.target === modal) {
        closeModal();
    }
}
