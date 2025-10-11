document.addEventListener("DOMContentLoaded", async () => {
  // --- チームリスト（順番を指定） ---
  const alTeams = ["Tigers", "Guardians", "Mariners", "Red Sox", "Yankees", "Blue Jays"];
  const nlTeams = ["Reds","Dodgers", "Phillies", "Padres", "Cubs", "Brewers"];

  // --- MLB Stats APIからteamIdを取得してロゴURL生成 ---
  async function fetchTeamLogo(teamName) {
    try {
      const res = await fetch(`https://statsapi.mlb.com/api/v1/teams?sportId=1`);
      const data = await res.json();
      const team = data.teams.find(
        t =>
          t.teamName.toLowerCase() === teamName.toLowerCase() ||
          t.name.toLowerCase() === teamName.toLowerCase()
      );

      if (team && team.id) {
        return `https://www.mlbstatic.com/team-logos/${team.id}.svg`;
      } else {
        return "/images/mlb_placeholder.svg"; // デフォルト用（存在しないとき）
      }
    } catch (err) {
      console.error("Error fetching logo:", err);
      return "/images/mlb_placeholder.svg";
    }
  }

  // --- チームロゴだけの要素を生成 ---
  async function createTeamElement(teamName) {
    const div = document.createElement("div");
    div.className = "team";

    const img = document.createElement("img");
    img.src = await fetchTeamLogo(teamName);
    img.alt = teamName;

    div.appendChild(img);
    return div;
  }

  // --- チームリストを描画 ---
  async function renderTeams(containerId, teamList) {
    const container = document.getElementById(containerId);
    for (const team of teamList) {
      container.appendChild(await createTeamElement(team));
    }
  }

  // --- AL / NL側の描画 ---
  await renderTeams("al-wildcard", alTeams);
  await renderTeams("nl-wildcard", nlTeams);

  // --- World Series中央ロゴ ---
  const ws = document.getElementById("world-series");
  const wsImg = document.createElement("img");
  wsImg.src = "/images/worldseries2025.png";
  wsImg.alt = "World Series 2025";
  wsImg.style.width = "116px";
  wsImg.style.margin = "0 auto";
  wsImg.style.display = "block";
  ws.appendChild(wsImg);
});
