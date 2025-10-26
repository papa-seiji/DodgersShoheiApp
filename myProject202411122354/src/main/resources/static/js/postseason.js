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

        // ✅ Postseason成績をロード
        await loadPostseasonStats();
    } catch (e) {
        console.error("Error fetching series results:", e);
    }
});

async function loadPostseasonStats() {
    try {
        const response = await fetch("/api/postseason/stats");
        const statsData = await response.json();
        console.log("Postseason Stats (REST版):", statsData);

        // ✅ 対象選手定義
            const players = [
                {
                    key: "ohtani",
                    name: "大谷翔平",
                    img: "/images/PostSeason-Stats_IMG/PostSeason-Ohtani.png"
                },
                {
                    key: "yamamoto",
                    name: "山本由伸",
                    img: "/images/PostSeason-Stats_IMG/PostSeason-Yamamoto.png"
                },
                {
                    key: "sasaki",
                    name: "佐々木朗希",
                    img: "/images/PostSeason-Stats_IMG/PostSeason-Sasaki.png"
                }
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
