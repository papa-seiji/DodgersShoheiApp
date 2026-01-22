let stompClient = null;

function connectTournamentWS() {
  const socket = new SockJS("/ws");
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log("[WS] connected");

    stompClient.subscribe("/topic/wbc/tournament", () => {
      console.log("[WS] update received");
      window.redrawTournament(); // ← ここだけ呼ぶ
    });
  });
}

document.addEventListener("DOMContentLoaded", connectTournamentWS);
