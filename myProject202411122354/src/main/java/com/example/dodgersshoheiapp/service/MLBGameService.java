package com.example.dodgersshoheiapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.dodgersshoheiapp.repository.OhtaniGameRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MLBGameService {

    // ============================================
    // ★ MLB Team ID--------------------棒グラフ用
    // ============================================
    private static final Map<String, Integer> TEAM_IDS = Map.ofEntries(

            Map.entry("Arizona Diamondbacks", 109),
            Map.entry("Atlanta Braves", 144),
            Map.entry("Baltimore Orioles", 110),
            Map.entry("Boston Red Sox", 111),
            Map.entry("Chicago Cubs", 112),
            Map.entry("Chicago White Sox", 145),
            Map.entry("Cincinnati Reds", 113),
            Map.entry("Cleveland Guardians", 114),
            Map.entry("Colorado Rockies", 115),
            Map.entry("Detroit Tigers", 116),
            Map.entry("Houston Astros", 117),
            Map.entry("Kansas City Royals", 118),
            Map.entry("Los Angeles Angels", 108),
            Map.entry("Los Angeles Dodgers", 119),
            Map.entry("Miami Marlins", 146),
            Map.entry("Milwaukee Brewers", 158),
            Map.entry("Minnesota Twins", 142),
            Map.entry("New York Mets", 121),
            Map.entry("New York Yankees", 147),
            Map.entry("Oakland Athletics", 133),
            Map.entry("Philadelphia Phillies", 143),
            Map.entry("Pittsburgh Pirates", 134),
            Map.entry("San Diego Padres", 135),
            Map.entry("San Francisco Giants", 137),
            Map.entry("Seattle Mariners", 136),
            Map.entry("St. Louis Cardinals", 138),
            Map.entry("Tampa Bay Rays", 139),
            Map.entry("Texas Rangers", 140),
            Map.entry("Toronto Blue Jays", 141),
            Map.entry("Washington Nationals", 120)

    );

    // ============================================
    // ★ Team ID取得--------------------棒グラフ用
    // ============================================
    public Integer getTeamId(String opponent) {

        return TEAM_IDS.get(opponent);
    }

    // ============================================
    // ★ NLチーム判定--------------------棒グラフ用
    // ============================================
    public boolean isNationalLeagueTeam(String opponent) {

        return switch (opponent) {

            case "Arizona Diamondbacks",
                    "Atlanta Braves",
                    "Chicago Cubs",
                    "Cincinnati Reds",
                    "Colorado Rockies",
                    "Los Angeles Dodgers",
                    "Miami Marlins",
                    "Milwaukee Brewers",
                    "New York Mets",
                    "Philadelphia Phillies",
                    "Pittsburgh Pirates",
                    "San Diego Padres",
                    "San Francisco Giants",
                    "St. Louis Cardinals",
                    "Washington Nationals" ->
                true;

            default -> false;
        };
    }

    private final String BASE_URL = "https://statsapi.mlb.com/api/v1/schedule/games/";
    private final String SCHEDULE_URL = "https://statsapi.mlb.com/api/v1/schedule";
    private final OhtaniGameRepository ohtaniGameRepository;

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

    // baseballsavantからピッチングの軌道情報
    // pitchData.coordinates.pX
    // pitchData.coordinates.pZ
    // details.type.code
    // を取得する！ ↓↓↓

    public List<Map<String, Object>> getPitchData(Long gamePk) {

        Map<String, Object> playByPlay = getPlayByPlay(gamePk);

        if (playByPlay == null)
            return Collections.emptyList();

        List<Map<String, Object>> allPlays = (List<Map<String, Object>>) playByPlay.get("allPlays");

        if (allPlays == null)
            return Collections.emptyList();

        List<Map<String, Object>> result = new ArrayList<>();

        // 🔥 大谷ID
        final long OHTANI_ID = 660271;

        for (Map<String, Object> play : allPlays) {

            // =========================
            // 🔥 ここ追加（投手判定）
            // =========================
            Map<String, Object> matchup = (Map<String, Object>) play.get("matchup");
            if (matchup == null)
                continue;

            Map<String, Object> pitcher = (Map<String, Object>) matchup.get("pitcher");
            if (pitcher == null)
                continue;

            Integer pitcherId = (Integer) pitcher.get("id");

            // 👉 大谷以外スキップ
            if (pitcherId == null || pitcherId != OHTANI_ID)
                continue;

            // =========================
            // ここから既存処理
            // =========================

            List<Map<String, Object>> events = (List<Map<String, Object>>) play.get("playEvents");

            if (events == null)
                continue;

            for (Map<String, Object> e : events) {

                if (!Boolean.TRUE.equals(e.get("isPitch")))
                    continue;

                Map<String, Object> pitchData = (Map<String, Object>) e.get("pitchData");

                if (pitchData == null)
                    continue;

                // =========================
                // 🔥 velocity取得（ここに書く）
                // =========================
                Double startSpeed = null;

                if (pitchData.get("startSpeed") != null) {
                    startSpeed = ((Number) pitchData.get("startSpeed")).doubleValue();
                }

                Map<String, Object> coords = (Map<String, Object>) pitchData.get("coordinates");

                if (coords == null)
                    continue;

                Double pX = (Double) coords.get("pX");
                Double pZ = (Double) coords.get("pZ");

                Map<String, Object> details = (Map<String, Object>) e.get("details");

                String type = null;
                if (details != null && details.get("type") != null) {
                    type = (String) ((Map<String, Object>) details.get("type")).get("code");
                }

                // =========================
                // 🔥 spinRate取得（ここ追加）
                // =========================
                Integer spinRate = null;

                if (pitchData.get("breaks") != null) {
                    Map<String, Object> breaks = (Map<String, Object>) pitchData.get("breaks");
                    spinRate = (Integer) breaks.get("spinRate");
                }

                Map<String, Object> pitch = new HashMap<>();
                pitch.put("pX", pX);
                pitch.put("pZ", pZ);
                pitch.put("type", type);
                // 👇これを追加
                pitch.put("spinRate", spinRate);

                // 🔥これ追加
                pitch.put("velocity", startSpeed);

                result.add(pitch);
            }
        }

        return result;
    }

    // =========================
    // 🔥 RISP 得点圏打率をhttps://baseball.yahoo.co.jp/mlb/player/2100825/rs
    // ここから取得（Yahoo方式）webスクレイピング
    // =========================
    public Map<String, String> getRispFromYahoo() {

        System.out.println("Yahoo取得メソッド開始");

        Map<String, String> result = new HashMap<>();

        try {
            String url = "https://baseball.yahoo.co.jp/mlb/player/2100825/rs";

            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(15000)
                    .get();

            System.out.println("Yahoo接続成功");

            // =========================
            // 🔥 「得点圏成績」セクションを探す
            // =========================
            org.jsoup.select.Elements headers = doc.select("h2, h3, th");

            for (org.jsoup.nodes.Element header : headers) {

                if (header.text().contains("得点圏成績")) {

                    System.out.println("得点圏セクション発見");

                    // 次のテーブル取得
                    org.jsoup.nodes.Element table = header.parent().nextElementSibling();

                    if (table == null)
                        continue;

                    org.jsoup.nodes.Element row = table.select("tbody tr").first();
                    if (row == null)
                        continue;

                    org.jsoup.select.Elements cols = row.select("td");

                    String avg = cols.get(0).text(); // 打率 (.160)
                    String atBat = cols.get(2).text(); // 打数 (25)
                    String hit = cols.get(3).text(); // 安打 (4)

                    String detail = hit + "-" + atBat;

                    System.out.println("取得成功: avg=" + avg + " detail=" + detail);

                    result.put("avg", avg);
                    result.put("detail", detail);

                    return result;
                }
            }

            // 見つからなかった場合
            System.out.println("得点圏成績が見つからない");
            result.put("avg", "-");
            result.put("detail", "-");

        } catch (Exception e) {
            e.printStackTrace();
            result.put("avg", "-");
            result.put("detail", "-");
        }

        return result;
    }

    /**
     * ============================================
     * ★ 打球方向分類-------円グラフ http://localhost:8080/batting/filter
     * ============================================
     */
    private String classifyDirection(
            double coordX,
            String batterSide) {

        // ============================================
        // ★ 左打者（大谷）
        // ============================================

        if ("L".equalsIgnoreCase(batterSide)) {

            if (coordX >= 140) {

                return "PULL";
            }

            else if (coordX >= 110) {

                return "CENTER";
            }

            else {

                return "OPPOSITE";
            }
        }

        // ============================================
        // ★ 右打者
        // ============================================

        else {

            if (coordX <= 110) {

                return "PULL";
            }

            else if (coordX <= 140) {

                return "CENTER";
            }

            else {

                return "OPPOSITE";
            }
        }
    }

    /**
     * ============================================
     * ★ 得点圏打率（RISP）取得（DB版）--------hogehoge_01.html用
     * ============================================
     */
    public Map<String, Object> getRispFromDB() {

        Map<String, Object> raw = ohtaniGameRepository.getRispStats();

        Map<String, Object> result = new HashMap<>();

        // =========================
        // 🔥 null対策（超重要）
        // =========================
        int hits = raw.get("hits") != null ? ((Number) raw.get("hits")).intValue() : 0;
        int atBats = raw.get("at_bats") != null ? ((Number) raw.get("at_bats")).intValue() : 0;
        double avg = raw.get("avg") != null ? ((Number) raw.get("avg")).doubleValue() : 0.0;

        // =========================
        // 🔥 表示用フォーマット
        // =========================

        // 👉 .160形式にする
        String avgStr = String.format("%.3f", avg);

        if (avgStr.startsWith("0")) {
            avgStr = avgStr.substring(1);
        }

        // 👉 4-25形式
        String detail = hits + "-" + atBats;

        // =========================
        // 🔥 modelに渡す値
        // =========================
        result.put("avg", avgStr);
        result.put("detail", detail);

        System.out.println("RISP(DB): " + avgStr + " (" + detail + ")");

        return result;
    }

    // =========================
    // 🔥 DBの 0.211 を👉 「.211」形式にする
    // =========================
    // public String getVsRightAvgFormatted() {

    // Double avg = ohtaniGameRepository.getVsRightAvg();

    // // null対策
    // if (avg == null) {
    // return ".000";
    // }

    // // 0.211 → ".211"
    // return String.format("%.3f", avg).replace("0.", ".");
    // }

    /**
     * ============================================
     * ★ 対右ピッチャー👉 hits / at_bats も取る必要あり
     * ============================================
     */
    public Map<String, String> getVsRightStatsFormatted(
            Integer season,
            String result) {

        Map<String, Object> stats = ohtaniGameRepository.getVsRightStats(
                season,
                result);

        int hits = ((Number) stats.get("hits")).intValue();
        int atBats = ((Number) stats.get("at_bats")).intValue();

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = (avg == null)
                ? ".000"
                : String.format("%.3f", avg).replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対右投手 × 対戦チーム別 AVG
     * ============================================
     */
    public Map<String, String> getVsRightStatsByOpponentFormatted(
            String opponent,
            Integer season,
            String result) {

        List<Map<String, Object>> logs = getVsRightLogs(
                result,
                opponent,
                null,
                null,
                null,
                null,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String rowResult = (String) row.get("result");

            // ★ AVG分母
            if (!"BB".equals(rowResult)
                    && !"SF".equals(rowResult)
                    && rowResult != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {

                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> formatted = new HashMap<>();
        formatted.put("avg", avgStr);
        formatted.put("detail", detail);

        return formatted;
    }

    /**
     * ============================================
     * ★ 対右投手 × 投手別 AVG
     * ============================================
     */
    public Map<String, String> getVsRightStatsByPitcherFormatted(
            String pitcher,
            Integer season,
            String result,
            String opponent) {

        List<Map<String, Object>> logs = getVsRightLogs(
                result,
                opponent,
                pitcher,
                null,
                null,
                null,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String rowResult = (String) row.get("result");

            // ★ AVG分母
            if (!"BB".equals(rowResult)
                    && !"SF".equals(rowResult)
                    && rowResult != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {

                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対右投手 × 投手別 × 球種別 AVG
     * ============================================
     */
    public Map<String, String> getVsRightStatsByPitcherAndPitchTypeFormatted(
            String pitcher,
            String pitchType,
            Integer season,
            String result) {

        Map<String, Object> stats = ohtaniGameRepository
                .getVsRightStatsByPitcherAndPitchType(
                        pitcher,
                        pitchType,
                        season,
                        result);

        int hits = stats.get("hits") != null
                ? ((Number) stats.get("hits")).intValue()
                : 0;

        int atBats = stats.get("at_bats") != null
                ? ((Number) stats.get("at_bats")).intValue()
                : 0;

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = String.format("%.3f", avg)
                .replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対右投手 × 球種別 AVG
     * ============================================
     */
    public Map<String, String> getVsRightStatsByPitchTypeFormatted(
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer season) {

        // ============================================
        // ★ BREAKING（変化球と丸っと包括）でも検索できるようにする仕組み--1469行目と親子
        // ============================================
        if ("BREAKING".equals(pitchType)) {

            List<Map<String, Object>> logs = getVsRightLogs(
                    result,
                    opponent,
                    pitcher,
                    null,
                    null,
                    null,
                    season);

            List<String> breakingTypes = normalizePitchTypes(pitchType);

            int hits = 0;
            int atBats = 0;

            for (Map<String, Object> row : logs) {

                String type = (String) row.get("pitchType");

                String rowResult = (String) row.get("result");

                if (!breakingTypes.contains(type)) {
                    continue;
                }

                // ★ AVG分母
                if (!"BB".equals(rowResult)
                        && !"SF".equals(rowResult)
                        && rowResult != null) {

                    atBats++;
                }

                // ★ AVG分子
                if ("HIT".equals(rowResult)
                        || "HR".equals(rowResult)) {

                    hits++;
                }
            }

            double avg = atBats == 0
                    ? 0.0
                    : (double) hits / atBats;

            String avgStr = String.format("%.3f", avg)
                    .replace("0.", ".");

            String detail = hits + "-" + atBats;

            Map<String, String> resultMap = new HashMap<>();

            resultMap.put("avg", avgStr);
            resultMap.put("detail", detail);

            return resultMap;
        }

        List<Map<String, Object>> logs = getVsRightLogs(
                result,
                opponent,
                pitcher,
                pitchType,
                null,
                null,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String rowResult = (String) row.get("result");

            // ★ AVG分母
            if (!"BB".equals(rowResult)
                    && !"SF".equals(rowResult)
                    && rowResult != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {

                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg)
                .replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対右ピッチャー👉 ログ表示
     * ============================================
     */
    public List<Map<String, Object>> getVsRightLogs(
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer speedMin,
            Integer speedMax,
            Integer season) {

        List<Map<String, Object>> logs = ohtaniGameRepository.getVsRightLogs(
                result,
                opponent,
                pitcher,
                pitchType,
                speedMin,
                speedMax,
                season);

        // ============================================
        // ★ 球速・球種 inject
        // ============================================
        for (Map<String, Object> row : logs) {

            String description = (String) row.get("description");

            row.put(
                    "pitchSpeed",
                    extractPitchSpeed(description));

            row.put(
                    "pitchType",
                    extractPitchType(description));
        }

        return logs;
    }

    /**
     * ============================================
     * ★ description から mph 抽出
     * ============================================
     */
    private Double extractMph(String text) {

        if (text == null || text.isBlank()) {
            return null;
        }

        Pattern pattern = Pattern.compile("(\\d+\\.?\\d*)mph");

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {

            try {
                return Double.parseDouble(matcher.group(1));
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    /**
     * ============================================
     * ★ 対右投手 × 球速帯 AVG
     * ============================================
     */
    public Map<String, String> getVsRightStatsBySpeedFormatted(
            String result,
            Integer speedMin,
            Integer speedMax,
            Integer season) {

        List<Map<String, Object>> logs = ohtaniGameRepository.getVsRightLogs(
                result,
                null,
                null,
                null,
                speedMin,
                speedMax,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String rowResult = (String) row.get("result");

            String description = (String) row.get("description");

            Double mph = extractMph(description);

            // ★ mph抽出失敗は除外
            if (mph == null) {
                continue;
            }

            // ★ speed範囲外は除外
            if (mph < speedMin || mph > speedMax) {
                continue;
            }

            // ★ AVG分母
            if (!"BB".equals(rowResult)
                    && !"SF".equals(rowResult)
                    && rowResult != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {

                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg)
                .replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対右投手 × 全フィルタ併用 AVG
     * result + opponent + pitcher + pitchType + speedRange + season
     * AVGカードとログ数を完全同期させるため、ログ抽出結果を正として計算する
     * ============================================
     */
    public Map<String, String> getVsRightStatsByFilterFormatted(
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer speedMin,
            Integer speedMax,
            Integer season) {

        List<Map<String, Object>> logs = getVsRightLogs(
                result,
                opponent,
                pitcher,
                pitchType,
                speedMin,
                speedMax,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String rowResult = (String) row.get("result");

            // ★ AVG分母
            if (rowResult != null
                    && !"BB".equals(rowResult)
                    && !"SF".equals(rowResult)) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {

                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg)
                .replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対左ピッチャー
     * ============================================
     */
    public Map<String, String> getVsLeftStatsFormatted(
            Integer season,
            String result) {

        Map<String, Object> stats = ohtaniGameRepository.getVsLeftStats(
                season,
                result);

        int hits = ((Number) stats.get("hits")).intValue();
        int atBats = ((Number) stats.get("at_bats")).intValue();

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対左投手 × 対戦チーム別 AVG
     * ============================================
     */
    public Map<String, String> getVsLeftStatsByOpponentFormatted(
            String opponent,
            Integer season,
            String result) {

        List<Map<String, Object>> logs = getVsLeftLogs(
                result,
                opponent,
                null,
                null,
                null,
                null,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String rowResult = (String) row.get("result");

            // ★ AVG分母
            if (!"BB".equals(rowResult)
                    && !"SF".equals(rowResult)
                    && rowResult != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {

                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> formatted = new HashMap<>();

        formatted.put("avg", avgStr);
        formatted.put("detail", detail);

        return formatted;
    }

    /**
     * ============================================
     * ★ 対左投手 × 投手別 AVG
     * ============================================
     */
    public Map<String, String> getVsLeftStatsByPitcherFormatted(
            String pitcher,
            Integer season,
            String result,
            String opponent) {

        List<Map<String, Object>> logs = getVsLeftLogs(
                result,
                opponent,
                pitcher,
                null,
                null,
                null,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String rowResult = (String) row.get("result");

            // ★ AVG分母
            if (!"BB".equals(rowResult)
                    && !"SF".equals(rowResult)
                    && rowResult != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {

                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対左投手 × 投手別 × 球種別 AVG
     * ============================================
     */
    public Map<String, String> getVsLeftStatsByPitcherAndPitchTypeFormatted(
            String pitcher,
            String pitchType,
            Integer season,
            String result) {

        Map<String, Object> stats = ohtaniGameRepository
                .getVsLeftStatsByPitcherAndPitchType(
                        pitcher,
                        pitchType,
                        season,
                        result);

        int hits = stats.get("hits") != null
                ? ((Number) stats.get("hits")).intValue()
                : 0;

        int atBats = stats.get("at_bats") != null
                ? ((Number) stats.get("at_bats")).intValue()
                : 0;

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = String.format("%.3f", avg)
                .replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対左投手 × 球種別 AVG
     * ============================================
     */
    public Map<String, String> getVsLeftStatsByPitchTypeFormatted(
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer season) {

        // ============================================
        // ★ BREAKING（変化球まとめ）対応
        // ============================================
        if ("BREAKING".equals(pitchType)) {

            List<Map<String, Object>> logs = getVsLeftLogs(
                    result,
                    opponent,
                    pitcher,
                    pitchType,
                    null,
                    null,
                    season);

            List<String> breakingTypes = normalizePitchTypes(pitchType);

            int hits = 0;
            int atBats = 0;

            for (Map<String, Object> row : logs) {

                String type = extractPitchType(
                        (String) row.get("description"));

                String rowResult = (String) row.get("result");

                if (!breakingTypes.contains(type)) {
                    continue;
                }

                // ★ AVG分母
                if (!"BB".equals(rowResult)
                        && !"SF".equals(rowResult)
                        && rowResult != null) {

                    atBats++;
                }

                // ★ AVG分子
                if ("HIT".equals(rowResult)
                        || "HR".equals(rowResult)) {

                    hits++;
                }
            }

            double avg = atBats == 0
                    ? 0.0
                    : (double) hits / atBats;

            String avgStr = String.format("%.3f", avg)
                    .replace("0.", ".");

            String detail = hits + "-" + atBats;

            Map<String, String> resultMap = new HashMap<>();

            resultMap.put("avg", avgStr);
            resultMap.put("detail", detail);

            return resultMap;
        }

        List<Map<String, Object>> logs = getVsLeftLogs(
                result,
                opponent,
                pitcher,
                pitchType,
                null,
                null,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String type = (String) row.get("pitchType");

            if (!pitchType.equals(type)) {
                continue;
            }

            String rowResult = (String) row.get("result");

            // ★ AVG分母
            if (!"BB".equals(rowResult)
                    && !"SF".equals(rowResult)
                    && rowResult != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {

                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg)
                .replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対左ログ
     * ============================================
     */
    public List<Map<String, Object>> getVsLeftLogs(
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer speedMin,
            Integer speedMax,
            Integer season) {

        List<Map<String, Object>> logs = ohtaniGameRepository.getVsLeftLogs(
                result,
                opponent,
                pitcher,
                pitchType,
                speedMin,
                speedMax,
                season);

        // ============================================
        // ★ 球速・球種 inject
        // ============================================
        for (Map<String, Object> row : logs) {

            String description = (String) row.get("description");

            row.put(
                    "pitchSpeed",
                    extractPitchSpeed(description));

            row.put(
                    "pitchType",
                    extractPitchType(description));
        }

        return logs;
    }

    /**
     * ============================================
     * ★ 対左投手 × 球速帯 AVG
     * ============================================
     */
    public Map<String, String> getVsLeftStatsBySpeedFormatted(
            String result,
            Integer speedMin,
            Integer speedMax,
            Integer season) {

        List<Map<String, Object>> logs = ohtaniGameRepository.getVsLeftLogs(
                result,
                null,
                null,
                null,
                speedMin,
                speedMax,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String rowResult = (String) row.get("result");

            String description = (String) row.get("description");

            Double mph = extractMph(description);

            // ★ mph抽出失敗は除外
            if (mph == null) {
                continue;
            }

            // ★ speed範囲外は除外
            if (mph < speedMin || mph > speedMax) {
                continue;
            }

            // ★ AVG分母
            if (!"BB".equals(rowResult)
                    && !"SF".equals(rowResult)
                    && rowResult != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {

                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg)
                .replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対左投手 × 全フィルタ併用 AVG
     * result + opponent + pitcher + pitchType + speedRange + season
     * AVGカードとログ数を完全同期させるため、ログ抽出結果を正として計算する
     * ============================================
     */
    public Map<String, String> getVsLeftStatsByFilterFormatted(
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer speedMin,
            Integer speedMax,
            Integer season) {

        List<Map<String, Object>> logs = getVsLeftLogs(
                result,
                opponent,
                pitcher,
                pitchType,
                speedMin,
                speedMax,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String rowResult = (String) row.get("result");

            if (rowResult != null
                    && !"BB".equals(rowResult)
                    && !"SF".equals(rowResult)) {
                atBats++;
            }

            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {
                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg)
                .replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対ALLピッチャー打率（VS ALL）
     * ============================================
     */
    public Map<String, String> getVsAllStatsFormatted(
            Integer season,
            String result,
            String opponent) {

        Map<String, Object> stats = ohtaniGameRepository.getVsAllStats(
                season,
                result,
                opponent);

        int hits = ((Number) stats.get("hits")).intValue();
        int atBats = ((Number) stats.get("at_bats")).intValue();

        Number avgNum = (Number) stats.get("avg");

        Double avg = (avgNum != null)
                ? avgNum.doubleValue()
                : null;

        String avgStr = (avg != null)
                ? String.format("%.3f", avg).replace("0.", ".")
                : ".000";

        Map<String, String> formatted = new HashMap<>();

        formatted.put("avg", avgStr);
        formatted.put("detail", hits + "-" + atBats);

        return formatted;
    }

    /**
     * ============================================
     * ★ 対ALL投手 × 対戦チーム別 AVG
     * ============================================
     */
    public Map<String, String> getVsAllStatsByOpponentFormatted(
            String opponent,
            Integer season,
            String result) {

        List<Map<String, Object>> logs = getVsAllLogs(
                null, // ★ AVG計算は全打席取得
                opponent,
                null,
                null,
                null,
                null,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String rowResult = (String) row.get("result");

            // ★ resultフィルタ
            if (result != null
                    && !result.isBlank()
                    && !"ALL".equals(result)
                    && !result.equals(rowResult)) {

                continue;
            }

            // ★ AVG分母
            if (!"BB".equals(rowResult)
                    && !"SF".equals(rowResult)
                    && rowResult != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {

                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg)
                .replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対ALL × 投手別 AVG
     * ============================================
     */
    public Map<String, String> getVsAllStatsByPitcherFormatted(
            String pitcher,
            Integer season,
            String result,
            String opponent) {

        List<Map<String, Object>> logs = getVsAllLogs(
                result,
                opponent,
                pitcher,
                null,
                null,
                null,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String rowResult = (String) row.get("result");

            // ★ AVG分母
            if (!"BB".equals(rowResult)
                    && !"SF".equals(rowResult)
                    && rowResult != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {

                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対ALL × 投手別 × 球種別 AVG
     * ============================================
     */
    public Map<String, String> getVsAllStatsByPitcherAndPitchTypeFormatted(
            String pitcher,
            String pitchType,
            Integer season,
            String result) {

        Map<String, Object> stats = ohtaniGameRepository
                .getVsAllStatsByPitcherAndPitchType(
                        pitcher,
                        pitchType,
                        season,
                        result);

        int hits = stats.get("hits") != null
                ? ((Number) stats.get("hits")).intValue()
                : 0;

        int atBats = stats.get("at_bats") != null
                ? ((Number) stats.get("at_bats")).intValue()
                : 0;

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = String.format("%.3f", avg)
                .replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対ALL × 球種別 AVG
     * ============================================
     */
    public Map<String, String> getVsAllStatsByPitchTypeFormatted(
            String result,
            String pitchType,
            Integer season,
            String opponent) {

        // ============================================
        // ★ BREAKING（変化球と丸っと包括）でも検索できるようにする仕組み
        // ============================================
        if ("BREAKING".equals(pitchType)) {

            List<Map<String, Object>> logs = getVsAllLogs(
                    result,
                    opponent,
                    null,
                    null,
                    null,
                    null,
                    season);

            List<String> breakingTypes = normalizePitchTypes(pitchType);

            int hits = 0;
            int atBats = 0;

            for (Map<String, Object> row : logs) {

                String type = (String) row.get("pitchType");

                String rowResult = (String) row.get("result");

                if (!breakingTypes.contains(type)) {
                    continue;
                }

                // ★ AVG分母
                if (!"BB".equals(rowResult)
                        && !"SF".equals(rowResult)
                        && rowResult != null) {

                    atBats++;
                }

                // ★ AVG分子
                if ("HIT".equals(rowResult)
                        || "HR".equals(rowResult)) {

                    hits++;
                }
            }

            double avg = atBats == 0
                    ? 0.0
                    : (double) hits / atBats;

            String avgStr = String.format("%.3f", avg)
                    .replace("0.", ".");

            String detail = hits + "-" + atBats;

            Map<String, String> resultMap = new HashMap<>();

            resultMap.put("avg", avgStr);
            resultMap.put("detail", detail);

            return resultMap;
        }

        List<Map<String, Object>> logs = getVsAllLogs(
                result,
                opponent,
                null,
                null,
                null,
                null,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String type = (String) row.get("pitchType");

            if (!pitchType.equals(type)) {
                continue;
            }

            String rowResult = (String) row.get("result");

            // ★ AVG分母
            if (!"BB".equals(rowResult)
                    && !"SF".equals(rowResult)
                    && rowResult != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {

                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg)
                .replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対ALL投手 × 球速帯 AVG
     * ============================================
     */
    public Map<String, String> getVsAllStatsBySpeedFormatted(
            String result,
            Integer speedMin,
            Integer speedMax,
            Integer season) {

        List<Map<String, Object>> logs = ohtaniGameRepository.getVsAllLogs(
                result,
                null,
                null,
                null,
                speedMin,
                speedMax,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String rowResult = (String) row.get("result");

            String description = (String) row.get("description");

            Double mph = extractMph(description);

            // ★ mph抽出失敗は除外
            if (mph == null) {
                continue;
            }

            // ★ speed範囲外は除外
            if (mph < speedMin || mph > speedMax) {
                continue;
            }

            // ★ AVG分母
            if (!"BB".equals(rowResult)
                    && !"SF".equals(rowResult)
                    && rowResult != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {

                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg)
                .replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();

        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対ALL投手 × 全フィルタ併用 AVG
     * result + opponent + pitcher + pitchType + speedRange + season
     * AVGカードとログ数を完全同期させるため、ログ抽出結果を正として計算する
     * ============================================
     */
    public Map<String, String> getVsAllStatsByFilterFormatted(
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer speedMin,
            Integer speedMax,
            Integer season) {

        List<Map<String, Object>> logs = getVsAllLogs(
                result,
                opponent,
                pitcher,
                pitchType,
                speedMin,
                speedMax,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String rowResult = (String) row.get("result");

            if (rowResult != null
                    && !"BB".equals(rowResult)
                    && !"SF".equals(rowResult)) {
                atBats++;
            }

            if ("HIT".equals(rowResult)
                    || "HR".equals(rowResult)) {
                hits++;
            }
        }

        double avg = atBats == 0
                ? 0.0
                : (double) hits / atBats;

        String avgStr = String.format("%.3f", avg)
                .replace("0.", ".");

        String detail = hits + "-" + atBats;

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("avg", avgStr);
        resultMap.put("detail", detail);

        return resultMap;
    }

    /**
     * ============================================
     * ★ 対ALLログ（Service）
     * ============================================
     */
    public List<Map<String, Object>> getVsAllLogs(
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer speedMin,
            Integer speedMax,
            Integer season) {

        List<Map<String, Object>> logs = ohtaniGameRepository.getVsAllLogs(
                result,
                opponent,
                pitcher,
                pitchType,
                speedMin,
                speedMax,
                season);

        // ============================================
        // ★ 球速・球種 inject
        // ============================================
        for (Map<String, Object> row : logs) {

            String description = (String) row.get("description");

            row.put(
                    "pitchSpeed",
                    extractPitchSpeed(description));

            row.put(
                    "pitchType",
                    extractPitchType(description));
        }

        return logs;
    }

    /**
     * ============================================
     * ★ 対ALLピッチャー打率（Repository呼び出し）
     * ============================================
     */
    public Map<String, Object> getVsAllStats(
            Integer season,
            String result,
            String opponent) {

        return ohtaniGameRepository.getVsAllStats(
                season,
                result,
                opponent);
    }

    /**
     * ============================================
     * ★ 対戦チーム一覧取得（Service）
     * ============================================
     */
    public List<String> getAllOpponents() {
        return ohtaniGameRepository.getAllOpponents();
    }

    /**
     * ============================================
     * ★ 投手サジェスト検索（Service）
     * ============================================
     */
    public List<String> searchPitchers(
            String keyword,
            String opponent) {

        return ohtaniGameRepository
                .searchPitchers(
                        keyword,
                        opponent);
    }

    public boolean existsPitcherInOpponent(
            String pitcher,
            String opponent) {

        return ohtaniGameRepository.existsPitcherInOpponent(
                pitcher,
                opponent);
    }

    // ============================================
    // ★ 投手所属チーム取得
    // ============================================
    public String getOpponentByPitcher(String pitcher) {

        return ohtaniGameRepository
                .getOpponentByPitcher(pitcher);
    }

    /**
     * ============================================
     * ★ 投手の左右取得（左右が違えば警告を出して再入力を促す）
     * ============================================
     */
    public String getPitcherHand(String pitcher) {

        return ohtaniGameRepository.getPitcherHand(pitcher);
    }

    /**
     * ============================================
     * ★ pitchType 正規化
     * BREAKING → 変化球一覧へ変換
     * ============================================
     */
    private List<String> normalizePitchTypes(
            String pitchType) {

        // ============================================
        // ★ ALL
        // ============================================
        if (pitchType == null
                || pitchType.isBlank()
                || "ALL".equals(pitchType)) {

            return null;
        }

        // ============================================
        // ★ BREAKING（変化球と丸っと包括）でも検索できるようにする仕組み
        // ============================================
        if ("BREAKING".equals(pitchType)) {

            return List.of(
                    "Sinker",
                    "Sweeper",
                    "Slider",
                    "Splitter",
                    "Cutter",
                    "Knuckle Curve",
                    "Slurve",
                    "Changeup");
        }

        // ============================================
        // ★ 単体球種
        // ============================================
        return List.of(pitchType);
    }

    // ============================================
    // ★ description から球速取得
    // ============================================
    private String extractPitchSpeed(String description) {

        if (description == null) {
            return "-";
        }

        Pattern pattern = Pattern.compile("(\\d+\\.?\\d*)mph");

        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            return matcher.group(1) + "mph";
        }

        return "-";
    }

    // ============================================
    // ★ description から球種取得
    // ============================================
    private String extractPitchType(String description) {

        if (description == null) {
            return "-";
        }

        String[] pitchTypes = {
                "Knuckle Curve",
                "Four-Seam",
                "Sinker",
                "Sweeper",
                "Slider",
                "Slurve",
                "Curve",
                "Cutter",
                "Splitter",
                "Changeup"
        };

        for (String type : pitchTypes) {

            if (description.contains(type)) {
                return type;
            }
        }

        return "-";
    }

    /**
     * ============================================
     * ★ 打球方向集計（ALL）-----------------円グラフ
     * ============================================
     */
    public Map<String, Integer> getHitDirectionStats(
            Integer season,
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer speedMin,
            Integer speedMax) {

        return ohtaniGameRepository.getHitDirectionStats(
                season,
                result,
                opponent,
                pitcher,
                pitchType,
                speedMin,
                speedMax);
    }

    /**
     * ============================================
     * ★ 打球方向集計（対右）-----------------円グラフ
     * result + opponent + pitcher + pitchType + speedRange + season
     * ============================================
     */
    public Map<String, Integer> getHitDirectionStatsByRight(
            Integer season,
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer speedMin,
            Integer speedMax) {

        return ohtaniGameRepository
                .getHitDirectionStatsByRight(
                        season,
                        result,
                        opponent,
                        pitcher,
                        pitchType,
                        speedMin,
                        speedMax);
    }

    /**
     * ============================================
     * ★ 打球方向集計（対左）-----------------円グラフ
     * ============================================
     */
    public Map<String, Integer> getHitDirectionStatsByLeft(
            Integer season,
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer speedMin,
            Integer speedMax) {

        return ohtaniGameRepository
                .getHitDirectionStatsByLeft(
                        season,
                        result,
                        opponent,
                        pitcher,
                        pitchType,
                        speedMin,
                        speedMax);
    }

    /**
     * ============================================
     * ★ 得点圏打率（RISP）取得（左右対応版）--------batting/filter用
     * ============================================
     */
    public Map<String, Object> getRispFromDBByHand(
            String pitcherHand,
            Integer season) {

        Map<String, Object> raw = ohtaniGameRepository.getRispStatsByHand(
                pitcherHand,
                season);

        Map<String, Object> result = new HashMap<>();

        // =========================
        // ★ null対策
        // =========================
        int hits = raw.get("hits") != null
                ? ((Number) raw.get("hits")).intValue()
                : 0;

        int atBats = raw.get("at_bats") != null
                ? ((Number) raw.get("at_bats")).intValue()
                : 0;

        double avg = raw.get("avg") != null
                ? ((Number) raw.get("avg")).doubleValue()
                : 0.0;

        // =========================
        // ★ .214形式
        // =========================
        String avgStr = String.format("%.3f", avg);

        if (avgStr.startsWith("0")) {
            avgStr = avgStr.substring(1);
        }

        // =========================
        // ★ 6-28形式
        // =========================
        String detail = hits + "-" + atBats;

        result.put("avg", avgStr);
        result.put("detail", detail);

        System.out.println(
                "RISP(" + pitcherHand + "): "
                        + avgStr
                        + " (" + detail + ")");

        return result;
    }

    /**
     * ============================================
     * ★ RISPログ取得---------------------------------batting/filter用
     * ============================================
     */
    public List<Map<String, Object>> getRispLogs(
            String pitcherHand,
            Integer season) {

        return ohtaniGameRepository.getRispLogs(
                pitcherHand,
                season);
    }

    /**
     * ============================================
     * ★ チーム別打率一覧--------------------棒グラフ用
     * ============================================
     */
    public List<Map<String, Object>> getTeamBattingAveragesAll(
            Integer season) {

        return ohtaniGameRepository.getTeamBattingAveragesAll(season);
    }

    /**
     * ============================================
     * ★ 2025取込検証用：1試合の大谷打席をDB形式へ変換
     * まずは gamePk=823942 で検証
     * ============================================
     */
    public Map<String, Object> buildOhtaniBattingImportPreview(Long gamePk) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://statsapi.mlb.com/api/v1.1/game/"
                + gamePk
                + "/feed/live";

        Map<String, Object> feed = restTemplate.getForObject(url, Map.class);

        Map<String, Object> result = new HashMap<>();

        if (feed == null) {
            result.put("error", "feed/live が取得できませんでした");
            return result;
        }

        Map<String, Object> gameData = (Map<String, Object>) feed.get("gameData");

        Map<String, Object> liveData = (Map<String, Object>) feed.get("liveData");

        Map<String, Object> datetime = (Map<String, Object>) gameData.get("datetime");

        String gameDate = (String) datetime.get("originalDate");

        Map<String, Object> teams = (Map<String, Object>) gameData.get("teams");

        Map<String, Object> home = (Map<String, Object>) teams.get("home");

        Map<String, Object> away = (Map<String, Object>) teams.get("away");

        Integer homeId = (Integer) home.get("id");

        Integer awayId = (Integer) away.get("id");

        String homeName = (String) home.get("name");

        String awayName = (String) away.get("name");

        boolean dodgersHome = homeId != null && homeId == 119;

        String opponent = dodgersHome ? awayName : homeName;

        Map<String, Object> linescore = (Map<String, Object>) liveData.get("linescore");

        Map<String, Object> lineTeams = (Map<String, Object>) linescore.get("teams");

        Map<String, Object> lineHome = (Map<String, Object>) lineTeams.get("home");

        Map<String, Object> lineAway = (Map<String, Object>) lineTeams.get("away");

        int homeRuns = ((Number) lineHome.get("runs")).intValue();

        int awayRuns = ((Number) lineAway.get("runs")).intValue();

        int dodgersRuns = dodgersHome ? homeRuns : awayRuns;

        int opponentRuns = dodgersHome ? awayRuns : homeRuns;

        String winLose = dodgersRuns > opponentRuns ? "○" : "●";

        String gameResult = winLose + dodgersRuns + "-" + opponentRuns;

        Map<String, Object> ohtaniGamesRow = new LinkedHashMap<>();

        ohtaniGamesRow.put("id", 0);
        ohtaniGamesRow.put("game_date", gameDate);
        ohtaniGamesRow.put("opponent", opponent);
        ohtaniGamesRow.put("result", gameResult);
        ohtaniGamesRow.put("form_value", 0);
        ohtaniGamesRow.put("created_at", gameDate + " 00:00:00");
        ohtaniGamesRow.put("comment", "-");
        ohtaniGamesRow.put("game_pk", gamePk);

        Map<String, Object> plays = (Map<String, Object>) liveData.get("plays");

        List<Map<String, Object>> allPlays = (List<Map<String, Object>>) plays.get("allPlays");

        List<Map<String, Object>> ohtaniPlateAppearances = new ArrayList<>();

        final int OHTANI_ID = 660271;

        for (Map<String, Object> play : allPlays) {

            Map<String, Object> matchup = (Map<String, Object>) play.get("matchup");

            if (matchup == null) {
                continue;
            }

            Map<String, Object> batter = (Map<String, Object>) matchup.get("batter");

            if (batter == null) {
                continue;
            }

            Integer batterId = (Integer) batter.get("id");

            if (batterId == null || batterId != OHTANI_ID) {
                continue;
            }

            // ★ 大谷の打席結果ではないプレーは除外
            // 例：大谷打席中の一塁ランナー牽制死
            if (isNonBatterResultPlay(play)) {
                continue;
            }

            ohtaniPlateAppearances.add(
                    buildOhtaniPaRow(play));
        }

        Map<String, Object> detailRow = new LinkedHashMap<>();

        detailRow.put("id", 0);
        detailRow.put("game_id", 0);
        detailRow.put("created_at", gameDate + " 00:00:00");

        for (int i = 1; i <= 6; i++) {

            if (i <= ohtaniPlateAppearances.size()) {

                Map<String, Object> pa = ohtaniPlateAppearances.get(i - 1);

                detailRow.put("pa" + i + "_pitcher", pa.get("pitcher"));
                detailRow.put("pa" + i + "_pitcher_hand", pa.get("pitcher_hand"));
                detailRow.put("pa" + i + "_result", pa.get("result"));
                detailRow.put("pa" + i + "_description", pa.get("description"));

            } else {

                detailRow.put("pa" + i + "_pitcher", null);
                detailRow.put("pa" + i + "_pitcher_hand", null);
                detailRow.put("pa" + i + "_result", null);
                detailRow.put("pa" + i + "_description", null);
            }
        }

        result.put("ohtani_games", ohtaniGamesRow);
        result.put("ohtani_game_details", detailRow);
        result.put("pa_count", ohtaniPlateAppearances.size());

        return result;
    }

    /**
     * ============================================
     * ★ 今回は 2025/2024を壊さず、2023用だけ Angels基準で追加します。現在の取得ロジックは Dodgers teamId=119
     * 前提なので、2023だけ teamId=108 を渡せる形にします。
     * ============================================
     */
    private Map<String, Object> buildOhtaniBattingImportPreview(
            Long gamePk,
            int targetTeamId) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://statsapi.mlb.com/api/v1.1/game/"
                + gamePk
                + "/feed/live";

        Map<String, Object> feed = restTemplate.getForObject(url, Map.class);

        Map<String, Object> result = new HashMap<>();

        if (feed == null) {
            result.put("error", "feed/live が取得できませんでした");
            return result;
        }

        Map<String, Object> gameData = (Map<String, Object>) feed.get("gameData");
        Map<String, Object> liveData = (Map<String, Object>) feed.get("liveData");

        Map<String, Object> datetime = (Map<String, Object>) gameData.get("datetime");
        String gameDate = (String) datetime.get("originalDate");

        Map<String, Object> teams = (Map<String, Object>) gameData.get("teams");
        Map<String, Object> home = (Map<String, Object>) teams.get("home");
        Map<String, Object> away = (Map<String, Object>) teams.get("away");

        Integer homeId = (Integer) home.get("id");
        String homeName = (String) home.get("name");
        String awayName = (String) away.get("name");

        boolean targetHome = homeId != null && homeId == targetTeamId;
        String opponent = targetHome ? awayName : homeName;

        Map<String, Object> linescore = (Map<String, Object>) liveData.get("linescore");
        Map<String, Object> lineTeams = (Map<String, Object>) linescore.get("teams");
        Map<String, Object> lineHome = (Map<String, Object>) lineTeams.get("home");
        Map<String, Object> lineAway = (Map<String, Object>) lineTeams.get("away");

        int homeRuns = ((Number) lineHome.get("runs")).intValue();
        int awayRuns = ((Number) lineAway.get("runs")).intValue();

        int targetRuns = targetHome ? homeRuns : awayRuns;
        int opponentRuns = targetHome ? awayRuns : homeRuns;

        String winLose = targetRuns > opponentRuns ? "○" : "●";
        String gameResult = winLose + targetRuns + "-" + opponentRuns;

        Map<String, Object> ohtaniGamesRow = new LinkedHashMap<>();
        ohtaniGamesRow.put("id", 0);
        ohtaniGamesRow.put("game_date", gameDate);
        ohtaniGamesRow.put("opponent", opponent);
        ohtaniGamesRow.put("result", gameResult);
        ohtaniGamesRow.put("form_value", 0);
        ohtaniGamesRow.put("created_at", gameDate + " 00:00:00");
        ohtaniGamesRow.put("comment", "-");
        ohtaniGamesRow.put("game_pk", gamePk);

        Map<String, Object> plays = (Map<String, Object>) liveData.get("plays");
        List<Map<String, Object>> allPlays = (List<Map<String, Object>>) plays.get("allPlays");

        List<Map<String, Object>> ohtaniPlateAppearances = new ArrayList<>();

        final int OHTANI_ID = 660271;

        for (Map<String, Object> play : allPlays) {

            Map<String, Object> matchup = (Map<String, Object>) play.get("matchup");
            if (matchup == null)
                continue;

            Map<String, Object> batter = (Map<String, Object>) matchup.get("batter");
            if (batter == null)
                continue;

            Integer batterId = (Integer) batter.get("id");

            if (batterId == null || batterId != OHTANI_ID) {
                continue;
            }

            if (isNonBatterResultPlay(play)) {
                continue;
            }

            ohtaniPlateAppearances.add(buildOhtaniPaRow(play));
        }

        Map<String, Object> detailRow = new LinkedHashMap<>();
        detailRow.put("id", 0);
        detailRow.put("game_id", 0);
        detailRow.put("created_at", gameDate + " 00:00:00");

        for (int i = 1; i <= 6; i++) {

            if (i <= ohtaniPlateAppearances.size()) {

                Map<String, Object> pa = ohtaniPlateAppearances.get(i - 1);

                detailRow.put("pa" + i + "_pitcher", pa.get("pitcher"));
                detailRow.put("pa" + i + "_pitcher_hand", pa.get("pitcher_hand"));
                detailRow.put("pa" + i + "_result", pa.get("result"));
                detailRow.put("pa" + i + "_description", pa.get("description"));

            } else {

                detailRow.put("pa" + i + "_pitcher", null);
                detailRow.put("pa" + i + "_pitcher_hand", null);
                detailRow.put("pa" + i + "_result", null);
                detailRow.put("pa" + i + "_description", null);
            }
        }

        result.put("ohtani_games", ohtaniGamesRow);
        result.put("ohtani_game_details", detailRow);
        result.put("pa_count", ohtaniPlateAppearances.size());

        return result;
    }

    /**
     * ============================================
     * ★ 大谷の打席結果ではないプレーを除外
     * 例：大谷打席中の牽制死、盗塁死など
     * ============================================
     */
    private boolean isNonBatterResultPlay(Map<String, Object> play) {

        Map<String, Object> result = (Map<String, Object>) play.get("result");

        if (result == null) {
            return false;
        }

        String event = result.get("event") != null
                ? result.get("event").toString()
                : "";

        String description = result.get("description") != null
                ? result.get("description").toString().toLowerCase()
                : "";

        return event.contains("Pickoff")
                || event.contains("Caught Stealing")
                || description.contains("pickoff")
                || description.contains("caught stealing")
                || description.contains("picked off")
                || description.contains("pick-off");
    }

    private Map<String, Object> buildOhtaniPaRow(
            Map<String, Object> play) {

        Map<String, Object> row = new LinkedHashMap<>();

        Map<String, Object> matchup = (Map<String, Object>) play.get("matchup");

        Map<String, Object> pitcher = (Map<String, Object>) matchup.get("pitcher");

        Map<String, Object> pitchHand = (Map<String, Object>) matchup.get("pitchHand");

        String pitcherName = pitcher != null
                ? (String) pitcher.get("fullName")
                : "-";

        String pitcherHand = pitchHand != null
                ? (String) pitchHand.get("code")
                : "-";

        Map<String, Object> result = (Map<String, Object>) play.get("result");

        String event = result != null
                ? (String) result.get("event")
                : "";

        String resultCode = convertResultCode(event);

        List<Map<String, Object>> playEvents = (List<Map<String, Object>>) play.get("playEvents");

        Map<String, Object> lastPitch = findLastPitch(playEvents);

        String countText = buildPrePitchCountText(playEvents);

        int pitchNumber = countPitches(playEvents);

        String pitchSpeed = extractPitchSpeedText(lastPitch);

        String pitchType = extractPitchTypeText(lastPitch);

        String direction = extractDirectionText(play);

        String eventJa = convertEventJapanese(event, play);

        boolean risp = hasRunnerInScoringPosition(play);

        String description;

        if ("Intent Walk".equals(event) || "Intentional Walk".equals(event)) {

            description = "申告敬遠50mph Nopitch";

        } else if ("BB".equals(resultCode) || "SO".equals(resultCode)) {

            description = countText
                    + "となった"
                    + pitchNumber
                    + "球目"
                    + pitchSpeed
                    + " "
                    + pitchType
                    + "を"
                    + eventJa;

        } else {

            description = countText
                    + "となった"
                    + pitchNumber
                    + "球目"
                    + pitchSpeed
                    + " "
                    + pitchType
                    + "を"
                    + direction
                    + eventJa;
        }

        if (risp) {
            description += " 得点圏にランナー有";
        }

        row.put("pitcher", pitcherName);
        row.put("pitcher_hand", pitcherHand);
        row.put("result", resultCode);
        row.put("description", description);

        return row;
    }

    private Map<String, Object> findLastPitch(
            List<Map<String, Object>> playEvents) {

        if (playEvents == null) {
            return null;
        }

        Map<String, Object> lastPitch = null;

        for (Map<String, Object> e : playEvents) {

            if (Boolean.TRUE.equals(e.get("isPitch"))) {
                lastPitch = e;
            }
        }

        return lastPitch;
    }

    private int countPitches(
            List<Map<String, Object>> playEvents) {

        if (playEvents == null) {
            return 0;
        }

        int count = 0;

        for (Map<String, Object> e : playEvents) {

            if (Boolean.TRUE.equals(e.get("isPitch"))) {
                count++;
            }
        }

        return count;
    }

    // private String buildCountText(
    // Map<String, Object> lastPitch) {

    // if (lastPitch == null) {
    // return "-";
    // }

    // Map<String, Object> count = (Map<String, Object>) lastPitch.get("count");

    // if (count == null) {
    // return "-";
    // }

    // Object balls = count.get("balls");

    // Object strikes = count.get("strikes");

    // return balls + "-" + strikes;
    // }

    /**
     * ============================================
     * ★ 最後の投球「前」のカウントを作る
     * 例：四球後 4-2 ではなく、投球前の 3-2 に戻す
     * ============================================
     */
    private String buildPrePitchCountText(
            List<Map<String, Object>> playEvents) {

        if (playEvents == null || playEvents.isEmpty()) {
            return "-";
        }

        Map<String, Object> lastPitch = findLastPitch(playEvents);

        if (lastPitch == null) {
            return "-";
        }

        Map<String, Object> count = (Map<String, Object>) lastPitch.get("count");

        Map<String, Object> details = (Map<String, Object>) lastPitch.get("details");

        if (count == null || details == null) {
            return "-";
        }

        int balls = ((Number) count.get("balls")).intValue();

        int strikes = ((Number) count.get("strikes")).intValue();

        String code = details.get("code") != null
                ? details.get("code").toString()
                : "";

        String description = details.get("description") != null
                ? details.get("description").toString()
                : "";

        /*
         * 最後の投球後カウントから、投球前カウントへ戻す
         */
        if ("B".equals(code)
                || "I".equals(code)
                || "H".equals(code)
                || "V".equals(code)
                || description.contains("Ball")
                || description.contains("Hit By Pitch")) {
            balls = Math.max(0, balls - 1);
        }

        if ("S".equals(code)
                || "C".equals(code)
                || "W".equals(code)
                || "F".equals(code)
                || "T".equals(code)
                || "L".equals(code)
                || "M".equals(code)
                || description.contains("Strike")
                || description.contains("Foul")) {
            strikes = Math.max(0, strikes - 1);
        }

        return balls + "-" + strikes;
    }

    private String extractPitchSpeedText(
            Map<String, Object> lastPitch) {

        if (lastPitch == null) {
            return "-";
        }

        Map<String, Object> pitchData = (Map<String, Object>) lastPitch.get("pitchData");

        if (pitchData == null || pitchData.get("startSpeed") == null) {
            return "-";
        }

        double speed = ((Number) pitchData.get("startSpeed")).doubleValue();

        return String.format("%.1f", speed) + "mph";
    }

    // private String extractPitchTypeText(
    // Map<String, Object> lastPitch) {

    // if (lastPitch == null) {
    // return "-";
    // }

    // Map<String, Object> details = (Map<String, Object>) lastPitch.get("details");

    // if (details == null) {
    // return "-";
    // }

    // Map<String, Object> type = (Map<String, Object>) details.get("type");

    // if (type == null || type.get("description") == null) {
    // return "-";
    // }

    // return (String) type.get("description");
    // }

    private String extractPitchTypeText(
            Map<String, Object> lastPitch) {

        if (lastPitch == null) {
            return "-";
        }

        Map<String, Object> details = (Map<String, Object>) lastPitch.get("details");

        if (details == null) {
            return "-";
        }

        Map<String, Object> type = (Map<String, Object>) details.get("type");

        if (type == null || type.get("description") == null) {
            return "-";
        }

        String pitchType = (String) type.get("description");

        // batting/filter の球種名に合わせる
        if ("Four-Seam Fastball".equals(pitchType)) {
            return "Four-Seam";
        }

        return pitchType;
    }

    private String convertResultCode(String event) {

        if (event == null) {
            return "OUT";
        }

        if ("Single".equals(event)
                || "Double".equals(event)
                || "Triple".equals(event)) {
            return "HIT";
        }

        if ("Home Run".equals(event)) {
            return "HR";
        }

        if ("Strikeout".equals(event)) {
            return "SO";
        }

        if ("Walk".equals(event)
                || "Intent Walk".equals(event)
                || "Intentional Walk".equals(event)
                || "Hit By Pitch".equals(event)) {
            return "BB";
        }

        if (event.contains("Field Error")
                || event.contains("Error")) {
            return "Err";
        }

        if (event.contains("Sac Fly")
                || event.contains("Sac Bunt")
                || event.contains("Sacrifice")) {
            return "SF";
        }

        if (event.contains("Fielder")
                || event.contains("Fielders Choice")) {
            return "FC";
        }

        return "OUT";
    }

    private String convertEventJapanese(
            String event,
            Map<String, Object> play) {

        if (event == null) {
            return "アウト";
        }

        return switch (event) {

            case "Single" -> "ゴロで安打";
            case "Double" -> "へ二塁打";
            case "Triple" -> "へ三塁打";
            case "Home Run" -> "へホームラン";
            case "Strikeout" -> "三振";
            case "Walk" -> "フォアボール";
            case "Intent Walk", "Intentional Walk" -> "申告敬遠50mph Nopitch";
            case "Hit By Pitch" -> "デッドボール";

            default -> {
                String desc = extractResultDescription(play);

                if (desc.contains("grounds")) {
                    yield "ゴロでアウト";
                }

                if (desc.contains("flies")) {
                    yield "フライでアウト";
                }

                if (desc.contains("lines")) {
                    yield "ライナーでアウト";
                }

                if (desc.contains("pops")) {
                    yield "ポップフライでアウト";
                }

                if (event.contains("Fielder")) {
                    yield "フィルダースチョイス";
                }

                if (event.contains("Error")) {
                    yield "エラーで出塁";
                }

                yield "アウト";
            }
        };
    }

    private String extractDirectionText(
            Map<String, Object> play) {

        String desc = extractResultDescription(play);

        if (desc == null) {
            return "";
        }

        desc = desc.toLowerCase();

        if (desc.contains("right-center")) {
            return "右中間";
        }

        if (desc.contains("left-center")) {
            return "左中間";
        }

        if (desc.contains("right fielder")
                || desc.contains("right field")) {
            return "ライト";
        }

        if (desc.contains("center fielder")
                || desc.contains("center field")) {
            return "センター";
        }

        if (desc.contains("left fielder")
                || desc.contains("left field")) {
            return "レフト";
        }

        if (desc.contains("second baseman")) {
            return "セカンド";
        }

        if (desc.contains("shortstop")) {
            return "ショート";
        }

        if (desc.contains("third baseman")) {
            return "サード";
        }

        if (desc.contains("first baseman")) {
            return "ファースト";
        }

        if (desc.contains("pitcher")) {
            return "ピッチャー";
        }

        return "";
    }

    private String extractResultDescription(
            Map<String, Object> play) {

        Map<String, Object> result = (Map<String, Object>) play.get("result");

        if (result == null || result.get("description") == null) {
            return "";
        }

        return ((String) result.get("description")).toLowerCase();
    }

    private boolean hasRunnerInScoringPosition(
            Map<String, Object> play) {

        List<Map<String, Object>> runners = (List<Map<String, Object>>) play.get("runners");

        if (runners == null) {
            return false;
        }

        for (Map<String, Object> runner : runners) {

            Map<String, Object> movement = (Map<String, Object>) runner.get("movement");

            if (movement == null) {
                continue;
            }

            String originBase = (String) movement.get("originBase");

            if ("2B".equals(originBase)
                    || "3B".equals(originBase)) {
                return true;
            }
        }

        return false;
    }

    /**
     * ============================================
     * まず STEP1：ohtani_games_2025.csv ダウンロード から作りましょう。
     * 
     * 1. MLBGameService.java に追加
     * ★ 2025年 Dodgers レギュラーシーズン一覧取得
     * ohtani_games_2025.csv 用
     * ============================================
     */
    public String buildOhtaniGames2025Csv() {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://statsapi.mlb.com/api/v1/schedule"
                + "?sportId=1"
                + "&teamId=119"
                + "&season=2025"
                + "&gameType=R";

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        StringBuilder csv = new StringBuilder();

        csv.append("id,game_date,opponent,result,form_value,created_at,comment,game_pk\n");

        if (response == null || response.get("dates") == null) {
            return csv.toString();
        }

        List<Map<String, Object>> dates = (List<Map<String, Object>>) response.get("dates");

        for (Map<String, Object> dateObj : dates) {

            List<Map<String, Object>> games = (List<Map<String, Object>>) dateObj.get("games");

            if (games == null) {
                continue;
            }

            for (Map<String, Object> game : games) {

                Object statusObj = game.get("status");

                if (statusObj == null) {
                    continue;
                }

                Map<String, Object> status = (Map<String, Object>) statusObj;

                String detailedState = (String) status.get("detailedState");

                if (!"Final".equals(detailedState)) {
                    continue;
                }

                Integer gamePk = ((Number) game.get("gamePk")).intValue();

                String gameDate = ((String) game.get("officialDate"));

                Map<String, Object> teams = (Map<String, Object>) game.get("teams");

                Map<String, Object> home = (Map<String, Object>) teams.get("home");

                Map<String, Object> away = (Map<String, Object>) teams.get("away");

                Map<String, Object> homeTeam = (Map<String, Object>) home.get("team");

                Map<String, Object> awayTeam = (Map<String, Object>) away.get("team");

                Integer homeId = ((Number) homeTeam.get("id")).intValue();

                Integer awayId = ((Number) awayTeam.get("id")).intValue();

                String homeName = (String) homeTeam.get("name");

                String awayName = (String) awayTeam.get("name");

                boolean dodgersHome = homeId == 119;

                String opponent = dodgersHome ? awayName : homeName;

                int homeScore = home.get("score") == null
                        ? 0
                        : ((Number) home.get("score")).intValue();

                int awayScore = away.get("score") == null
                        ? 0
                        : ((Number) away.get("score")).intValue();

                int dodgersScore = dodgersHome ? homeScore : awayScore;

                int opponentScore = dodgersHome ? awayScore : homeScore;

                String winLose = dodgersScore > opponentScore ? "○" : "●";

                String result = winLose + dodgersScore + "-" + opponentScore;

                csv.append("0").append(",");
                csv.append(gameDate).append(",");
                csv.append(escapeCsv(opponent)).append(",");
                csv.append(result).append(",");
                csv.append("0").append(",");
                csv.append(gameDate).append(" 00:00:00").append(",");
                csv.append("-").append(",");
                csv.append(gamePk).append("\n");
            }
        }

        return csv.toString();
    }

    /**
     * ============================================
     * 2. 同じ MLBGameService.java に追加
     * ============================================
     */
    private String escapeCsv(String value) {

        if (value == null) {
            return "";
        }

        if (value.contains(",")
                || value.contains("\"")
                || value.contains("\n")) {

            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    /**
     * ============================================
     * ★ 2025年 大谷翔平 打席詳細CSV
     * ohtani_game_details_2025.csv 用
     * ============================================
     */
    public String buildOhtaniGameDetails2025Csv() {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://statsapi.mlb.com/api/v1/schedule"
                + "?sportId=1"
                + "&teamId=119"
                + "&season=2025"
                + "&gameType=R";

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        StringBuilder csv = new StringBuilder();

        csv.append("id,game_id,created_at,")
                .append("pa1_pitcher,pa1_pitcher_hand,pa1_result,pa1_description,")
                .append("pa2_pitcher,pa2_pitcher_hand,pa2_result,pa2_description,")
                .append("pa3_pitcher,pa3_pitcher_hand,pa3_result,pa3_description,")
                .append("pa4_pitcher,pa4_pitcher_hand,pa4_result,pa4_description,")
                .append("pa5_pitcher,pa5_pitcher_hand,pa5_result,pa5_description,")
                .append("pa6_pitcher,pa6_pitcher_hand,pa6_result,pa6_description\n");

        if (response == null || response.get("dates") == null) {
            return csv.toString();
        }

        List<Map<String, Object>> dates = (List<Map<String, Object>>) response.get("dates");

        for (Map<String, Object> dateObj : dates) {

            List<Map<String, Object>> games = (List<Map<String, Object>>) dateObj.get("games");

            if (games == null) {
                continue;
            }

            for (Map<String, Object> game : games) {

                Map<String, Object> status = (Map<String, Object>) game.get("status");

                if (status == null) {
                    continue;
                }

                String detailedState = (String) status.get("detailedState");

                if (!"Final".equals(detailedState)) {
                    continue;
                }

                Long gamePk = ((Number) game.get("gamePk")).longValue();

                try {

                    Map<String, Object> preview = buildOhtaniBattingImportPreview(gamePk);

                    Map<String, Object> detail = (Map<String, Object>) preview.get("ohtani_game_details");

                    if (detail == null) {
                        continue;
                    }

                    csv.append("0").append(",");
                    csv.append("0").append(",");
                    csv.append(escapeCsv((String) detail.get("created_at"))).append(",");

                    for (int i = 1; i <= 6; i++) {

                        csv.append(escapeCsv((String) detail.get("pa" + i + "_pitcher"))).append(",");
                        csv.append(escapeCsv((String) detail.get("pa" + i + "_pitcher_hand"))).append(",");
                        csv.append(escapeCsv((String) detail.get("pa" + i + "_result"))).append(",");
                        csv.append(escapeCsv((String) detail.get("pa" + i + "_description")));

                        if (i < 6) {
                            csv.append(",");
                        }
                    }

                    csv.append("\n");

                } catch (Exception e) {

                    // 1試合失敗しても全体停止しない
                    System.out.println(
                            "ohtani_game_details CSV生成失敗 gamePk="
                                    + gamePk
                                    + " / "
                                    + e.getMessage());
                }
            }
        }

        return csv.toString();
    }

    /**
     * ============================================
     * まず STEP1：ohtani_games_2024.csv ダウンロード から作りましょう。
     * 
     * 1. MLBGameService.java に追加
     * ★ 2024年 Dodgers レギュラーシーズン一覧取得
     * ohtani_games_2024.csv 用
     * ============================================
     */

    public String buildOhtaniGames2024Csv() {
        return buildOhtaniGamesCsvBySeasonAndTeam(2024, 119);
    }

    public String buildOhtaniGameDetails2024Csv() {
        return buildOhtaniGameDetailsCsvBySeasonAndTeam(2024, 119);
    }

    private String buildOhtaniGamesCsvBySeasonAndTeam(
            int season,
            int teamId) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://statsapi.mlb.com/api/v1/schedule"
                + "?sportId=1"
                + "&teamId=" + teamId
                + "&season=" + season
                + "&gameType=R";

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        StringBuilder csv = new StringBuilder();
        csv.append("id,game_date,opponent,result,form_value,created_at,comment,game_pk\n");

        if (response == null || response.get("dates") == null) {
            return csv.toString();
        }

        List<Map<String, Object>> dates = (List<Map<String, Object>>) response.get("dates");

        for (Map<String, Object> dateObj : dates) {

            List<Map<String, Object>> games = (List<Map<String, Object>>) dateObj.get("games");

            if (games == null)
                continue;

            for (Map<String, Object> game : games) {

                Map<String, Object> status = (Map<String, Object>) game.get("status");
                if (status == null)
                    continue;

                String detailedState = (String) status.get("detailedState");
                if (!"Final".equals(detailedState))
                    continue;

                Long gamePk = ((Number) game.get("gamePk")).longValue();

                Map<String, Object> preview = buildOhtaniBattingImportPreview(gamePk, teamId);

                Map<String, Object> row = (Map<String, Object>) preview.get("ohtani_games");

                if (row == null)
                    continue;

                csv.append("0").append(",");
                csv.append(row.get("game_date")).append(",");
                csv.append(escapeCsv((String) row.get("opponent"))).append(",");
                csv.append(row.get("result")).append(",");
                csv.append("0").append(",");
                csv.append(row.get("created_at")).append(",");
                csv.append("-").append(",");
                csv.append(gamePk).append("\n");
            }
        }

        return csv.toString();
    }

    /**
     * ============================================
     * 2018~2022 CSV用メソッド追加。
     * 
     * 1. MLBGameService.java に追加
     * ★ 2018年~2022年 Angels レギュラーシーズン一覧取得
     * ohtani_games_2018.csv~ohtani_games_2022.csv 用
     * ============================================
     */

    public String buildOhtaniGameDetails2023Csv() {
        return buildOhtaniGameDetailsCsvBySeasonAndTeam(2023, 108);
    }

    public String buildOhtaniGames2018Csv() {
        return buildOhtaniGamesCsvBySeasonAndTeam(2018, 108);
    }

    public String buildOhtaniGameDetails2018Csv() {
        return buildOhtaniGameDetailsCsvBySeasonAndTeam(2018, 108);
    }

    public String buildOhtaniGames2019Csv() {
        return buildOhtaniGamesCsvBySeasonAndTeam(2019, 108);
    }

    public String buildOhtaniGameDetails2019Csv() {
        return buildOhtaniGameDetailsCsvBySeasonAndTeam(2019, 108);
    }

    public String buildOhtaniGames2020Csv() {
        return buildOhtaniGamesCsvBySeasonAndTeam(2020, 108);
    }

    public String buildOhtaniGameDetails2020Csv() {
        return buildOhtaniGameDetailsCsvBySeasonAndTeam(2020, 108);
    }

    public String buildOhtaniGames2021Csv() {
        return buildOhtaniGamesCsvBySeasonAndTeam(2021, 108);
    }

    public String buildOhtaniGameDetails2021Csv() {
        return buildOhtaniGameDetailsCsvBySeasonAndTeam(2021, 108);
    }

    public String buildOhtaniGames2022Csv() {
        return buildOhtaniGamesCsvBySeasonAndTeam(2022, 108);
    }

    public String buildOhtaniGameDetails2022Csv() {
        return buildOhtaniGameDetailsCsvBySeasonAndTeam(2022, 108);
    }

    /**
     * ============================================
     * 2023 CSV用メソッド追加。
     * 
     * 1. MLBGameService.java に追加
     * ★ 2023年 Angels レギュラーシーズン一覧取得
     * ohtani_games_2023.csv 用
     * ============================================
     */
    public String buildOhtaniGames2023Csv() {
        return buildOhtaniGamesCsvBySeasonAndTeam(2023, 108);
    }

    private String buildOhtaniGameDetailsCsvBySeasonAndTeam(
            int season,
            int teamId) {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://statsapi.mlb.com/api/v1/schedule"
                + "?sportId=1"
                + "&teamId=" + teamId
                + "&season=" + season
                + "&gameType=R";

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        StringBuilder csv = new StringBuilder();

        csv.append("id,game_id,created_at,")
                .append("pa1_pitcher,pa1_pitcher_hand,pa1_result,pa1_description,")
                .append("pa2_pitcher,pa2_pitcher_hand,pa2_result,pa2_description,")
                .append("pa3_pitcher,pa3_pitcher_hand,pa3_result,pa3_description,")
                .append("pa4_pitcher,pa4_pitcher_hand,pa4_result,pa4_description,")
                .append("pa5_pitcher,pa5_pitcher_hand,pa5_result,pa5_description,")
                .append("pa6_pitcher,pa6_pitcher_hand,pa6_result,pa6_description\n");

        if (response == null || response.get("dates") == null) {
            return csv.toString();
        }

        List<Map<String, Object>> dates = (List<Map<String, Object>>) response.get("dates");

        for (Map<String, Object> dateObj : dates) {

            List<Map<String, Object>> games = (List<Map<String, Object>>) dateObj.get("games");

            if (games == null) {
                continue;
            }

            for (Map<String, Object> game : games) {

                Map<String, Object> status = (Map<String, Object>) game.get("status");

                if (status == null) {
                    continue;
                }

                String detailedState = (String) status.get("detailedState");

                if (!"Final".equals(detailedState)) {
                    continue;
                }

                Long gamePk = ((Number) game.get("gamePk")).longValue();

                try {

                    Map<String, Object> preview = buildOhtaniBattingImportPreview(gamePk, teamId);

                    Map<String, Object> detail = (Map<String, Object>) preview.get("ohtani_game_details");

                    if (detail == null) {
                        continue;
                    }

                    csv.append("0").append(",");
                    csv.append("0").append(",");
                    csv.append(escapeCsv((String) detail.get("created_at"))).append(",");

                    for (int i = 1; i <= 6; i++) {

                        csv.append(escapeCsv((String) detail.get("pa" + i + "_pitcher"))).append(",");
                        csv.append(escapeCsv((String) detail.get("pa" + i + "_pitcher_hand"))).append(",");
                        csv.append(escapeCsv((String) detail.get("pa" + i + "_result"))).append(",");
                        csv.append(escapeCsv((String) detail.get("pa" + i + "_description")));

                        if (i < 6) {
                            csv.append(",");
                        }
                    }

                    csv.append("\n");

                } catch (Exception e) {

                    System.out.println(
                            "ohtani_game_details CSV生成失敗 gamePk="
                                    + gamePk
                                    + " / "
                                    + e.getMessage());
                }
            }
        }

        return csv.toString();
    }

}
