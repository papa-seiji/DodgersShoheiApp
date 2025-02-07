package com.example.dodgersshoheiapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DodgersStandingsService {
    private static final String API_URL = "https://statsapi.mlb.com/api/v1/standings?leagueId=103,104&season=2024&standingsTypes=regularSeason";
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

    public Map<String, List<Map<String, Object>>> getDodgersStandings() {
        Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);
        List<Map<String, Object>> records = (List<Map<String, Object>>) response.get("records");

        // 地区ごとのデータを格納
        Map<String, List<Map<String, Object>>> standingsByDivision = new LinkedHashMap<>();
        standingsByDivision.put("NL 東地区", new ArrayList<>());
        standingsByDivision.put("NL 中地区", new ArrayList<>());
        standingsByDivision.put("NL 西地区", new ArrayList<>());

        // 地区ごとの teamId リスト
        Set<Integer> eastTeams = Set.of(143, 144, 121, 120, 146);
        Set<Integer> centralTeams = Set.of(158, 112, 138, 113, 134);
        Set<Integer> westTeams = Set.of(119, 135, 109, 137, 115);

        if (records != null) {
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
                        standingsByDivision.get("NL 東地区").add(teamData);
                    } else if (centralTeams.contains(teamId)) {
                        standingsByDivision.get("NL 中地区").add(teamData);
                    } else if (westTeams.contains(teamId)) {
                        standingsByDivision.get("NL 西地区").add(teamData);
                    }
                }
            }
        }
        return standingsByDivision;
    }
}
