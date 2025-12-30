document.addEventListener("DOMContentLoaded", function () {
    const params = new URLSearchParams(window.location.search);

    if (params.get("login") === "success") {

        const toast = document.getElementById("login-toast");
        const message = document.getElementById("login-toast-message");

        if (!toast || !message) return;

        const username = window.LOGIN_USERNAME;

message.innerHTML = username
    ? `${username}さん、<br>参加ありがとうございます！`
    : "参加ありがとうございます！";

        toast.classList.remove("hidden");
        toast.classList.add("show");

        setTimeout(() => {
            toast.classList.remove("show");
            toast.classList.add("hidden");
        }, 10000);

        history.replaceState(null, "", "/home");
    }
});
