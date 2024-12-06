document.addEventListener("DOMContentLoaded", function () {
    const uploadForm = document.getElementById("image-upload-form");
    const visitorCounter = document.getElementById("visitor-counter-value");
    const gallery = document.getElementById("image-gallery");
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
                addImageToGallery(newImage, true); // WebSocketからの更新
            });

            stompClient.subscribe('/topic/visitorCounter', function (message) {
                const newVisitorCount = JSON.parse(message.body);
                if (visitorCounter) {
                    visitorCounter.textContent = newVisitorCount;
                }
            });
        });
    }

    
    // Visitorカウンターの取得
    function fetchVisitorCounter() {
        fetch('/api/visitorCounter')
            .then(response => response.json())
            .then(count => {
                if (visitorCounter) {
                    visitorCounter.textContent = count;
                }
            })
            .catch(error => console.error("Error fetching visitor counter:", error));
    }

    // ギャラリーに画像を追加
    const addedImageIds = new Set(); // 重複チェック用
    function addImageToGallery(image, isWebSocketUpdate = false) {
        if (addedImageIds.has(image.id)) {
            if (isWebSocketUpdate) {
                console.warn("Duplicate image received via WebSocket:", image);
            }
            return;
        }
        addedImageIds.add(image.id);

        const card = document.createElement("div");
        card.classList.add("card");

        const img = document.createElement("img");
        img.src = image.imageUrl;
        img.alt = image.description;

        const description = document.createElement("p");
        description.textContent = image.description;

        const createdBy = document.createElement("small");
        createdBy.textContent = `Posted by: ${image.createdBy} at ${new Date(image.createdAt).toLocaleString()}`;

        card.appendChild(img);
        card.appendChild(description);
        card.appendChild(createdBy);

        gallery.appendChild(card);
    }

    // ギャラリーを読み込む
    function loadGallery() {
        console.log("Loading gallery...");
        fetch('/api/proud/images')
            .then(response => response.json())
            .then(images => {
                gallery.innerHTML = ''; // 初期化
                addedImageIds.clear(); // 重複チェック用リセット
                images.forEach(image => addImageToGallery(image));
                console.log("Gallery loaded successfully.");
            })
            .catch(error => console.error("Error loading gallery:", error));
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
                    if (!response.ok) {
                        throw new Error("Failed to upload");
                    }
                    return response.text();
                })
                .then(message => {
                    console.log(message);
                })
                .catch(error => console.error("Error uploading image:", error));
        });
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
    
        // ギャラリー画像クリックイベントを追加
        gallery.addEventListener("click", function (event) {
            if (event.target.tagName === "IMG") {
                const imageUrl = event.target.src;
                const description = event.target.nextElementSibling?.textContent || "";
                openModal(imageUrl, description);
            }
        });




    // 初期化処理
    fetchVisitorCounter();
    initializeWebSocket();
    loadGallery();
});
