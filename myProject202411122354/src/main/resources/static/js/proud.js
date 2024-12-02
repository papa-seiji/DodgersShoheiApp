document.addEventListener("DOMContentLoaded", function () {
    const gallery = document.getElementById("image-gallery");
    const uploadForm = document.getElementById("image-upload-form");
    const visitorCounter = document.getElementById("visitor-counter-value");
    const customSubmitButton = document.getElementById("custom-submit-button");
    let stompClient = null;

    // WebSocket初期化
    function initializeWebSocket() {
        console.log("Initializing WebSocket...");
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function () {
            console.log("WebSocket connected.");
            stompClient.subscribe('/topic/proudGallery', function (message) {
                console.log("Received new image via WebSocket:", message.body);
                const newImage = JSON.parse(message.body);
                addImageToGallery(newImage);
            });

            stompClient.subscribe('/topic/visitorCounter', function (message) {
                const newVisitorCount = JSON.parse(message.body);
                if (visitorCounter) {
                    visitorCounter.textContent = newVisitorCount;
                }
                console.log("Updated Visitor Counter:", newVisitorCount);
            });
        }, function (error) {
            console.error("WebSocket connection error:", error);
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
    function addImageToGallery(image) {
        const card = document.createElement("div");
        card.classList.add("card");

        const img = document.createElement("img");
        img.src = `/api/proud/files/${image.imageUrl.split('/').pop()}`;
        img.alt = image.description;

        const description = document.createElement("p");
        description.textContent = image.description;

        const createdBy = document.createElement("small");
        createdBy.textContent = `Posted by: ${image.createdBy}`;

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
                gallery.innerHTML = '';
                images.forEach(addImageToGallery);
                console.log("Gallery loaded successfully.");
            })
            .catch(error => console.error("Error loading gallery:", error));
    }

    // 画像アップロード
    if (uploadForm) {
        uploadForm.addEventListener("submit", function (e) {
            e.preventDefault();
            console.log("Uploading image...");

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
                    loadGallery();
                })
                .catch(error => console.error("Error uploading image:", error));
        });


        // if (customSubmitButton) {
        //     customSubmitButton.addEventListener("click", function (e) {
        //         e.preventDefault(); // デフォルトのリンク動作を防ぐ
        //         if (uploadForm) {
        //             uploadForm.submit(); // フォームをプログラム的に送信
        //         }
        //     });
        // }
    
    }

    // 初期化処理
    fetchVisitorCounter();
    initializeWebSocket();
    loadGallery();
});
