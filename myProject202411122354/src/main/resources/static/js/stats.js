document.addEventListener("DOMContentLoaded", async () => {

    let currentYear = "2026";   // デフォルト2026

    async function loadStats(year) {

        // 🔥 タイトル更新
            document.querySelectorAll(".stats-year-title").forEach(el => {
            el.textContent = `${year} Stats`;
        });

        try {
            const response = await fetch(`/api/stats?year=${year}`);
            if (!response.ok) {
                throw new Error(`Failed to load stats: ${response.status}`);
            }

            const [hitterStatsData, pitcherStatsData] = await response.json();

            const hitterSplit = hitterStatsData?.stats?.[0]?.splits?.[0];
            const pitcherSplit = pitcherStatsData?.stats?.[0]?.splits?.[0];

            // =========================
            // 打者
            // =========================
            if (hitterSplit) {
                const hitterStats = hitterSplit.stat;

                const hitterMap = {
                    gamesPlayed: hitterStats.gamesPlayed,
                    avg: hitterStats.avg,
                    homeRuns: hitterStats.homeRuns,
                    stolenBases: hitterStats.stolenBases,
                    rbi: hitterStats.rbi,
                    ops: hitterStats.ops
                };

                Object.keys(hitterMap).forEach(id => {
                    const el = document.getElementById(id);
                    if (el) el.textContent = hitterMap[id] || "N/A";
                });

            } else {
                document.querySelectorAll("#hitter-stats span")
                    .forEach(el => el.textContent = "—");
            }

            // =========================
            // 投手
            // =========================
            if (pitcherSplit) {
                const pitcherStats = pitcherSplit.stat;

                const pitcherMap = {
                    pitcherGamesPlayed: pitcherStats.gamesPlayed,
                    wins: pitcherStats.wins,
                    losses: pitcherStats.losses,
                    era: pitcherStats.era,
                    strikeOuts: pitcherStats.strikeOuts
                };

                Object.keys(pitcherMap).forEach(id => {
                    const el = document.getElementById(id);
                    if (el) el.textContent = pitcherMap[id] || "N/A";
                });

            } else {
                document.querySelectorAll("#pitcher-stats span")
                    .forEach(el => el.textContent = "—");
            }

        } catch (error) {
            console.error("Error loading stats:", error);
        }
    }

    // タブ同期
    function syncTabs(year) {
        document.querySelectorAll(".season-tab").forEach(tab => {
            tab.classList.toggle("active", tab.dataset.year === year);
        });
    }

    // 初期表示
    syncTabs(currentYear);
    await loadStats(currentYear);

    // タブイベント
    document.querySelectorAll(".season-tab").forEach(tab => {
        tab.addEventListener("click", async () => {
            currentYear = tab.dataset.year;
            syncTabs(currentYear);
            await loadStats(currentYear);
        });
    });

});