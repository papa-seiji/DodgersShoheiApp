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
    public String getVsRightAvgFormatted() {

        Double avg = ohtaniGameRepository.getVsRightAvg();

        // null対策
        if (avg == null) {
            return ".000";
        }

        // 0.211 → ".211"
        return String.format("%.3f", avg).replace("0.", ".");
    }

    /**
     * ============================================
     * ★ 対右ピッチャー👉 hits / at_bats も取る必要あり
     * ============================================
     */
    public Map<String, String> getVsRightStatsFormatted(Integer season) {

        Map<String, Object> stats = ohtaniGameRepository.getVsRightStats(season);

        int hits = ((Number) stats.get("hits")).intValue();
        int atBats = ((Number) stats.get("at_bats")).intValue();
        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = (avg == null) ? ".000" : String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> result = new HashMap<>();
        result.put("avg", avgStr);
        result.put("detail", detail);

        return result;
    }

    /**
     * ============================================
     * ★ 対右投手 × 対戦チーム別 AVG
     * ============================================
     */
    public Map<String, String> getVsRightStatsByOpponentFormatted(
            String opponent,
            Integer season) {

        Map<String, Object> stats = ohtaniGameRepository.getVsRightStatsByOpponent(
                opponent,
                season);

        int hits = stats.get("hits") != null
                ? ((Number) stats.get("hits")).intValue()
                : 0;

        int atBats = stats.get("at_bats") != null
                ? ((Number) stats.get("at_bats")).intValue()
                : 0;

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> result = new HashMap<>();
        result.put("avg", avgStr);
        result.put("detail", detail);

        return result;
    }

    /**
     * ============================================
     * ★ 対右投手 × 投手別 AVG
     * ============================================
     */
    public Map<String, String> getVsRightStatsByPitcherFormatted(String pitcher) {

        Map<String, Object> stats = ohtaniGameRepository.getVsRightStatsByPitcher(pitcher);

        int hits = stats.get("hits") != null
                ? ((Number) stats.get("hits")).intValue()
                : 0;

        int atBats = stats.get("at_bats") != null
                ? ((Number) stats.get("at_bats")).intValue()
                : 0;

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> result = new HashMap<>();
        result.put("avg", avgStr);
        result.put("detail", detail);

        return result;
    }

    /**
     * ============================================
     * ★ 対右投手 × 球種別 AVG
     * ============================================
     */
    public Map<String, String> getVsRightStatsByPitchTypeFormatted(
            String pitchType,
            Integer season) {

        // ============================================
        // ★ BREAKING（変化球と丸っと包括）でも検索できるようにする仕組み--1469行目と親子
        // ============================================
        if ("BREAKING".equals(pitchType)) {

            List<Map<String, Object>> logs = getVsRightLogs(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    season);

            List<String> breakingTypes = normalizePitchTypes(
                    pitchType);

            int hits = 0;
            int atBats = 0;

            for (Map<String, Object> row : logs) {

                String type = (String) row.get("pitchType");

                String result = (String) row.get("result");

                if (!breakingTypes.contains(type)) {
                    continue;
                }

                // ★ AVG分母
                if (!"BB".equals(result)
                        && !"SF".equals(result)
                        && result != null) {

                    atBats++;
                }

                // ★ AVG分子
                if ("HIT".equals(result)
                        || "HR".equals(result)) {

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

        Map<String, Object> stats = ohtaniGameRepository.getVsRightStatsByPitchType(pitchType);

        int hits = stats.get("hits") != null
                ? ((Number) stats.get("hits")).intValue()
                : 0;

        int atBats = stats.get("at_bats") != null
                ? ((Number) stats.get("at_bats")).intValue()
                : 0;

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> result = new HashMap<>();
        result.put("avg", avgStr);
        result.put("detail", detail);

        return result;
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
            Integer speedMin,
            Integer speedMax,
            Integer season) {

        List<Map<String, Object>> logs = ohtaniGameRepository.getVsRightLogs(
                null,
                null,
                null,
                null,
                speedMin,
                speedMax,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String result = (String) row.get("result");

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
            if (!"BB".equals(result)
                    && !"SF".equals(result)
                    && result != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(result)
                    || "HR".equals(result)) {

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
    public Map<String, String> getVsLeftStatsFormatted(Integer season) {

        Map<String, Object> stats = ohtaniGameRepository.getVsLeftStats(season);

        int hits = ((Number) stats.get("hits")).intValue();
        int atBats = ((Number) stats.get("at_bats")).intValue();

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> result = new HashMap<>();
        result.put("avg", avgStr);
        result.put("detail", detail);

        return result;
    }

    /**
     * ============================================
     * ★ 対左投手 × 対戦チーム別 AVG
     * ============================================
     */
    public Map<String, String> getVsLeftStatsByOpponentFormatted(
            String opponent,
            Integer season) {

        Map<String, Object> stats = ohtaniGameRepository.getVsLeftStatsByOpponent(
                opponent,
                season);

        int hits = stats.get("hits") != null
                ? ((Number) stats.get("hits")).intValue()
                : 0;

        int atBats = stats.get("at_bats") != null
                ? ((Number) stats.get("at_bats")).intValue()
                : 0;

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> result = new HashMap<>();
        result.put("avg", avgStr);
        result.put("detail", detail);

        return result;
    }

    /**
     * ============================================
     * ★ 対左投手 × 投手別 AVG
     * ============================================
     */
    public Map<String, String> getVsLeftStatsByPitcherFormatted(String pitcher) {

        Map<String, Object> stats = ohtaniGameRepository.getVsLeftStatsByPitcher(pitcher);

        int hits = stats.get("hits") != null
                ? ((Number) stats.get("hits")).intValue()
                : 0;

        int atBats = stats.get("at_bats") != null
                ? ((Number) stats.get("at_bats")).intValue()
                : 0;

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> result = new HashMap<>();
        result.put("avg", avgStr);
        result.put("detail", detail);

        return result;
    }

    /**
     * ============================================
     * ★ 対左投手 × 球種別 AVG
     * ============================================
     */
    public Map<String, String> getVsLeftStatsByPitchTypeFormatted(
            String pitchType,
            Integer season) {

        // ============================================
        // ★ BREAKING（変化球まとめ）対応
        // ============================================
        if ("BREAKING".equals(pitchType)) {

            List<Map<String, Object>> logs = getVsLeftLogs(
                    null,
                    null,
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

                String result = (String) row.get("result");

                if (!breakingTypes.contains(type)) {
                    continue;
                }

                // ★ AVG分母
                if (!"BB".equals(result)
                        && !"SF".equals(result)
                        && result != null) {

                    atBats++;
                }

                // ★ AVG分子
                if ("HIT".equals(result)
                        || "HR".equals(result)) {

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

        Map<String, Object> stats = ohtaniGameRepository.getVsLeftStatsByPitchType(pitchType);

        int hits = stats.get("hits") != null
                ? ((Number) stats.get("hits")).intValue()
                : 0;

        int atBats = stats.get("at_bats") != null
                ? ((Number) stats.get("at_bats")).intValue()
                : 0;

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> result = new HashMap<>();
        result.put("avg", avgStr);
        result.put("detail", detail);

        return result;
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
            Integer speedMin,
            Integer speedMax,
            Integer season) {

        List<Map<String, Object>> logs = ohtaniGameRepository.getVsLeftLogs(
                null,
                null,
                null,
                null,
                speedMin,
                speedMax,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String result = (String) row.get("result");

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
            if (!"BB".equals(result)
                    && !"SF".equals(result)
                    && result != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(result)
                    || "HR".equals(result)) {

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
            String opponent,
            Integer season) {

        Map<String, Object> result = ohtaniGameRepository.getVsAllStats(
                opponent,
                season);

        int hits = ((Number) result.get("hits")).intValue();
        int atBats = ((Number) result.get("at_bats")).intValue();

        Number avgNum = (Number) result.get("avg");
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
     * ★ 対ALL × 対戦チーム別 AVG
     * ============================================
     */
    public Map<String, String> getVsAllStatsByOpponentFormatted(
            String opponent,
            Integer season) {

        Map<String, Object> stats = ohtaniGameRepository.getVsAllStats(
                opponent,
                season);

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

        Map<String, String> result = new HashMap<>();

        result.put("avg", avgStr);
        result.put("detail", detail);

        return result;
    }

    /**
     * ============================================
     * ★ 対ALL × 投手別 AVG
     * ============================================
     */
    public Map<String, String> getVsAllStatsByPitcherFormatted(String pitcher) {

        Map<String, Object> stats = ohtaniGameRepository.getVsAllStatsByPitcher(pitcher);

        int hits = stats.get("hits") != null
                ? ((Number) stats.get("hits")).intValue()
                : 0;

        int atBats = stats.get("at_bats") != null
                ? ((Number) stats.get("at_bats")).intValue()
                : 0;

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> result = new HashMap<>();
        result.put("avg", avgStr);
        result.put("detail", detail);

        return result;
    }

    /**
     * ============================================
     * ★ 対ALL × 球種別 AVG
     * ============================================
     */
    public Map<String, String> getVsAllStatsByPitchTypeFormatted(
            String pitchType,
            Integer season) {

        // ============================================
        // ★ BREAKING（変化球と丸っと包括）でも検索できるようにする仕組み--1469行目と親子
        // ============================================
        if ("BREAKING".equals(pitchType)) {

            List<Map<String, Object>> logs = getVsAllLogs(
                    null,
                    null,
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

                String result = (String) row.get("result");

                if (!breakingTypes.contains(type)) {
                    continue;
                }

                // ★ AVG分母
                if (!"BB".equals(result)
                        && !"SF".equals(result)
                        && result != null) {

                    atBats++;
                }

                // ★ AVG分子
                if ("HIT".equals(result)
                        || "HR".equals(result)) {

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

        Map<String, Object> stats = ohtaniGameRepository.getVsAllStatsByPitchType(pitchType);

        int hits = stats.get("hits") != null
                ? ((Number) stats.get("hits")).intValue()
                : 0;

        int atBats = stats.get("at_bats") != null
                ? ((Number) stats.get("at_bats")).intValue()
                : 0;

        Double avg = stats.get("avg") != null
                ? ((Number) stats.get("avg")).doubleValue()
                : 0.0;

        String avgStr = String.format("%.3f", avg).replace("0.", ".");
        String detail = hits + "-" + atBats;

        Map<String, String> result = new HashMap<>();
        result.put("avg", avgStr);
        result.put("detail", detail);

        return result;
    }

    /**
     * ============================================
     * ★ 対ALL投手 × 球速帯 AVG
     * ============================================
     */
    public Map<String, String> getVsAllStatsBySpeedFormatted(
            Integer speedMin,
            Integer speedMax,
            Integer season) {

        List<Map<String, Object>> logs = ohtaniGameRepository.getVsAllLogs(
                null,
                null,
                null,
                null,
                speedMin,
                speedMax,
                season);

        int hits = 0;
        int atBats = 0;

        for (Map<String, Object> row : logs) {

            String result = (String) row.get("result");

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
            if (!"BB".equals(result)
                    && !"SF".equals(result)
                    && result != null) {

                atBats++;
            }

            // ★ AVG分子
            if ("HIT".equals(result)
                    || "HR".equals(result)) {

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
            Integer season) {

        return ohtaniGameRepository.getVsAllStats(season);
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
                "Four-Seam",
                "Sinker",
                "Sweeper",
                "Slider",
                "Curve",
                "Cutter",
                "Splitter",
                "Changeup",
                "Knuckle Curve",
                "Sweeping Curve"
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
}
