document.addEventListener("DOMContentLoaded", async () => {

    let currentYear = "2026";   // デフォルト2026

    async function loadStats(year) {
        try {
            const response = await fetch(`/api/stats?year=${year}`);
            if (!response.ok) {
                throw new Error(`Failed to load stats: ${response.status}`);
            }

            const [hitterStatsData, pitcherStatsData] = await response.json();

            const hitterSplit = hitterStatsData?.stats?.[0]?.splits?.[0];
            const pitcherSplit = pitcherStatsData?.stats?.[0]?.splits?.[0];

            // ===== 打者 =====
            if (hitterSplit) {
                const hitterStats = hitterSplit.stat;
                document.getElementById("gamesPlayed").textContent = hitterStats.gamesPlayed || "N/A";
                document.getElementById("avg").textContent = hitterStats.avg || "N/A";
                document.getElementById("homeRuns").textContent = hitterStats.homeRuns || "N/A";
                document.getElementById("stolenBases").textContent = hitterStats.stolenBases || "N/A";
                document.getElementById("rbi").textContent = hitterStats.rbi || "N/A";
                document.getElementById("ops").textContent = hitterStats.ops || "N/A";
            } else {
                document.querySelectorAll("#hitter-stats span").forEach(el => el.textContent = "—");
            }

            // ===== 投手 =====
            if (pitcherSplit) {
                const pitcherStats = pitcherSplit.stat;
                document.getElementById("pitcherGamesPlayed").textContent = pitcherStats.gamesPlayed || "N/A";
                document.getElementById("wins").textContent = pitcherStats.wins || "N/A";
                document.getElementById("losses").textContent = pitcherStats.losses || "N/A";
                document.getElementById("era").textContent = pitcherStats.era || "N/A";
                document.getElementById("strikeOuts").textContent = pitcherStats.strikeOuts || "N/A";
            } else {
                document.querySelectorAll("#pitcher-stats span").forEach(el => el.textContent = "—");
            }

        } catch (error) {
            console.error("Error loading stats:", error);
        }
    }

    // 🔥 年を全部のタブに反映させる関数
    function syncTabs(year) {

        document.querySelectorAll(".season-tab").forEach(tab => {
            if (tab.dataset.year === year) {
                tab.classList.add("active");
            } else {
                tab.classList.remove("active");
            }
        });

        document.getElementById("SeasonStats-1").textContent = year + " Stats";
        document.getElementById("SeasonStats-2").textContent = year + " Stats";
    }

    // 初期表示
    syncTabs(currentYear);
    await loadStats(currentYear);

    // 全タブ共通イベント
    document.querySelectorAll(".season-tab").forEach(tab => {

        tab.addEventListener("click", async () => {

            currentYear = tab.dataset.year;

            syncTabs(currentYear);
            await loadStats(currentYear);

        });

    });

});