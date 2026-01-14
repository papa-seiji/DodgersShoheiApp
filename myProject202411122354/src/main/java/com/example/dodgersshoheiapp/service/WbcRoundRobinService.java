package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.dto.CellDto;
import com.example.dodgersshoheiapp.dto.TeamStatDto;
import com.example.dodgersshoheiapp.model.WbcPoolMatch;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

@Service
public class WbcRoundRobinService {

    /**
     * 文字列正規化
     * ・前後空白除去
     */
    private String normalize(String s) {
        return s == null ? null : s.trim();
    }

    /**
     * POOLごとの固定チーム一覧
     */
    public List<String> getFixedTeams(String pool) {

        if ("A".equalsIgnoreCase(pool)) {
            return List.of(
                    "CUBA",
                    "PANAMA",
                    "PUERTO RICO",
                    "COLOMBIA",
                    "CANADA");
        }

        if ("B".equalsIgnoreCase(pool)) {
            return List.of(
                    "MEXICO",
                    "UNITED KINGDOM",
                    "USA",
                    "BRAZIL",
                    "ITALY");
        }

        if ("C".equalsIgnoreCase(pool)) {
            return List.of(
                    "AUSTRALIA",
                    "CHINESE TAIPEI",
                    "CZECH REPUBLIC",
                    "JAPAN",
                    "KOREA");
        }

        if ("D".equalsIgnoreCase(pool)) {
            return List.of(
                    "NETHERLANDS",
                    "VENEZUELA",
                    "NICARAGUA",
                    "DOMINICAN REPUBLIC",
                    "ISRAEL");
        }

        return List.of();
    }

    /**
     * 空の総当たりマトリクス作成
     */
    public Map<String, Map<String, CellDto>> createEmptyMatrix(List<String> teams) {

        Map<String, Map<String, CellDto>> matrix = new HashMap<>();

        for (String homeRaw : teams) {
            String home = normalize(homeRaw);

            Map<String, CellDto> row = new HashMap<>();

            for (String awayRaw : teams) {
                String away = normalize(awayRaw);

                CellDto cell = new CellDto();

                if (home.equals(away)) {
                    cell.setSelf(true);
                    cell.setFinished(false);
                } else {
                    cell.setSelf(false);
                    cell.setFinished(false);
                }

                row.put(away, cell);
            }

            matrix.put(home, row);
        }

        return matrix;
    }

    /**
     * 試合結果をマトリクスに反映
     */
    public void applyMatchesToMatrix(
            List<WbcPoolMatch> matches,
            Map<String, Map<String, CellDto>> matrix) {

        System.out.println("applyMatchesToMatrix: matches size = " + matches.size());

        for (WbcPoolMatch match : matches) {

            // status null-safe（FINISHED のみ）
            if (match.getStatus() == null ||
                    !"FINISHED".equalsIgnoreCase(match.getStatus().trim())) {
                continue;
            }

            // ★ 決定打：スコアが無い試合は完全除外
            if (match.getHomeScore() == null || match.getAwayScore() == null) {
                continue;
            }

            String home = normalize(match.getHomeTeam());
            String away = normalize(match.getAwayTeam());

            if (home == null || away == null) {
                continue;
            }

            if (!matrix.containsKey(home) || !matrix.containsKey(away)) {
                System.out.println(
                        "⚠ SKIP: matrix key not found home=" + home + " away=" + away);
                continue;
            }

            // HOME → AWAY
            CellDto homeCell = matrix.get(home).get(away);
            if (homeCell != null) {
                homeCell.setFinished(true);
                homeCell.setHomeScore(match.getHomeScore());
                homeCell.setAwayScore(match.getAwayScore());
                homeCell.setWin(match.getHomeScore() > match.getAwayScore());
            }

            // AWAY → HOME（対称セル）
            CellDto awayCell = matrix.get(away).get(home);
            if (awayCell != null) {
                awayCell.setFinished(true);
                awayCell.setHomeScore(match.getAwayScore());
                awayCell.setAwayScore(match.getHomeScore());
                awayCell.setWin(match.getAwayScore() > match.getHomeScore());
            }
        }

        // ★ 確認用ダンプ（残してOK）
        System.out.println("=== MATRIX DUMP ===");
        matrix.forEach((home, row) -> {
            System.out.println("HOME KEY = [" + home + "]");
            row.forEach((away, cell) -> {
                System.out.println(
                        "  -> AWAY KEY = [" + away + "] finished=" + cell.isFinished());
            });
        });
    }

    public Map<String, TeamStatDto> calculateTeamStats(
            List<String> teams,
            Map<String, Map<String, CellDto>> matrix) {

        Map<String, TeamStatDto> statsMap = new HashMap<>();

        // ===============================
        // 勝・敗・得失点 集計
        // ===============================
        for (String team : teams) {

            TeamStatDto stat = new TeamStatDto();
            Map<String, CellDto> row = matrix.get(team);

            if (row == null) {
                statsMap.put(team, stat);
                continue;
            }

            for (CellDto cell : row.values()) {

                if (!cell.isFinished() || cell.isSelf()) {
                    continue;
                }

                int diff = cell.getHomeScore() - cell.getAwayScore();
                stat.addRunDiff(diff);

                if (diff > 0) {
                    stat.addWin();
                } else {
                    stat.addLose();
                }
            }

            statsMap.put(team, stat);
        }

        // ===============================
        // ★ 順位決定ロジック
        // ===============================
        List<Map.Entry<String, TeamStatDto>> sorted = statsMap.entrySet().stream()
                .sorted(
                        Comparator
                                .comparing((Map.Entry<String, TeamStatDto> e) -> e.getValue().getWin())
                                .reversed()
                                .thenComparing(
                                        e -> e.getValue().getRunDiff(),
                                        Comparator.reverseOrder()))
                .toList();

        int rank = 1;
        for (Map.Entry<String, TeamStatDto> entry : sorted) {
            entry.getValue().setRank(rank++);
        }

        return statsMap;
    }

    public String teamShort(String team) {
        return switch (team) {
            case "JAPAN" -> "JPN";
            case "KOREA" -> "KOR";
            case "AUSTRALIA" -> "AUS";
            case "CANADA" -> "CAN";
            case "CUBA" -> "CUB";
            case "PANAMA" -> "PAN";
            case "PUERTO RICO" -> "PUR";
            case "COLOMBIA" -> "COL";
            case "USA" -> "USA";
            case "MEXICO" -> "MEX";
            case "BRAZIL" -> "BRA";
            case "ITALY" -> "ITA";
            case "UNITED KINGDOM" -> "GBR";
            case "NETHERLANDS" -> "NED";
            case "ISRAEL" -> "ISR";
            case "NICARAGUA" -> "NIC";
            case "DOMINICAN REPUBLIC" -> "DOM";
            case "VENEZUELA" -> "VEN";
            case "CHINESE TAIPEI" -> "TPE";
            case "CZECH REPUBLIC" -> "CZE";
            default -> team.substring(0, Math.min(3, team.length()));
        };
    }

}
