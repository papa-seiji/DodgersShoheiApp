package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.dto.WbcPoolStandingDto;
import com.example.dodgersshoheiapp.model.WbcPoolMatch;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WbcPoolStandingService {

    /**
     * POOL順位を計算する
     *
     * @param matches 試合一覧（同一年・同POOLを想定）
     * @return 順位付き standings
     */
    public List<WbcPoolStandingDto> calculateStandings(List<WbcPoolMatch> matches) {

        Map<String, WbcPoolStandingDto> standingsMap = new HashMap<>();

        for (WbcPoolMatch match : matches) {

            // 未試合は除外（SCHEDULED 等）
            if (!"FINISHED".equals(match.getStatus())) {
                continue;
            }

            String home = match.getHomeTeam();
            String away = match.getAwayTeam();

            int homeScore = match.getHomeScore();
            int awayScore = match.getAwayScore();

            Integer year = match.getYear();
            String pool = match.getPool();

            // DTO 初期化
            standingsMap.putIfAbsent(
                    home,
                    new WbcPoolStandingDto(year, pool, home));
            standingsMap.putIfAbsent(
                    away,
                    new WbcPoolStandingDto(year, pool, away));

            WbcPoolStandingDto homeDto = standingsMap.get(home);
            WbcPoolStandingDto awayDto = standingsMap.get(away);

            // 得点・失点
            homeDto.addScore(homeScore, awayScore);
            awayDto.addScore(awayScore, homeScore);

            // 勝敗
            if (homeScore > awayScore) {
                homeDto.win();
                awayDto.lose();
            } else {
                awayDto.win();
                homeDto.lose();
            }
        }

        // 並び替え（勝数 → 得失点差）
        List<WbcPoolStandingDto> standings = standingsMap.values()
                .stream()
                .sorted(
                        Comparator
                                .comparingInt(WbcPoolStandingDto::getWins).reversed()
                                .thenComparing(
                                        Comparator.comparingInt(WbcPoolStandingDto::getRunDiff).reversed()))
                .collect(Collectors.toList());

        // 順位付与 ＋ 通過フラグ
        int rank = 1;
        for (WbcPoolStandingDto dto : standings) {
            dto.setRank(rank);
            dto.setQualified(rank <= 2); // ⭐ 1位・2位通過
            rank++;
        }

        return standings;
    }
}
