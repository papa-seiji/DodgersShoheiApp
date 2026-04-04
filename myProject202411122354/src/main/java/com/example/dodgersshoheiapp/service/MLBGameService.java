package com.example.dodgersshoheiapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;

@Service
public class MLBGameService {

    private final String BASE_URL = "https://statsapi.mlb.com/api/v1/schedule/games/";
    private final String SCHEDULE_URL = "https://statsapi.mlb.com/api/v1/schedule";

    /**
     * ★ 追加：日付から gamePk を取得する
     */
    public Long findGamePkByDate(String date) {

        // 追加修正///////////////////////////////////////////////////////////////////////////////////
        // 🔥 JST → MLB対策（追加）
        LocalDate inputDate = LocalDate.parse(date);
        LocalDate adjustedDate = inputDate.minusDays(1);
        date = adjustedDate.toString();
        // 追加修正///////////////////////////////////////////////////////////////////////////////////

        RestTemplate restTemplate = new RestTemplate();

        String url = UriComponentsBuilder
                .fromHttpUrl(SCHEDULE_URL)
                .queryParam("sportId", 1)
                .queryParam("teamId", 119) // Dodgers
                .queryParam("date", date)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("dates")) {
            return null;
        }

        List<Map<String, Object>> dates = (List<Map<String, Object>>) response.get("dates");

        if (dates.isEmpty()) {
            return null;
        }

        List<Map<String, Object>> games = (List<Map<String, Object>>) dates.get(0).get("games");

        if (games.isEmpty()) {
            return null;
        }
        // 修正前/////////////////////////////////////////////////////////////////////////////////////
        // Map<String, Object> game = games.get(0);

        // Object gamePkObj = game.get("gamePk");

        // if (gamePkObj instanceof Integer) {
        // return ((Integer) gamePkObj).longValue();
        // }

        // if (gamePkObj instanceof Long) {
        // return (Long) gamePkObj;
        // }

        // return null;
        // 修正前/////////////////////////////////////////////////////////////////////////////////////
        // 修正後/////////////////////////////////////////////////////////////////////////////////////
        // 🔥 Dodgersの試合を探す
        for (Map<String, Object> game : games) {

            Map<String, Object> teams = (Map<String, Object>) game.get("teams");

            Map<String, Object> home = (Map<String, Object>) teams.get("home");
            Map<String, Object> away = (Map<String, Object>) teams.get("away");

            Map<String, Object> homeTeam = (Map<String, Object>) home.get("team");
            Map<String, Object> awayTeam = (Map<String, Object>) away.get("team");

            Integer homeId = (Integer) homeTeam.get("id");
            Integer awayId = (Integer) awayTeam.get("id");

            // Dodgers = 119
            if (homeId == 119 || awayId == 119) {

                Object gamePkObj = game.get("gamePk");

                if (gamePkObj instanceof Integer) {
                    return ((Integer) gamePkObj).longValue();
                }

                if (gamePkObj instanceof Long) {
                    return (Long) gamePkObj;
                }
            }
        }

        // 見つからなかった場合
        return null;
        // 修正後/////////////////////////////////////////////////////////////////////////////////////
    }

    // 既存メソッドはそのまま
    public Map<String, Object> getFormattedGameInfo(String date) {

        RestTemplate restTemplate = new RestTemplate();
        String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("sportId", 1)
                .queryParam("teamId", 119)
                .queryParam("date", date)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("dates")) {
            return Collections.singletonMap("error", "試合情報が取得できませんでした");
        }

        List<Map<String, Object>> dates = (List<Map<String, Object>>) response.get("dates");

        if (dates.isEmpty()) {
            return Collections.singletonMap("error", "試合がありません");
        }

        List<Map<String, Object>> games = (List<Map<String, Object>>) dates.get(0).get("games");

        if (games.isEmpty()) {
            return Collections.singletonMap("error", "試合データなし");
        }

        Map<String, Object> game = games.get(0);
        Map<String, Object> awayTeam = (Map<String, Object>) ((Map<String, Object>) game.get("teams")).get("away");
        Map<String, Object> homeTeam = (Map<String, Object>) ((Map<String, Object>) game.get("teams")).get("home");

        Map<String, Object> formattedGame = new HashMap<>();
        formattedGame.put("date", game.get("gameDate"));
        formattedGame.put("status", game.get("status"));
        formattedGame.put("venue",
                ((Map<String, Object>) game.get("venue")).get("name"));
        formattedGame.put("away_team",
                ((Map<String, Object>) awayTeam.get("team")).get("name"));
        formattedGame.put("home_team",
                ((Map<String, Object>) homeTeam.get("team")).get("name"));
        formattedGame.put("away_score",
                awayTeam.getOrDefault("score", "N/A"));
        formattedGame.put("home_score",
                homeTeam.getOrDefault("score", "N/A"));

        return formattedGame;
    }

    // gamePk → playByPlay JSON が取れる
    public Map<String, Object> getPlayByPlay(Long gamePk) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://statsapi.mlb.com/api/v1/game/"
                + gamePk + "/playByPlay";

        return restTemplate.getForObject(url, Map.class);
    }

    ////////////////////////////// 対象日のHR数を取得する
    public int countHomeRuns(Long gamePk) {

        Map<String, Object> playByPlay = getPlayByPlay(gamePk);

        if (playByPlay == null)
            return 0;

        var allPlays = (java.util.List<Map<String, Object>>) playByPlay.get("allPlays");

        if (allPlays == null)
            return 0;

        int hrCount = 0;

        for (Map<String, Object> play : allPlays) {
            Map<String, Object> result = (Map<String, Object>) play.get("result");
            if (result != null) {
                String event = (String) result.get("event");
                if ("Home Run".equals(event)) {
                    hrCount++;
                }
            }
        }

        return hrCount;
    }

    ////////////////////////////// HRを打った選手名を取得する
    public java.util.List<String> getHomeRunHitters(Long gamePk) {

        Map<String, Object> playByPlay = getPlayByPlay(gamePk);
        if (playByPlay == null)
            return java.util.Collections.emptyList();

        var allPlays = (java.util.List<Map<String, Object>>) playByPlay.get("allPlays");
        if (allPlays == null)
            return java.util.Collections.emptyList();

        java.util.List<String> hitters = new java.util.ArrayList<>();

        for (Map<String, Object> play : allPlays) {
            Map<String, Object> result = (Map<String, Object>) play.get("result");
            if (result != null) {
                String event = (String) result.get("event");
                if ("Home Run".equals(event)) {

                    Map<String, Object> matchup = (Map<String, Object>) play.get("matchup");

                    if (matchup != null) {
                        Map<String, Object> batter = (Map<String, Object>) matchup.get("batter");

                        if (batter != null) {
                            hitters.add((String) batter.get("fullName"));
                        }
                    }
                }
            }
        }

        return hitters;
    }

    // HRの打球データを取得する
    // launchSpeed（打球速度）launchAngle（打球角度）totalDistance（飛距離）hitCoordinates（座標）
    public java.util.List<Map<String, Object>> getHomeRunDetails(Long gamePk) {

        Map<String, Object> playByPlay = getPlayByPlay(gamePk);
        if (playByPlay == null)
            return java.util.Collections.emptyList();

        var allPlays = (java.util.List<Map<String, Object>>) playByPlay.get("allPlays");
        if (allPlays == null)
            return java.util.Collections.emptyList();

        java.util.List<Map<String, Object>> hrDetails = new java.util.ArrayList<>();

        for (Map<String, Object> play : allPlays) {

            Map<String, Object> result = (Map<String, Object>) play.get("result");
            if (result == null)
                continue;

            String event = (String) result.get("event");
            if (!"Home Run".equals(event))
                continue;

            Map<String, Object> matchup = (Map<String, Object>) play.get("matchup");

            Map<String, Object> batter = matchup != null ? (Map<String, Object>) matchup.get("batter") : null;

            Map<String, Object> hr = new java.util.HashMap<>();

            if (batter != null) {
                hr.put("hitter", batter.get("fullName"));
            }

            // 🔥 ここが修正ポイント
            var playEvents = (java.util.List<Map<String, Object>>) play.get("playEvents");

            if (playEvents != null) {
                for (Map<String, Object> eventObj : playEvents) {

                    Map<String, Object> hitData = (Map<String, Object>) eventObj.get("hitData");

                    if (hitData != null) {
                        hr.put("launchSpeed", hitData.get("launchSpeed"));
                        hr.put("launchAngle", hitData.get("launchAngle"));
                        hr.put("totalDistance", hitData.get("totalDistance"));
                    }
                }
            }

            hrDetails.add(hr);
        }

        return hrDetails;
    }

    /////////////////////////////// Shohei専用抽出メソッド
    public List<Map<String, Object>> getShoheiHomeRuns(Long gamePk) {

        Map<String, Object> playByPlay = getPlayByPlay(gamePk);
        if (playByPlay == null)
            return Collections.emptyList();

        var allPlays = (List<Map<String, Object>>) playByPlay.get("allPlays");
        if (allPlays == null)
            return Collections.emptyList();

        List<Map<String, Object>> shoheiHRs = new ArrayList<>();

        for (Map<String, Object> play : allPlays) {

            Map<String, Object> result = (Map<String, Object>) play.get("result");
            if (result == null)
                continue;

            if (!"Home Run".equals(result.get("event")))
                continue;

            Map<String, Object> matchup = (Map<String, Object>) play.get("matchup");

            if (matchup == null)
                continue;

            Map<String, Object> batter = (Map<String, Object>) matchup.get("batter");

            if (batter == null)
                continue;

            String name = (String) batter.get("fullName");

            if (!"Shohei Ohtani".equals(name))
                continue;

            Map<String, Object> hr = new HashMap<>();
            hr.put("hitter", name);

            var playEvents = (List<Map<String, Object>>) play.get("playEvents");

            if (playEvents != null) {
                for (Map<String, Object> eventObj : playEvents) {
                    Map<String, Object> hitData = (Map<String, Object>) eventObj.get("hitData");

                    if (hitData != null) {
                        hr.put("launchSpeed", hitData.get("launchSpeed"));
                        hr.put("launchAngle", hitData.get("launchAngle"));
                        hr.put("totalDistance", hitData.get("totalDistance"));

                        // 🔥 ここ追加
                        Map<String, Object> coordinates = (Map<String, Object>) hitData.get("coordinates");

                        if (coordinates != null) {
                            hr.put("coordX", coordinates.get("coordX"));
                            hr.put("coordY", coordinates.get("coordY"));
                        }
                    }
                }
            }

            shoheiHRs.add(hr);
        }

        return shoheiHRs;
    }
}
