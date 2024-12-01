document.addEventListener("DOMContentLoaded", function () {
    const uploadForm = document.getElementById("image-upload-form");
    const gallery = document.getElementById("image-gallery");

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
                    alert(message);
                    loadGallery();
                })
                .catch(error => console.error("Error uploading image:", error));
        });
    }

    // ギャラリー表示
    function loadGallery() {
        fetch('/api/proud/images')
            .then(response => response.json())
            .then(images => {
                gallery.innerHTML = '';
                images.forEach(image => {
                    const card = document.createElement("div");
                    card.classList.add("card");

                    const img = document.createElement("img");
                    img.src = image.imageUrl;
                    img.alt = image.description;

                    const description = document.createElement("p");
                    description.textContent = image.description;

                    const createdBy = document.createElement("small");
                    createdBy.textContent = `Posted by: ${image.createdBy}`;

                    card.appendChild(img);
                    card.appendChild(description);
                    card.appendChild(createdBy);

                    gallery.appendChild(card);
                });
            })
            .catch(error => console.error("Error loading gallery:", error));
    }

    loadGallery();
});
