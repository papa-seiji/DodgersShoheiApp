package com.example.dodgersshoheiapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class DodgersStandingsService {

    private final RestTemplate restTemplate;

    public DodgersStandingsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 🔥 year を受け取るように変更
    public Map<String, List<Map<String, Object>>> getMLBStandings(String year) {

        Map<String, List<Map<String, Object>>> standingsByLeague = new LinkedHashMap<>();

        // 🔥 season を動的に組み立て
        String apiUrlNL = "https://statsapi.mlb.com/api/v1/standings?leagueId=104&season="
                + year + "&standingsTypes=regularSeason";

        String apiUrlAL = "https://statsapi.mlb.com/api/v1/standings?leagueId=103&season="
                + year + "&standingsTypes=regularSeason";

        processLeagueData(apiUrlNL, standingsByLeague, "NL");
        processLeagueData(apiUrlAL, standingsByLeague, "AL");

        return standingsByLeague;
    }

    private void processLeagueData(String apiUrl,
            Map<String, List<Map<String, Object>>> standingsByLeague,
            String leaguePrefix) {

        Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
        if (response == null)
            return;

        List<Map<String, Object>> records = (List<Map<String, Object>>) response.get("records");

        if (records == null)
            return;

        Set<Integer> eastTeams = leaguePrefix.equals("NL")
                ? Set.of(144, 146, 121, 143, 120)
                : Set.of(110, 111, 147, 139, 141);

        Set<Integer> centralTeams = leaguePrefix.equals("NL")
                ? Set.of(112, 113, 158, 134, 138)
                : Set.of(145, 114, 116, 118, 142);

        Set<Integer> westTeams = leaguePrefix.equals("NL")
                ? Set.of(109, 115, 119, 135, 137)
                : Set.of(117, 108, 133, 136, 140);

        standingsByLeague.put(leaguePrefix + " 東地区", new ArrayList<>());
        standingsByLeague.put(leaguePrefix + " 中地区", new ArrayList<>());
        standingsByLeague.put(leaguePrefix + " 西地区", new ArrayList<>());

        for (Map<String, Object> record : records) {

            List<Map<String, Object>> teams = (List<Map<String, Object>>) record.get("teamRecords");

            if (teams == null)
                continue;

            for (Map<String, Object> team : teams) {

                Map<String, Object> teamData = new HashMap<>();
                Map<String, Object> teamInfo = (Map<String, Object>) team.get("team");

                int teamId = (int) teamInfo.get("id");

                teamData.put("rank", team.get("divisionRank"));
                teamData.put("name", teamInfo.get("name"));
                teamData.put("wins", team.get("wins"));
                teamData.put("losses", team.get("losses"));
                teamData.put("winPercentage", team.get("winPercentage"));
                teamData.put("gamesBack", team.get("gamesBack"));
                teamData.put("teamId", teamId);

                if (eastTeams.contains(teamId)) {
                    standingsByLeague.get(leaguePrefix + " 東地区").add(teamData);
                } else if (centralTeams.contains(teamId)) {
                    standingsByLeague.get(leaguePrefix + " 中地区").add(teamData);
                } else if (westTeams.contains(teamId)) {
                    standingsByLeague.get(leaguePrefix + " 西地区").add(teamData);
                }
            }
        }
    }
}