package com.example.dodgersshoheiapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class DodgersStandingsService {
    private static final String API_URL_NL = "https://statsapi.mlb.com/api/v1/standings?leagueId=104&season=2024&standingsTypes=regularSeason";
    private static final String API_URL_AL = "https://statsapi.mlb.com/api/v1/standings?leagueId=103&season=2024&standingsTypes=regularSeason";

    // 他のモードの URL (切り替え用)
    // private static final String SPRING_TRAINING_API_URL =
    // "https://statsapi.mlb.com/api/v1/standings?leagueId=104&division=203&season=2024&standingsTypes=springTraining";
    // private static final String POSTSEASON_API_URL =
    // "https://statsapi.mlb.com/api/v1/standings?leagueId=104&division=203&season=2024&standingsTypes=postseason";
    // private static final String DIVISION_SERIES_API_URL =
    // "https://statsapi.mlb.com/api/v1/standings?leagueId=104&division=203&season=2024&standingsTypes=divisionSeries";
    // private static final String WORLD_SERIES_API_URL =
    // "https://statsapi.mlb.com/api/v1/standings?leagueId=104&division=203&season=2024&standingsTypes=worldSeries";

    private final RestTemplate restTemplate;

    public DodgersStandingsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, List<Map<String, Object>>> getMLBStandings() {
        Map<String, List<Map<String, Object>>> standingsByLeague = new LinkedHashMap<>();

        // NL & AL のデータ取得
        processLeagueData(API_URL_NL, standingsByLeague, "NL");
        processLeagueData(API_URL_AL, standingsByLeague, "AL");

        return standingsByLeague;
    }

    private void processLeagueData(String apiUrl, Map<String, List<Map<String, Object>>> standingsByLeague,
            String leaguePrefix) {
        Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
        List<Map<String, Object>> records = (List<Map<String, Object>>) response.get("records");

        if (records == null)
            return;

        // **修正：チームIDで正しく地区を振り分ける**
        Set<Integer> eastTeams = leaguePrefix.equals("NL")
                ? Set.of(144, 146, 121, 143, 120) // **NL 東地区**
                : Set.of(110, 111, 147, 139, 141); // **AL 東地区**

        Set<Integer> centralTeams = leaguePrefix.equals("NL")
                ? Set.of(112, 113, 158, 134, 138) // **NL 中地区**
                : Set.of(145, 114, 116, 118, 142); // **AL 中地区（修正済み！）**

        Set<Integer> westTeams = leaguePrefix.equals("NL")
                ? Set.of(109, 115, 119, 135, 137) // **NL 西地区**
                : Set.of(117, 108, 133, 136, 140); // **AL 西地区（Astros 修正済み！）**

        standingsByLeague.put(leaguePrefix + " 東地区", new ArrayList<>());
        standingsByLeague.put(leaguePrefix + " 中地区", new ArrayList<>());
        standingsByLeague.put(leaguePrefix + " 西地区", new ArrayList<>());

        for (Map<String, Object> record : records) {
            List<Map<String, Object>> teams = (List<Map<String, Object>>) record.get("teamRecords");

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
