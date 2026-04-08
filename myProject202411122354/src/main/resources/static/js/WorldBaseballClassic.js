document.addEventListener("DOMContentLoaded", async () => {
    try {
        // ✅ シリーズ結果をロード
        const res = await fetch("/api/mlb/series-results");
        const data = await res.json();

        for (const key in data) {
            const el = document.getElementById(key);
            if (el) el.textContent = data[key];
        }
        console.log("シリーズデータを取得しました:", data);

        // ✅ タブ切り替え機能
        initializeTabs();

        // ✅ Postseason成績をロード
        // await
        loadPostseasonStats();

        // ✅ URLハッシュ対応（ニュースリンクからのジャンプ）
        handleHashNavigation();

        // ✅ フェードスライド開始
        initializeFadeSlideshow();

        // ✅ 🔇／🔊 ミュート + 音量制御（★ 改良版 ★）
        setupMuteControl("videoWBC", "muteWBC");
        setupMuteControl("videoWBC23", "muteWBC23");
        setupMuteControl("videoChampion", "muteChampion");

// ✅ 🎬 すべての動画を確実に再生（新規追記済み）
// ensureVideoPlayback(["videoWBC", "videoWBC23", "videoChampion"]);

        // ✅ 🎬 すべての動画を確実に再生（← ここを新規追記！）
        // ensureVideoPlayback(["videoWBC", "videoChampion","videoWBC23"]);

    } catch (e) {
        console.error("Error fetching series results:", e);
    }
});

// ✅ 改良版：全動画ミュート対応＋ボタン状態同期
function setupMuteControl(videoId, buttonId, initialVolume = 0.04) {
  const video = document.getElementById(videoId);
  const button = document.getElementById(buttonId);

  if (!video || !button) {
    console.warn(`❌ ${videoId} または ${buttonId} が見つかりません`);
    return;
  }

  video.volume = initialVolume;
  video.muted = true; // 初期状態ミュート
  button.textContent = "🔇";

  button.addEventListener("click", () => {
    const allVideos = document.querySelectorAll("video.decorative-video");
    const allButtons = document.querySelectorAll(".mute-btn");

    // 🧩 もし現在ミュート状態 → ミュート解除（他をミュート）
    if (video.muted) {
      allVideos.forEach((v, i) => {
        v.muted = true;
        if (allButtons[i]) allButtons[i].textContent = "🔇";
      });
      video.muted = false;
      button.textContent = "🔊";
      console.log(`🎵 ${videoId} の音声を有効化。他の動画はミュートにしました。`);

    // 🧩 もしすでに解除状態 → 今回クリックでミュートに戻す
    } else {
      video.muted = true;
      button.textContent = "🔇";
      console.log(`🔇 ${videoId} をミュートに戻しました。`);
    }
  });

  // 🔉右クリックで音量DOWN
  button.addEventListener("contextmenu", (e) => {
    e.preventDefault();
    video.volume = Math.max(0, video.volume - 0.1);
    console.log(`🔽 ${videoId} 音量: ${Math.round(video.volume * 100)}%`);
  });

  // 🔊ダブルクリックで音量UP
  button.addEventListener("dblclick", (e) => {
    e.preventDefault();
    video.volume = Math.min(1, video.volume + 0.1);
    console.log(`🔼 ${videoId} 音量: ${Math.round(video.volume * 100)}%`);
  });
}


// ✅ 複数動画の自動再生保証（ブラウザのautoplay制限回避）
// function ensureVideoPlayback(videoIds) {
//   videoIds.forEach(id => {
//     const video = document.getElementById(id);
//     if (!video) return;

//     // 自動再生がブロックされていた場合にtry-catchで再試行
//     const playPromise = video.play();
//     if (playPromise !== undefined) {
//       playPromise
//         .then(() => console.log(`▶ ${id} 再生開始`))
//         .catch(err => {
//           console.warn(`⚠ ${id} の自動再生がブロックされました。ユーザー操作で再開します。`);
          // 初回クリック時に再生開始
//           document.body.addEventListener(
//             "click",
//             () => {
//               video.play().then(() => console.log(`✅ ${id} 再生を手動開始`));
//             },
//             { once: true }
//           );
//         });
//     }
//   });
// }


// ✅ フェードスライド関数（新規追加）
function initializeFadeSlideshow() {
    const images = document.querySelectorAll(".fade-image");
    let currentIndex = 0;

    if (images.length > 0) {
        setInterval(() => {
            images[currentIndex].classList.remove("active");
            currentIndex = (currentIndex + 1) % images.length;
            images[currentIndex].classList.add("active");
        }, 3800); // 3.8秒ごとに切替
    }
}

// ✅ タブ切り替え
function initializeTabs() {
    const tabs = document.querySelectorAll(".tab-button");
    const contents = document.querySelectorAll(".tab-content");

    tabs.forEach(tab => {
        tab.addEventListener("click", () => {
            tabs.forEach(t => t.classList.remove("active"));
            contents.forEach(c => c.classList.remove("active"));

            tab.classList.add("active");
            document.getElementById(tab.dataset.tab).classList.add("active");
        });
    });
}

// ✅ ハッシュ遷移（/postseason#hoge-hoge_landingXX対応）
function handleHashNavigation() {
    const hash = window.location.hash;
    if (!hash) return;

    const targetId = hash.substring(1); // 例: hoge-hoge_landing12

    const mapping = {
        "hoge-hoge_landing7": "game1",
        "hoge-hoge_landing8": "game2",
        "hoge-hoge_landing9": "game3",
        "hoge-hoge_landing10": "game4",
        "hoge-hoge_landing11": "game5",
        "hoge-hoge_landing12": "game6",
        "hoge-hoge_landing13": "game7",
        "hoge-hoge_landing14": "game8",
        "hoge-hoge_landing6": "nlcs"
    };

    const tabId = mapping[targetId];
    if (tabId) {
        const tabs = document.querySelectorAll(".tab-button");
        const contents = document.querySelectorAll(".tab-content");

        tabs.forEach(t => t.classList.remove("active"));
        contents.forEach(c => c.classList.remove("active"));

        const targetTab = document.querySelector(`.tab-button[data-tab="${tabId}"]`);
        if (targetTab) targetTab.classList.add("active");

        const targetContent = document.getElementById(tabId);
        if (targetContent) targetContent.classList.add("active");

        setTimeout(() => {
            const target = document.getElementById(targetId);
            if (target) target.scrollIntoView({ behavior: "smooth", block: "start" });
        }, 400);
    }
}

// ✅ Postseason成績ロード処理（API優先・手動フォールバック対応）
async function loadPostseasonStats() {
    try {
        const response = await fetch("/api/postseason/stats");

        if (!response.ok) {
            throw new Error(`APIエラー: ${response.status}`);
        }

        const statsData = await response.json();
        console.log("Postseason Stats (REST版):", statsData);

        if (!statsData || Object.keys(statsData).length === 0 || !statsData.ohtaniHitting) {
            throw new Error("APIデータが空のためフォールバックを使用します");
        }

        renderPostseasonStats(statsData);

    } catch (error) {
        console.warn("API取得失敗、手動データで表示します:", error);

        const manualStats = {
            ohtaniHitting: { avg: ".462", homeRuns: 3, rbi: 7, ops: "1.842" },
            // ohtaniPitching: { era: "1.86", inningsPitched: "9 2/3", strikeOuts: 11, whip: "0.152" },
            yamamotoPitching: { era: "2.70", inningsPitched: "6 2/3", strikeOuts: 7, whip: "1.2" },
            // sasakiPitching: { era: "3.52", inningsPitched: "7 2/3", strikeOuts: 11, whip: "0.233" }
        };

        renderPostseasonStats(manualStats);
    }
}

// ✅ 共通描画関数（API形式・手動形式どちらにも対応）
function renderPostseasonStats(statsData) {
    const players = [
        { key: "ohtani", name: "大谷翔平", img: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/2026WBC_Ohtani.jpg" },
        { key: "yamamoto", name: "山本由伸", img: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/2026WBC_Yoshinobu.jpg" },
        { key: "sasaki", name: "佐々木朗希", img: "https://dodgersshoheiapp-assets.s3.us-east-1.amazonaws.com/img/2026_WBC/2026WBC_Rouki.jpg" }
    ];

    const ohtaniHit =
        statsData.ohtaniHitting?.stats?.[0]?.splits?.[0]?.stat ||
        statsData.ohtaniHitting ||
        {};

    document.getElementById("batting-stats").innerHTML = `
        <div class="player-card">
            <div class="player-header">
                <img src="${players[0].img}" alt="${players[0].name}" class="player-photo"/>
                <h3>${players[0].name}</h3>
            </div>
            <div class="stats-row">
                <div>AVG<br><span>${ohtaniHit.avg || '-'}</span></div>
                <div>HR<br><span>${ohtaniHit.homeRuns || '-'}</span></div>
                <div>RBI<br><span>${ohtaniHit.rbi || '-'}</span></div>
                <div>OPS<br><span>${ohtaniHit.ops || '-'}</span></div>
            </div>
        </div>
    `;

    const pitchers = [
        // { data: statsData.ohtaniPitching?.stats?.[0]?.splits?.[0]?.stat || statsData.ohtaniPitching || {}, player: players[0] },
        { data: statsData.yamamotoPitching?.stats?.[0]?.splits?.[0]?.stat || statsData.yamamotoPitching || {}, player: players[1] },
        // { data: statsData.sasakiPitching?.stats?.[0]?.splits?.[0]?.stat || statsData.sasakiPitching || {}, player: players[2] }
    ];

    document.getElementById("pitching-stats").innerHTML = pitchers.map(p => `
        <div class="player-card">
            <div class="player-header">
                <img src="${p.player.img}" alt="${p.player.name}" class="player-photo"/>
                <h3>${p.player.name}</h3>
            </div>
            <div class="stats-row">
                <div>ERA<br><span>${p.data.era || '-'}</span></div>
                <div>IP<br><span>${p.data.inningsPitched || '-'}</span></div>
                <div>SO<br><span>${p.data.strikeOuts || '-'}</span></div>
                <div>WHIP<br><span>${p.data.whip || '-'}</span></div>
            </div>
        </div>
    `).join('');
}
