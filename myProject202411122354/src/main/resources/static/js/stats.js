document.addEventListener("DOMContentLoaded", async () => {
    try {
        const response = await fetch("/api/stats");
        if (!response.ok) {
            throw new Error(`Failed to load stats: ${response.status}`);
        }

        const [hitterStatsData, pitcherStatsData] = await response.json();

        // 打者成績（存在する場合のみ）
        const gamesPlayedEl = document.getElementById("gamesPlayed");
        if (gamesPlayedEl) {
        const hitterStats = hitterStatsData.stats[0].splits[0].stat;
        gamesPlayedEl.textContent = hitterStats.gamesPlayed || "N/A";
        document.getElementById("avg").textContent = hitterStats.avg || "N/A";
        document.getElementById("homeRuns").textContent = hitterStats.homeRuns || "N/A";
        document.getElementById("stolenBases").textContent = hitterStats.stolenBases || "N/A";
        document.getElementById("rbi").textContent = hitterStats.rbi || "N/A";
        document.getElementById("ops").textContent = hitterStats.ops || "N/A";
        }

        // 投手成績を表示
        const pitcherStats = pitcherStatsData.stats[0].splits[0].stat;
        document.getElementById("pitcherGamesPlayed").textContent = pitcherStats.gamesPlayed || "N/A";
        document.getElementById("wins").textContent = pitcherStats.wins || "N/A";
        document.getElementById("losses").textContent = pitcherStats.losses || "N/A";
        document.getElementById("era").textContent = pitcherStats.era || "N/A";
        document.getElementById("strikeOuts").textContent = pitcherStats.strikeOuts || "N/A";

    } catch (error) {
        console.error("Error loading stats:", error);
    }
});
