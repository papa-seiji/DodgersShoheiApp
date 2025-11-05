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
        await loadPostseasonStats();

        // ✅ URLハッシュ対応（ニュースリンクからのジャンプ）
        handleHashNavigation();

        // ✅ フェードスライド開始
        initializeFadeSlideshow();

        // ✅ 🔇／🔊 ミュート + 音量制御（★ 改良版 ★）
        const video = document.getElementById("postseasonVideo");
        const muteBtn = document.getElementById("muteToggle");

        if (video && muteBtn) {
            // 初期音量を30%に設定（0.0〜1.0）
            video.volume = 0.04;
            console.log("初期音量を30%に設定しました。");

            muteBtn.addEventListener("click", () => {
                video.muted = !video.muted;
                muteBtn.textContent = video.muted ? "🔇" : "🔊";
            });

            // 🔊 長押しで音量アップ・ダウンを追加（オプション）
            muteBtn.addEventListener("contextmenu", (e) => {
                e.preventDefault();
                video.volume = Math.max(0, video.volume - 0.1);
                console.log("音量ダウン:", Math.round(video.volume * 100) + "%");
            });

            muteBtn.addEventListener("dblclick", (e) => {
                e.preventDefault();
                video.volume = Math.min(1, video.volume + 0.1);
                console.log("音量アップ:", Math.round(video.volume * 100) + "%");
            });

            console.log("ミュートボタン制御＋音量調整を初期化しました。");
        } else {
            console.warn("動画またはミュートボタンが見つかりません。");
        }

    } catch (e) {
        console.error("Error fetching series results:", e);
    }
});

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
        "hoge-hoge_landing6": "nlcs"
    };

    const tabId = mapping[targetId];
    if (tabId) {
        // 対応するタブをアクティブ化
        const tabs = document.querySelectorAll(".tab-button");
        const contents = document.querySelectorAll(".tab-content");

        tabs.forEach(t => t.classList.remove("active"));
        contents.forEach(c => c.classList.remove("active"));

        const targetTab = document.querySelector(`.tab-button[data-tab="${tabId}"]`);
        if (targetTab) targetTab.classList.add("active");

        const targetContent = document.getElementById(tabId);
        if (targetContent) targetContent.classList.add("active");

        // スクロール実行（若干遅延を入れる）
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

        // ✅ ステータスコードチェック
        if (!response.ok) {
            throw new Error(`APIエラー: ${response.status}`);
        }

        const statsData = await response.json();
        console.log("Postseason Stats (REST版):", statsData);

        // ✅ データが空・不正ならフォールバック
        if (!statsData || Object.keys(statsData).length === 0 || !statsData.ohtaniHitting) {
            throw new Error("APIデータが空のためフォールバックを使用します");
        }

        renderPostseasonStats(statsData);

    } catch (error) {
        console.warn("API取得失敗、手動データで表示します:", error);

        // ✅ 手動データ（フォールバック）
        const manualStats = {
            ohtaniHitting: { avg: ".265", homeRuns: 8, rbi: 14, ops: "1.096" },
            ohtaniPitching: { era: "4.43", inningsPitched: "20.1", strikeOuts: 28, whip: "1.13" },
            yamamotoPitching: { era: "1.45", inningsPitched: "37.1", strikeOuts: 33, whip: "0.78" },
            sasakiPitching: { era: "0.84", inningsPitched: "10.2", strikeOuts: 6, whip: "1.03" }
        };

        renderPostseasonStats(manualStats);
    }
}

// ✅ 共通描画関数（API形式・手動形式どちらにも対応）
function renderPostseasonStats(statsData) {
    const players = [
        { key: "ohtani", name: "大谷翔平", img: "/images/PostSeason-Stats_IMG/PostSeason-Ohtani.png" },
        { key: "yamamoto", name: "山本由伸", img: "/images/PostSeason-Stats_IMG/PostSeason-Yamamoto.png" },
        { key: "sasaki", name: "佐々木朗希", img: "/images/PostSeason-Stats_IMG/PostSeason-Sasaki.png" }
    ];

    // ✅ 打撃データ（API形式 or 手動形式 両対応）
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

    // ✅ 投手データ（API形式 or 手動形式 両対応）
    const pitchers = [
        { data: statsData.ohtaniPitching?.stats?.[0]?.splits?.[0]?.stat || statsData.ohtaniPitching || {}, player: players[0] },
        { data: statsData.yamamotoPitching?.stats?.[0]?.splits?.[0]?.stat || statsData.yamamotoPitching || {}, player: players[1] },
        { data: statsData.sasakiPitching?.stats?.[0]?.splits?.[0]?.stat || statsData.sasakiPitching || {}, player: players[2] }
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
