package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.WbcTournamentMatch;
import com.example.dodgersshoheiapp.repository.WbcTournamentMatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WbcTournamentService {

    private final WbcTournamentMatchRepository repository;

    public WbcTournamentService(WbcTournamentMatchRepository repository) {
        this.repository = repository;
    }

    /**
     * QF 初期化
     */
    @Transactional
    public void initQuarterFinal(
            int year,
            String poolA1, String poolA2,
            String poolB1, String poolB2,
            String poolC1, String poolC2,
            String poolD1, String poolD2) {
        repository.deleteByYearAndRound(year, "QF");

        repository.save(create(year, "QF", 1, poolA1, poolB2));
        repository.save(create(year, "QF", 2, poolB1, poolA2));
        repository.save(create(year, "QF", 3, poolC1, poolD2));
        repository.save(create(year, "QF", 4, poolD1, poolC2));
    }

    /**
     * SF 初期化
     */
    @Transactional
    public void initSemiFinal(int year) {

        repository.deleteByYearAndRound(year, "SF");

        List<WbcTournamentMatch> qf = repository.findByYearAndRoundOrderByMatchNo(year, "QF");

        if (qf.size() < 4) {
            throw new IllegalStateException("QF winners are not ready");
        }

        repository.save(create(year, "SF", 1,
                qf.get(0).getWinnerTeam(),
                qf.get(1).getWinnerTeam()));

        repository.save(create(year, "SF", 2,
                qf.get(2).getWinnerTeam(),
                qf.get(3).getWinnerTeam()));
    }

    /**
     * FINAL 初期化
     */
    @Transactional
    public void initFinal(int year) {

        repository.deleteByYearAndRound(year, "FINAL");

        List<WbcTournamentMatch> sf = repository.findByYearAndRoundOrderByMatchNo(year, "SF");

        if (sf.size() < 2) {
            throw new IllegalStateException("SF winners are not ready");
        }

        repository.save(create(year, "FINAL", 1,
                sf.get(0).getWinnerTeam(),
                sf.get(1).getWinnerTeam()));
    }

    /**
     * Champion 決定
     */
    @Transactional
    public void decideChampion(int year, String winnerTeam) {

        List<WbcTournamentMatch> finals = repository.findByYearAndRoundOrderByMatchNo(year, "FINAL");

        if (finals.isEmpty()) {
            throw new IllegalStateException("FINAL match not found");
        }

        WbcTournamentMatch match = finals.get(0);
        match.setWinnerTeam(winnerTeam);

        repository.save(match);
    }

    // ---------- 共通生成 ----------
    private WbcTournamentMatch create(
            int year, String round, int matchNo,
            String home, String away) {
        WbcTournamentMatch m = new WbcTournamentMatch();
        m.setYear(year);
        m.setRound(round);
        m.setMatchNo(matchNo);
        m.setHomeTeam(home);
        m.setAwayTeam(away);
        return m;
    }
}
