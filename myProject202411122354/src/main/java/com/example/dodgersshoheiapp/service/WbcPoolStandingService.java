package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.dto.WbcPoolStandingDto;
import com.example.dodgersshoheiapp.model.WbcPoolMatch;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WbcPoolStandingService {

    public List<WbcPoolStandingDto> calculateStandings(
            int year,
            String pool,
            List<String> teams,
            List<WbcPoolMatch> matches) {

        Map<String, WbcPoolStandingDto> standingsMap = new LinkedHashMap<>();

        // ① 全チーム初期化
        for (String team : teams) {
            standingsMap.put(
                    team,
                    new WbcPoolStandingDto(year, pool, team));
        }

        // ② 試合反映
        for (WbcPoolMatch match : matches) {

            if (!"FINISHED".equals(match.getStatus())) {
                continue;
            }

            WbcPoolStandingDto home = standingsMap.get(match.getHomeTeam());
            WbcPoolStandingDto away = standingsMap.get(match.getAwayTeam());

            if (home == null || away == null) {
                continue;
            }

            int homeScore = match.getHomeScore();
            int awayScore = match.getAwayScore();

            home.addScore(homeScore, awayScore);
            away.addScore(awayScore, homeScore);

            if (homeScore > awayScore) {
                home.win();
                away.lose();
            } else {
                away.win();
                home.lose();
            }
        }

        // ③ 並び替え（★修正ポイント）
        List<WbcPoolStandingDto> standings = new ArrayList<>(standingsMap.values());

        standings.sort(
                Comparator
                        .comparingInt(WbcPoolStandingDto::getWins)
                        .thenComparingInt(WbcPoolStandingDto::getRunDiff)
                        .reversed());

        // ④ 順位付与
        int rank = 1;
        for (WbcPoolStandingDto dto : standings) {
            dto.setRank(rank++);
        }

        return standings;
    }
}
