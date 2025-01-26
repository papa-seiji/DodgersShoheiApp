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

// サニタイズ関数
function sanitizeHTML(str) {
    if (!str) return ''; // nullやundefinedを防ぐ
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;')
        .replace(/\(/g, '&#40;')
        .replace(/\)/g, '&#41;');
}

function openModal(title, details, imageUrl) {
    const modal = document.getElementById("news-modal");
    const modalTitle = document.getElementById("modal-title");
    const modalContent = document.getElementById("modal-content");
    const modalImages = document.getElementById("modal-images");

    modalTitle.textContent = sanitizeHTML(title);
    modalContent.innerHTML = sanitizeHTML(details) || "詳細情報がありません。";

    // 複数画像を処理
    if (imageUrl) {
        modalImages.innerHTML = ""; // リセット
        const images = imageUrl.split(','); // カンマ区切りで分割
        images.forEach((url) => {
            const img = document.createElement('img');
            img.src = sanitizeHTML(url.trim());
            img.alt = "News Image";
            img.style.maxWidth = "100%";
            modalImages.appendChild(img);
        });
        modalImages.style.display = "block";
    } else {
        modalImages.style.display = "none";
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
