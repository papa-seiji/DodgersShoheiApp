package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.dto.CellDto;
import com.example.dodgersshoheiapp.model.WbcPoolMatch;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if ("C".equalsIgnoreCase(pool)) {
            return List.of(
                    "AUSTRALIA",
                    "CHINESE TAIPEI",
                    "CZECH REPUBLIC",
                    "JAPAN",
                    "KOREA");
        }

        // A / B / D は後で拡張
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
}
