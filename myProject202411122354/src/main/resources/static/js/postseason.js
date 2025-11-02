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
    } catch (e) {
        console.error("Error fetching series results:", e);
    }
});

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

// ✅ Postseason成績ロード処理
async function loadPostseasonStats() {
    try {
        const response = await fetch("/api/postseason/stats");
        const statsData = await response.json();
        console.log("Postseason Stats (REST版):", statsData);

        // ✅ 対象選手定義
        const players = [
            { key: "ohtani", name: "大谷翔平", img: "/images/PostSeason-Stats_IMG/PostSeason-Ohtani.png" },
            { key: "yamamoto", name: "山本由伸", img: "/images/PostSeason-Stats_IMG/PostSeason-Yamamoto.png" },
            { key: "sasaki", name: "佐々木朗希", img: "/images/PostSeason-Stats_IMG/PostSeason-Sasaki.png" }
        ];

        // ✅ 打撃・投手データを個別に抽出
        const ohtaniHit = statsData.ohtaniHitting?.stats?.[0]?.splits?.[0]?.stat || {};
        const ohtaniPitch = statsData.ohtaniPitching?.stats?.[0]?.splits?.[0]?.stat || {};
        const yamamotoPitch = statsData.yamamotoPitching?.stats?.[0]?.splits?.[0]?.stat || {};
        const sasakiPitch = statsData.sasakiPitching?.stats?.[0]?.splits?.[0]?.stat || {};

        // --- 大谷翔平（打撃カード） ---
        document.getElementById("batting-stats").insertAdjacentHTML(
            "beforeend",
            `
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
            </div>`
        );

        // --- 大谷翔平（投手カード） ---
        document.getElementById("pitching-stats").insertAdjacentHTML(
            "beforeend",
            `
            <div class="player-card">
                <div class="player-header">
                    <img src="${players[0].img}" alt="${players[0].name}" class="player-photo"/>
                    <h3>${players[0].name}</h3>
                </div>
                <div class="stats-row">
                    <div>ERA<br><span>${ohtaniPitch.era || '-'}</span></div>
                    <div>IP<br><span>${ohtaniPitch.inningsPitched || '-'}</span></div>
                    <div>SO<br><span>${ohtaniPitch.strikeOuts || '-'}</span></div>
                    <div>WHIP<br><span>${ohtaniPitch.whip || '-'}</span></div>
                </div>
            </div>`
        );

        // --- 山本由伸（投手カード） ---
        document.getElementById("pitching-stats").insertAdjacentHTML(
            "beforeend",
            `
            <div class="player-card">
                <div class="player-header">
                    <img src="${players[1].img}" alt="${players[1].name}" class="player-photo"/>
                    <h3>${players[1].name}</h3>
                </div>
                <div class="stats-row">
                    <div>ERA<br><span>${yamamotoPitch.era || '-'}</span></div>
                    <div>IP<br><span>${yamamotoPitch.inningsPitched || '-'}</span></div>
                    <div>SO<br><span>${yamamotoPitch.strikeOuts || '-'}</span></div>
                    <div>WHIP<br><span>${yamamotoPitch.whip || '-'}</span></div>
                </div>
            </div>`
        );

        // --- 佐々木朗希（投手カード） ---
        document.getElementById("pitching-stats").insertAdjacentHTML(
            "beforeend",
            `
            <div class="player-card">
                <div class="player-header">
                    <img src="${players[2].img}" alt="${players[2].name}" class="player-photo"/>
                    <h3>${players[2].name}</h3>
                </div>
                <div class="stats-row">
                    <div>ERA<br><span>${sasakiPitch.era || '-'}</span></div>
                    <div>IP<br><span>${sasakiPitch.inningsPitched || '-'}</span></div>
                    <div>SO<br><span>${sasakiPitch.strikeOuts || '-'}</span></div>
                    <div>WHIP<br><span>${sasakiPitch.whip || '-'}</span></div>
                </div>
            </div>`
        );

    } catch (error) {
        console.error("Error loading postseason stats:", error);
    }
}
