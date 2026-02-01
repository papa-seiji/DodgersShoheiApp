package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.OhtaniGame;
import com.example.dodgersshoheiapp.model.OhtaniGameDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OhtaniGameRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 月別 試合一覧取得（例：2026年4月）
     */
    public List<OhtaniGame> findGamesByMonth(int year, int month) {

        String sql = """
                    SELECT
                        id,
                        game_date,
                        opponent,
                        result,
                        form_value,
                        created_at
                    FROM ohtani_games
                    WHERE EXTRACT(YEAR FROM game_date) = ?
                      AND EXTRACT(MONTH FROM game_date) = ?
                    ORDER BY game_date
                """;

        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(OhtaniGame.class),
                year,
                month);
    }

    /**
     * 試合詳細（打席）取得
     */
    public List<OhtaniGameDetail> findDetailsByGameId(int gameId) {

        String sql = """
                    SELECT
                        id,
                        game_id,
                        batting_order,
                        pitcher,
                        pitcher_hand,
                        result,
                        created_at
                    FROM ohtani_game_details
                    WHERE game_id = ?
                    ORDER BY batting_order
                """;

        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(OhtaniGameDetail.class),
                gameId);
    }
}
