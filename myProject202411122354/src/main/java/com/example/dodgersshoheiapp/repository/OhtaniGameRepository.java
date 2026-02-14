package com.example.dodgersshoheiapp.repository;

import com.example.dodgersshoheiapp.model.OhtaniGame;
import com.example.dodgersshoheiapp.model.OhtaniGameDetail;
import com.example.dodgersshoheiapp.model.OhtaniPitchingGame;

import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
                        comment,
                        created_at,
                        game_pk          -- ★ 追加
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
     * 試合詳細（打席内容）取得
     * ★ 新設計：1試合 = 1レコード（pa1〜pa6）
     */
    public List<OhtaniGameDetail> findDetailsByGameId(int gameId) {

        String sql = """
                    SELECT
                        id,
                        game_id,
                        created_at,

                        pa1_pitcher,
                        pa1_pitcher_hand,
                        pa1_result,
                        pa1_description,

                        pa2_pitcher,
                        pa2_pitcher_hand,
                        pa2_result,
                        pa2_description,

                        pa3_pitcher,
                        pa3_pitcher_hand,
                        pa3_result,
                        pa3_description,

                        pa4_pitcher,
                        pa4_pitcher_hand,
                        pa4_result,
                        pa4_description,

                        pa5_pitcher,
                        pa5_pitcher_hand,
                        pa5_result,
                        pa5_description,

                        pa6_pitcher,
                        pa6_pitcher_hand,
                        pa6_result,
                        pa6_description

                    FROM ohtani_game_details
                    WHERE game_id = ?
                """;

        return jdbcTemplate.query(sql, ohtaniGameDetailRowMapper, gameId);
    }

    /**
     * ★ 新規追加：横持ち(pa1〜pa6)専用 RowMapper
     */
    private final RowMapper<OhtaniGameDetail> ohtaniGameDetailRowMapper = new RowMapper<>() {
        @Override
        public OhtaniGameDetail mapRow(ResultSet rs, int rowNum) throws SQLException {

            OhtaniGameDetail d = new OhtaniGameDetail();

            d.setId(rs.getLong("id"));
            d.setGameId(rs.getLong("game_id"));
            d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

            d.setPa1Pitcher(rs.getString("pa1_pitcher"));
            d.setPa1PitcherHand(rs.getString("pa1_pitcher_hand"));
            d.setPa1Result(rs.getString("pa1_result"));
            d.setPa1Description(rs.getString("pa1_description"));

            d.setPa2Pitcher(rs.getString("pa2_pitcher"));
            d.setPa2PitcherHand(rs.getString("pa2_pitcher_hand"));
            d.setPa2Result(rs.getString("pa2_result"));
            d.setPa2Description(rs.getString("pa2_description"));

            d.setPa3Pitcher(rs.getString("pa3_pitcher"));
            d.setPa3PitcherHand(rs.getString("pa3_pitcher_hand"));
            d.setPa3Result(rs.getString("pa3_result"));
            d.setPa3Description(rs.getString("pa3_description"));

            d.setPa4Pitcher(rs.getString("pa4_pitcher"));
            d.setPa4PitcherHand(rs.getString("pa4_pitcher_hand"));
            d.setPa4Result(rs.getString("pa4_result"));
            d.setPa4Description(rs.getString("pa4_description"));

            d.setPa5Pitcher(rs.getString("pa5_pitcher"));
            d.setPa5PitcherHand(rs.getString("pa5_pitcher_hand"));
            d.setPa5Result(rs.getString("pa5_result"));
            d.setPa5Description(rs.getString("pa5_description"));

            d.setPa6Pitcher(rs.getString("pa6_pitcher"));
            d.setPa6PitcherHand(rs.getString("pa6_pitcher_hand"));
            d.setPa6Result(rs.getString("pa6_result"));
            d.setPa6Description(rs.getString("pa6_description"));

            return d;
        }
    };

    /**
     * 期間指定で試合取得（旬平均・グラフ用）
     */
    public List<OhtaniGame> findGamesBetween(LocalDate start, LocalDate end) {

        String sql = """
                    SELECT
                        id,
                        game_date,
                        opponent,
                        result,
                        form_value,
                        comment,
                        created_at
                    FROM ohtani_games
                    WHERE game_date BETWEEN ? AND ?
                    ORDER BY game_date
                """;

        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(OhtaniGame.class),
                start,
                end);
    }

    public OhtaniGame findLatestGame() {

        String sql = """
                    SELECT
                        id,
                        game_date,
                        opponent,
                        result,
                        form_value,
                        comment,
                        created_at
                    FROM ohtani_games
                    ORDER BY game_date DESC
                    LIMIT 1
                """;

        return jdbcTemplate.queryForObject(
                sql,
                new BeanPropertyRowMapper<>(OhtaniGame.class));
    }

    /**
     * ★ 最新試合日を取得（hogehoge_01 用）
     */
    public LocalDate findLatestGameDate() {

        String sql = """
                    SELECT game_date
                    FROM ohtani_games
                    ORDER BY game_date DESC
                    LIMIT 1
                """;

        return jdbcTemplate.queryForObject(sql, LocalDate.class);
    }

    public interface OhtaniPitchingGameRepository
            extends JpaRepository<OhtaniPitchingGame, Long> {

        OhtaniPitchingGame findTopByOrderByGameDateDesc();
    }

}
