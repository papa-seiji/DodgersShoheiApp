document.addEventListener("DOMContentLoaded", function () {
    const gallery = document.getElementById("image-gallery");
    const uploadForm = document.getElementById("image-upload-form");
    const visitorCounter = document.getElementById("visitor-counter-value");
    const modal = document.getElementById("image-modal");
    const modalImage = document.getElementById("modal-image");
    const modalDescription = document.getElementById("modal-description");
    const closeModal = document.querySelector(".close");
    let stompClient = null;

    // WebSocket初期化
    function initializeWebSocket() {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function () {
            stompClient.subscribe('/topic/proudGallery', function (message) {
                const newImage = JSON.parse(message.body);
                console.log("Received new image via WebSocket:", newImage);
                addImageToGallery(newImage, true);
            });

            stompClient.subscribe('/topic/visitorCounter', function (message) {
                const newVisitorCount = JSON.parse(message.body);
                if (visitorCounter) visitorCounter.textContent = newVisitorCount;
            });

            stompClient.subscribe('/topic/proudLikes', function (message) {
                const updatedImage = JSON.parse(message.body);
                console.log("Received likes update via WebSocket:", updatedImage);
                updateLikesOnCard(updatedImage.id, updatedImage.likesCount);
            });
        });
    }

    // Visitorカウンターの取得
    function fetchVisitorCounter() {
        fetch('/api/visitorCounter')
            .then(response => response.json())
            .then(count => {
                if (visitorCounter) visitorCounter.textContent = count;
            })
            .catch(error => console.error("Error fetching visitor counter:", error));
    }

    // ギャラリーに画像を追加
    const addedImageIds = new Set(); // 重複チェック用
    function addImageToGallery(image, isWebSocketUpdate = false) {
        if (addedImageIds.has(image.id)) {
            if (isWebSocketUpdate) console.warn("Duplicate image received via WebSocket:", image);
            return;
        }
        addedImageIds.add(image.id);

        const card = document.createElement("div");
        card.classList.add("card");

        const img = document.createElement("img");
        img.src = image.imageUrl;
        img.alt = image.description;
        img.addEventListener("click", function () {
            openModal(image.imageUrl, image.description); // モーダルを開く
        });

        const description = document.createElement("p");
        description.textContent = image.description;

        const createdBy = document.createElement("small");
        createdBy.textContent = `Posted by: ${image.createdBy} at ${new Date(image.createdAt).toLocaleString()}`;

        const likeContainer = document.createElement("div");
        likeContainer.classList.add("like-container");
        likeContainer.setAttribute("data-image-id", image.id);

        const likeButton = document.createElement("button");
        likeButton.classList.add("like-button");
        likeButton.textContent = "❤";

        const likeCount = document.createElement("span");
        likeCount.classList.add("like-count");
        likeCount.textContent = image.likesCount || 0;

        likeButton.addEventListener("click", function () {
            toggleLike(image.id);
        });

        likeContainer.appendChild(likeButton);
        likeContainer.appendChild(likeCount);

        card.appendChild(img);
        card.appendChild(description);
        card.appendChild(createdBy);
        card.appendChild(likeContainer);

        gallery.appendChild(card);
    }

    // 「イイね」カウントの更新
    function updateLikesOnCard(imageId, newLikesCount) {
        const card = [...gallery.children].find(
            card => card.querySelector(".like-container")?.getAttribute("data-image-id") == imageId
        );
        if (card) {
            const likeCount = card.querySelector(".like-count");
            if (likeCount) {
                likeCount.textContent = newLikesCount;
            }
        }
    }

    // 「イイね」トグル
    function toggleLike(imageId) {
        fetch(`/api/proud/like/${imageId}`, {
            method: "POST",
        })
            .then(response => {
                if (!response.ok) throw new Error("Failed to toggle like");
                return response.json();
            })
            .then(updatedImage => {
                updateLikesOnCard(updatedImage.id, updatedImage.likesCount);
            })
            .catch(console.error);
    }

    // モーダルを開く処理
    function openModal(imageUrl, description) {
        modalImage.src = imageUrl;
        modalDescription.textContent = description;
        modal.style.display = "block";
    }

    // モーダルを閉じる処理
    closeModal.addEventListener("click", function () {
        modal.style.display = "none";
    });

    window.addEventListener("click", function (event) {
        if (event.target === modal) {
            modal.style.display = "none";
        }
    });

    // ギャラリーを読み込む
    function loadGallery() {
        fetch('/api/proud/images')
            .then(response => response.json())
            .then(images => {
                gallery.innerHTML = '';
                addedImageIds.clear();
                images.forEach(image => addImageToGallery(image));
            })
            .catch(console.error);
    }

    // 画像アップロード
    if (uploadForm) {
        uploadForm.addEventListener("submit", function (e) {
            e.preventDefault();
            const formData = new FormData(uploadForm);

            fetch('/api/proud/upload', {
                method: 'POST',
                body: formData,
            })
                .then(response => {
                    if (!response.ok) throw new Error("Failed to upload");
                    return response.text();
                })
                .catch(console.error);
        });
    }

    // 初期化処理
    fetchVisitorCounter();
    initializeWebSocket();
    loadGallery();
});
