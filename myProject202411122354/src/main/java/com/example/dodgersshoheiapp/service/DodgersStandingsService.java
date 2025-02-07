package com.example.dodgersshoheiapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class DodgersStandingsService {
    // レギュラーシーズン
    private static final String API_URL = "https://statsapi.mlb.com/api/v1/standings?leagueId=104&division=203&season=2024&standingsTypes=regularSeason";

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

    public List<Map<String, Object>> getDodgersStandings() {
        Map<String, Object> response = restTemplate.getForObject(API_URL, Map.class);
        List<Map<String, Object>> records = (List<Map<String, Object>>) response.get("records");

        List<Map<String, Object>> standings = new ArrayList<>();

        if (records != null) {
            for (Map<String, Object> record : records) {
                List<Map<String, Object>> teams = (List<Map<String, Object>>) record.get("teamRecords");

                for (Map<String, Object> team : teams) {
                    Map<String, Object> teamData = new HashMap<>();
                    Map<String, Object> teamInfo = (Map<String, Object>) team.get("team");

                    teamData.put("rank", team.get("divisionRank"));
                    teamData.put("name", teamInfo.get("name"));
                    teamData.put("wins", team.get("wins"));
                    teamData.put("losses", team.get("losses"));
                    teamData.put("winPercentage", team.get("winPercentage"));
                    teamData.put("gamesBack", team.get("gamesBack"));
                    teamData.put("teamId", teamInfo.get("id")); // チームロゴ取得用

                    standings.add(teamData);
                }
            }
        }
        return standings;
    }
}
