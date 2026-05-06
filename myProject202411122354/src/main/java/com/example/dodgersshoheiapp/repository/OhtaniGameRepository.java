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
import java.util.Map;// ←追加

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

    // ★ 追加：game_pk 更新用
    public void updateGamePk(int gameId, Long gamePk) {

        String sql = """
                UPDATE ohtani_games
                SET game_pk = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(sql, gamePk, gameId);
    }

    /**
     * ============================================
     * ★ 得点圏打率（RISP）取得
     * ============================================
     */
    public Map<String, Object> getRispStats() {

        String sql = """
                    SELECT
                        SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) AS hits,
                        SUM(CASE WHEN result NOT IN ('BB','SF') AND result IS NOT NULL THEN 1 ELSE 0 END) AS at_bats,
                        ROUND(
                            SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) * 1.0
                            /
                            NULLIF(
                                SUM(CASE WHEN result NOT IN ('BB','SF') AND result IS NOT NULL THEN 1 ELSE 0 END),
                                0
                            )
                        , 3) AS avg
                    FROM (
                        SELECT pa1_result AS result FROM ohtani_game_details WHERE pa1_description LIKE '%得点圏にランナー有%'
                        UNION ALL
                        SELECT pa2_result FROM ohtani_game_details WHERE pa2_description LIKE '%得点圏にランナー有%'
                        UNION ALL
                        SELECT pa3_result FROM ohtani_game_details WHERE pa3_description LIKE '%得点圏にランナー有%'
                        UNION ALL
                        SELECT pa4_result FROM ohtani_game_details WHERE pa4_description LIKE '%得点圏にランナー有%'
                        UNION ALL
                        SELECT pa5_result FROM ohtani_game_details WHERE pa5_description LIKE '%得点圏にランナー有%'
                        UNION ALL
                        SELECT pa6_result FROM ohtani_game_details WHERE pa6_description LIKE '%得点圏にランナー有%'
                    ) t
                """;

        return jdbcTemplate.queryForMap(sql);
    }

    /**
     * ============================================
     * ★ 対右ピッチャー打率（VS R）「avgだけ」になってる
     * ============================================
     */
    public Double getVsRightAvg() {

        String sql = """
                    SELECT
                        ROUND(
                            SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) * 1.0
                            /
                            NULLIF(
                                SUM(CASE WHEN result NOT IN ('BB','SF') AND result IS NOT NULL THEN 1 ELSE 0 END),
                                0
                            )
                        , 3) AS avg
                    FROM (
                        SELECT pa1_result AS result FROM ohtani_game_details WHERE pa1_pitcher_hand = 'R'
                        UNION ALL
                        SELECT pa2_result FROM ohtani_game_details WHERE pa2_pitcher_hand = 'R'
                        UNION ALL
                        SELECT pa3_result FROM ohtani_game_details WHERE pa3_pitcher_hand = 'R'
                        UNION ALL
                        SELECT pa4_result FROM ohtani_game_details WHERE pa4_pitcher_hand = 'R'
                        UNION ALL
                        SELECT pa5_result FROM ohtani_game_details WHERE pa5_pitcher_hand = 'R'
                        UNION ALL
                        SELECT pa6_result FROM ohtani_game_details WHERE pa6_pitcher_hand = 'R'
                    ) t
                """;

        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    /**
     * ============================================
     * ★ 対右ピッチャー👉 hits / at_bats も取る必要あり
     * ============================================
     */
    public Map<String, Object> getVsRightStats() {

        String sql = """
                    SELECT
                        SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) AS hits,
                        SUM(CASE WHEN result NOT IN ('BB','SF') AND result IS NOT NULL THEN 1 ELSE 0 END) AS at_bats,
                        ROUND(
                            SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) * 1.0
                            /
                            NULLIF(
                                SUM(CASE WHEN result NOT IN ('BB','SF') AND result IS NOT NULL THEN 1 ELSE 0 END),
                                0
                            )
                        , 3) AS avg
                    FROM (
                        SELECT pa1_result AS result FROM ohtani_game_details WHERE pa1_pitcher_hand = 'R'
                        UNION ALL
                        SELECT pa2_result FROM ohtani_game_details WHERE pa2_pitcher_hand = 'R'
                        UNION ALL
                        SELECT pa3_result FROM ohtani_game_details WHERE pa3_pitcher_hand = 'R'
                        UNION ALL
                        SELECT pa4_result FROM ohtani_game_details WHERE pa4_pitcher_hand = 'R'
                        UNION ALL
                        SELECT pa5_result FROM ohtani_game_details WHERE pa5_pitcher_hand = 'R'
                        UNION ALL
                        SELECT pa6_result FROM ohtani_game_details WHERE pa6_pitcher_hand = 'R'
                    ) t
                """;

        return jdbcTemplate.queryForMap(sql);
    }

    /**
     * ============================================
     * ★ 対右投手 × 対戦チーム別 AVG
     * ============================================
     */
    public Map<String, Object> getVsRightStatsByOpponent(String opponent) {

        String sql = """
                    SELECT
                        SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) AS hits,

                        SUM(
                            CASE
                                WHEN result NOT IN ('BB','SF')
                                AND result IS NOT NULL
                                THEN 1
                                ELSE 0
                            END
                        ) AS at_bats,

                        ROUND(
                            SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) * 1.0
                            /
                            NULLIF(
                                SUM(
                                    CASE
                                        WHEN result NOT IN ('BB','SF')
                                        AND result IS NOT NULL
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            )
                        , 3) AS avg

                    FROM (

                        SELECT
                            pa1_result AS result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE pa1_pitcher_hand = 'R'
                        AND g.opponent = ?

                        UNION ALL

                        SELECT
                            pa2_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE pa2_pitcher_hand = 'R'
                        AND g.opponent = ?

                        UNION ALL

                        SELECT
                            pa3_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE pa3_pitcher_hand = 'R'
                        AND g.opponent = ?

                        UNION ALL

                        SELECT
                            pa4_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE pa4_pitcher_hand = 'R'
                        AND g.opponent = ?

                        UNION ALL

                        SELECT
                            pa5_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE pa5_pitcher_hand = 'R'
                        AND g.opponent = ?

                        UNION ALL

                        SELECT
                            pa6_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE pa6_pitcher_hand = 'R'
                        AND g.opponent = ?

                    ) t
                """;

        return jdbcTemplate.queryForMap(
                sql,
                opponent,
                opponent,
                opponent,
                opponent,
                opponent,
                opponent);
    }

    /**
     * ============================================
     * ★ 対右投手 × 投手別 AVG
     * ============================================
     */
    public Map<String, Object> getVsRightStatsByPitcher(String pitcher) {

        String sql = """
                    SELECT
                        SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) AS hits,

                        SUM(
                            CASE
                                WHEN result NOT IN ('BB','SF')
                                AND result IS NOT NULL
                                THEN 1
                                ELSE 0
                            END
                        ) AS at_bats,

                        ROUND(
                            SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) * 1.0
                            /
                            NULLIF(
                                SUM(
                                    CASE
                                        WHEN result NOT IN ('BB','SF')
                                        AND result IS NOT NULL
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            )
                        , 3) AS avg

                    FROM (

                        SELECT pa1_result AS result
                        FROM ohtani_game_details
                        WHERE pa1_pitcher_hand = 'R'
                        AND pa1_pitcher = ?

                        UNION ALL

                        SELECT pa2_result
                        FROM ohtani_game_details
                        WHERE pa2_pitcher_hand = 'R'
                        AND pa2_pitcher = ?

                        UNION ALL

                        SELECT pa3_result
                        FROM ohtani_game_details
                        WHERE pa3_pitcher_hand = 'R'
                        AND pa3_pitcher = ?

                        UNION ALL

                        SELECT pa4_result
                        FROM ohtani_game_details
                        WHERE pa4_pitcher_hand = 'R'
                        AND pa4_pitcher = ?

                        UNION ALL

                        SELECT pa5_result
                        FROM ohtani_game_details
                        WHERE pa5_pitcher_hand = 'R'
                        AND pa5_pitcher = ?

                        UNION ALL

                        SELECT pa6_result
                        FROM ohtani_game_details
                        WHERE pa6_pitcher_hand = 'R'
                        AND pa6_pitcher = ?

                    ) t
                """;

        return jdbcTemplate.queryForMap(
                sql,
                pitcher,
                pitcher,
                pitcher,
                pitcher,
                pitcher,
                pitcher);
    }

    /**
     * ============================================
     * ★ 対右ピッチャー（ログ取得）最終版
     * （CAST維持＋result＋opponent＋pitcher＋pitchType＋speedRange）
     * ============================================
     */
    public List<Map<String, Object>> getVsRightLogs(
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer speedMin,
            Integer speedMax) {

        String sql = """
                    SELECT game_date, opponent, pitcher, hand, result, description
                    FROM (
                        SELECT g.game_date,
                            g.opponent,
                            d.pa1_pitcher AS pitcher,
                            d.pa1_pitcher_hand AS hand,
                            d.pa1_result AS result,
                            d.pa1_description AS description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE d.pa1_pitcher_hand = 'R'
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa1_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa1_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa1_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa1_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa1_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date, g.opponent, d.pa2_pitcher, d.pa2_pitcher_hand, d.pa2_result, d.pa2_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE d.pa2_pitcher_hand = 'R'
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa2_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa2_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa2_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa2_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa2_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date, g.opponent, d.pa3_pitcher, d.pa3_pitcher_hand, d.pa3_result, d.pa3_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE d.pa3_pitcher_hand = 'R'
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa3_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa3_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa3_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa3_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa3_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date, g.opponent, d.pa4_pitcher, d.pa4_pitcher_hand, d.pa4_result, d.pa4_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE d.pa4_pitcher_hand = 'R'
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa4_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa4_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa4_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa4_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa4_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date, g.opponent, d.pa5_pitcher, d.pa5_pitcher_hand, d.pa5_result, d.pa5_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE d.pa5_pitcher_hand = 'R'
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa5_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa5_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa5_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa5_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa5_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date, g.opponent, d.pa6_pitcher, d.pa6_pitcher_hand, d.pa6_result, d.pa6_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE d.pa6_pitcher_hand = 'R'
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa6_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa6_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa6_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa6_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa6_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )
                    ) t
                    ORDER BY game_date DESC
                """;

        return jdbcTemplate.queryForList(
                sql,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax);
    }

    /**
     * ============================================
     * ★ 対左ピッチャー打率（VS L）「avgだけ」になってる
     * ============================================
     */
    public Map<String, Object> getVsLeftStats() {

        String sql = """
                    SELECT
                        SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) AS hits,
                        SUM(CASE WHEN result NOT IN ('BB','SF') AND result IS NOT NULL THEN 1 ELSE 0 END) AS at_bats,
                        ROUND(
                            SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) * 1.0
                            /
                            NULLIF(
                                SUM(CASE WHEN result NOT IN ('BB','SF') AND result IS NOT NULL THEN 1 ELSE 0 END),
                                0
                            )
                        , 3) AS avg
                    FROM (
                        SELECT pa1_result AS result FROM ohtani_game_details WHERE pa1_pitcher_hand = 'L'
                        UNION ALL
                        SELECT pa2_result FROM ohtani_game_details WHERE pa2_pitcher_hand = 'L'
                        UNION ALL
                        SELECT pa3_result FROM ohtani_game_details WHERE pa3_pitcher_hand = 'L'
                        UNION ALL
                        SELECT pa4_result FROM ohtani_game_details WHERE pa4_pitcher_hand = 'L'
                        UNION ALL
                        SELECT pa5_result FROM ohtani_game_details WHERE pa5_pitcher_hand = 'L'
                        UNION ALL
                        SELECT pa6_result FROM ohtani_game_details WHERE pa6_pitcher_hand = 'L'
                    ) t
                """;

        return jdbcTemplate.queryForMap(sql);
    }

    /**
     * ============================================
     * ★ 対左投手 × 対戦チーム別 AVG
     * ============================================
     */
    public Map<String, Object> getVsLeftStatsByOpponent(String opponent) {

        String sql = """
                    SELECT
                        SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) AS hits,

                        SUM(
                            CASE
                                WHEN result NOT IN ('BB','SF')
                                AND result IS NOT NULL
                                THEN 1
                                ELSE 0
                            END
                        ) AS at_bats,

                        ROUND(
                            SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) * 1.0
                            /
                            NULLIF(
                                SUM(
                                    CASE
                                        WHEN result NOT IN ('BB','SF')
                                        AND result IS NOT NULL
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            )
                        , 3) AS avg

                    FROM (

                        SELECT
                            pa1_result AS result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE pa1_pitcher_hand = 'L'
                        AND g.opponent = ?

                        UNION ALL

                        SELECT
                            pa2_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE pa2_pitcher_hand = 'L'
                        AND g.opponent = ?

                        UNION ALL

                        SELECT
                            pa3_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE pa3_pitcher_hand = 'L'
                        AND g.opponent = ?

                        UNION ALL

                        SELECT
                            pa4_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE pa4_pitcher_hand = 'L'
                        AND g.opponent = ?

                        UNION ALL

                        SELECT
                            pa5_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE pa5_pitcher_hand = 'L'
                        AND g.opponent = ?

                        UNION ALL

                        SELECT
                            pa6_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE pa6_pitcher_hand = 'L'
                        AND g.opponent = ?

                    ) t
                """;

        return jdbcTemplate.queryForMap(
                sql,
                opponent,
                opponent,
                opponent,
                opponent,
                opponent,
                opponent);
    }

    /**
     * ============================================
     * ★ 対左投手 × 投手別 AVG
     * ============================================
     */
    public Map<String, Object> getVsLeftStatsByPitcher(String pitcher) {

        String sql = """
                    SELECT
                        SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) AS hits,

                        SUM(
                            CASE
                                WHEN result NOT IN ('BB','SF')
                                AND result IS NOT NULL
                                THEN 1
                                ELSE 0
                            END
                        ) AS at_bats,

                        ROUND(
                            SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) * 1.0
                            /
                            NULLIF(
                                SUM(
                                    CASE
                                        WHEN result NOT IN ('BB','SF')
                                        AND result IS NOT NULL
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            )
                        , 3) AS avg

                    FROM (

                        SELECT pa1_result AS result
                        FROM ohtani_game_details
                        WHERE pa1_pitcher_hand = 'L'
                        AND pa1_pitcher = ?

                        UNION ALL

                        SELECT pa2_result
                        FROM ohtani_game_details
                        WHERE pa2_pitcher_hand = 'L'
                        AND pa2_pitcher = ?

                        UNION ALL

                        SELECT pa3_result
                        FROM ohtani_game_details
                        WHERE pa3_pitcher_hand = 'L'
                        AND pa3_pitcher = ?

                        UNION ALL

                        SELECT pa4_result
                        FROM ohtani_game_details
                        WHERE pa4_pitcher_hand = 'L'
                        AND pa4_pitcher = ?

                        UNION ALL

                        SELECT pa5_result
                        FROM ohtani_game_details
                        WHERE pa5_pitcher_hand = 'L'
                        AND pa5_pitcher = ?

                        UNION ALL

                        SELECT pa6_result
                        FROM ohtani_game_details
                        WHERE pa6_pitcher_hand = 'L'
                        AND pa6_pitcher = ?

                    ) t
                """;

        return jdbcTemplate.queryForMap(
                sql,
                pitcher,
                pitcher,
                pitcher,
                pitcher,
                pitcher,
                pitcher);
    }

    /**
     * ============================================
     * ★ 対左ピッチャー（ログ取得）最終版
     * （CAST維持＋result＋opponent＋pitcher＋pitchType＋speedRange）
     * ============================================
     */
    public List<Map<String, Object>> getVsLeftLogs(
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer speedMin,
            Integer speedMax) {

        String sql = """
                    SELECT game_date, opponent, pitcher, hand, result, description
                    FROM (
                        SELECT g.game_date,
                            g.opponent,
                            d.pa1_pitcher AS pitcher,
                            d.pa1_pitcher_hand AS hand,
                            d.pa1_result AS result,
                            d.pa1_description AS description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE d.pa1_pitcher_hand = 'L'
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa1_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa1_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa1_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa1_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa1_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date, g.opponent, d.pa2_pitcher, d.pa2_pitcher_hand, d.pa2_result, d.pa2_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE d.pa2_pitcher_hand = 'L'
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa2_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa2_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa2_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa2_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa2_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date, g.opponent, d.pa3_pitcher, d.pa3_pitcher_hand, d.pa3_result, d.pa3_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE d.pa3_pitcher_hand = 'L'
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa3_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa3_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa3_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa3_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa3_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date, g.opponent, d.pa4_pitcher, d.pa4_pitcher_hand, d.pa4_result, d.pa4_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE d.pa4_pitcher_hand = 'L'
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa4_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa4_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa4_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa4_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa4_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date, g.opponent, d.pa5_pitcher, d.pa5_pitcher_hand, d.pa5_result, d.pa5_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE d.pa5_pitcher_hand = 'L'
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa5_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa5_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa5_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa5_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa5_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date, g.opponent, d.pa6_pitcher, d.pa6_pitcher_hand, d.pa6_result, d.pa6_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE d.pa6_pitcher_hand = 'L'
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa6_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa6_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa6_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa6_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa6_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )
                    ) t
                    ORDER BY game_date DESC
                """;

        return jdbcTemplate.queryForList(
                sql,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax);
    }

    /**
     * ============================================
     * ★ 対ALLピッチャー打率（内部）
     * ============================================
     */
    public Map<String, Object> getVsAllStats() {

        String sql = """
                    SELECT
                        SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) AS hits,
                        SUM(CASE WHEN result NOT IN ('BB','SF') AND result IS NOT NULL THEN 1 ELSE 0 END) AS at_bats,
                        ROUND(
                            SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) * 1.0
                            /
                            NULLIF(
                                SUM(CASE WHEN result NOT IN ('BB','SF') AND result IS NOT NULL THEN 1 ELSE 0 END),
                                0
                            )
                        , 3) AS avg
                    FROM (
                        SELECT pa1_result AS result FROM ohtani_game_details
                        UNION ALL
                        SELECT pa2_result FROM ohtani_game_details
                        UNION ALL
                        SELECT pa3_result FROM ohtani_game_details
                        UNION ALL
                        SELECT pa4_result FROM ohtani_game_details
                        UNION ALL
                        SELECT pa5_result FROM ohtani_game_details
                        UNION ALL
                        SELECT pa6_result FROM ohtani_game_details
                    ) t
                """;

        return jdbcTemplate.queryForMap(sql);
    }

    /**
     * ============================================
     * ★ 対ALL × 対戦チーム別 AVG
     * ============================================
     */
    public Map<String, Object> getVsAllStatsByOpponent(String opponent) {

        String sql = """
                    SELECT
                        SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) AS hits,

                        SUM(
                            CASE
                                WHEN result NOT IN ('BB','SF')
                                AND result IS NOT NULL
                                THEN 1
                                ELSE 0
                            END
                        ) AS at_bats,

                        ROUND(
                            SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) * 1.0
                            /
                            NULLIF(
                                SUM(
                                    CASE
                                        WHEN result NOT IN ('BB','SF')
                                        AND result IS NOT NULL
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            )
                        , 3) AS avg

                    FROM (

                        SELECT pa1_result AS result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE g.opponent = ?

                        UNION ALL

                        SELECT pa2_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE g.opponent = ?

                        UNION ALL

                        SELECT pa3_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE g.opponent = ?

                        UNION ALL

                        SELECT pa4_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE g.opponent = ?

                        UNION ALL

                        SELECT pa5_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE g.opponent = ?

                        UNION ALL

                        SELECT pa6_result
                        FROM ohtani_game_details d
                        JOIN ohtani_games g
                            ON d.game_id = g.id
                        WHERE g.opponent = ?

                    ) t
                """;

        return jdbcTemplate.queryForMap(
                sql,
                opponent,
                opponent,
                opponent,
                opponent,
                opponent,
                opponent);
    }

    /**
     * ============================================
     * ★ 対ALL × 投手別 AVG
     * ============================================
     */
    public Map<String, Object> getVsAllStatsByPitcher(String pitcher) {

        String sql = """
                    SELECT
                        SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) AS hits,

                        SUM(
                            CASE
                                WHEN result NOT IN ('BB','SF')
                                AND result IS NOT NULL
                                THEN 1
                                ELSE 0
                            END
                        ) AS at_bats,

                        ROUND(
                            SUM(CASE WHEN result IN ('HIT','HR') THEN 1 ELSE 0 END) * 1.0
                            /
                            NULLIF(
                                SUM(
                                    CASE
                                        WHEN result NOT IN ('BB','SF')
                                        AND result IS NOT NULL
                                        THEN 1
                                        ELSE 0
                                    END
                                ),
                                0
                            )
                        , 3) AS avg

                    FROM (

                        SELECT pa1_result AS result
                        FROM ohtani_game_details
                        WHERE pa1_pitcher = ?

                        UNION ALL

                        SELECT pa2_result
                        FROM ohtani_game_details
                        WHERE pa2_pitcher = ?

                        UNION ALL

                        SELECT pa3_result
                        FROM ohtani_game_details
                        WHERE pa3_pitcher = ?

                        UNION ALL

                        SELECT pa4_result
                        FROM ohtani_game_details
                        WHERE pa4_pitcher = ?

                        UNION ALL

                        SELECT pa5_result
                        FROM ohtani_game_details
                        WHERE pa5_pitcher = ?

                        UNION ALL

                        SELECT pa6_result
                        FROM ohtani_game_details
                        WHERE pa6_pitcher = ?

                    ) t
                """;

        return jdbcTemplate.queryForMap(
                sql,
                pitcher,
                pitcher,
                pitcher,
                pitcher,
                pitcher,
                pitcher);
    }

    /**
     * ============================================
     * ★ ALL（左右両方）（ログ取得）最終版
     * （CAST維持＋result＋opponent＋pitcher＋pitchType＋speedRange）
     * ============================================
     */
    public List<Map<String, Object>> getVsAllLogs(
            String result,
            String opponent,
            String pitcher,
            String pitchType,
            Integer speedMin,
            Integer speedMax) {

        String sql = """
                    SELECT game_date, opponent, pitcher, hand, result, description
                    FROM (
                        SELECT g.game_date,
                            g.opponent,
                            d.pa1_pitcher AS pitcher,
                            d.pa1_pitcher_hand AS hand,
                            d.pa1_result AS result,
                            d.pa1_description AS description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE 1=1
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa1_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa1_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa1_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa1_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa1_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date,
                            g.opponent,
                            d.pa2_pitcher,
                            d.pa2_pitcher_hand,
                            d.pa2_result,
                            d.pa2_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE 1=1
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa2_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa2_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa2_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa2_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa2_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date,
                            g.opponent,
                            d.pa3_pitcher,
                            d.pa3_pitcher_hand,
                            d.pa3_result,
                            d.pa3_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE 1=1
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa3_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa3_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa3_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa3_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa3_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date,
                            g.opponent,
                            d.pa4_pitcher,
                            d.pa4_pitcher_hand,
                            d.pa4_result,
                            d.pa4_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE 1=1
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa4_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa4_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa4_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa4_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa4_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date,
                            g.opponent,
                            d.pa5_pitcher,
                            d.pa5_pitcher_hand,
                            d.pa5_result,
                            d.pa5_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE 1=1
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa5_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa5_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa5_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa5_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa5_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )

                        UNION ALL

                        SELECT g.game_date,
                            g.opponent,
                            d.pa6_pitcher,
                            d.pa6_pitcher_hand,
                            d.pa6_result,
                            d.pa6_description
                        FROM ohtani_game_details d
                        JOIN ohtani_games g ON d.game_id = g.id
                        WHERE 1=1
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa6_result = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR g.opponent = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa6_pitcher = CAST(? AS TEXT))
                        AND (CAST(? AS TEXT) IS NULL OR CAST(? AS TEXT) = '' OR d.pa6_description LIKE '%' || CAST(? AS TEXT) || '%')
                        AND (
                            CAST(? AS NUMERIC) IS NULL
                            OR CAST(? AS NUMERIC) IS NULL
                            OR (
                                substring(d.pa6_description FROM '([0-9]+\\.?[0-9]*)mph') IS NOT NULL
                                AND CAST(substring(d.pa6_description FROM '([0-9]+\\.?[0-9]*)mph') AS NUMERIC)
                                    BETWEEN CAST(? AS NUMERIC) AND CAST(? AS NUMERIC)
                            )
                        )
                    ) t
                    ORDER BY game_date DESC
                """;

        return jdbcTemplate.queryForList(
                sql,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax,

                result, result, result,
                opponent, opponent, opponent,
                pitcher, pitcher, pitcher,
                pitchType, pitchType, pitchType,
                speedMin, speedMax, speedMin, speedMax);
    }

    /**
     * ============================================
     * ★ 対戦チーム一覧取得（重複なし）
     * ============================================
     */
    public List<String> getAllOpponents() {

        String sql = """
                    SELECT DISTINCT opponent
                    FROM ohtani_games
                    ORDER BY opponent
                """;

        return jdbcTemplate.queryForList(sql, String.class);
    }

    /**
     * ============================================
     * ★ 投手サジェスト検索（部分一致）
     * ============================================
     */
    public List<String> searchPitchers(String keyword) {

        String sql = """
                    SELECT DISTINCT pitcher FROM (
                        SELECT pa1_pitcher AS pitcher FROM ohtani_game_details
                        UNION
                        SELECT pa2_pitcher FROM ohtani_game_details
                        UNION
                        SELECT pa3_pitcher FROM ohtani_game_details
                        UNION
                        SELECT pa4_pitcher FROM ohtani_game_details
                        UNION
                        SELECT pa5_pitcher FROM ohtani_game_details
                        UNION
                        SELECT pa6_pitcher FROM ohtani_game_details
                    ) t
                    WHERE pitcher IS NOT NULL
                    AND LOWER(pitcher) LIKE LOWER(?)
                    ORDER BY pitcher
                    LIMIT 10
                """;

        return jdbcTemplate.queryForList(sql, String.class, "%" + keyword + "%");
    }

    /**
     * ============================================
     * ★ 投手の左右取得（左右が違えば警告を出して再入力を促す）
     * ============================================
     */
    public String getPitcherHand(String pitcher) {

        String sql = """
                    SELECT hand
                    FROM (
                        SELECT pa1_pitcher AS pitcher,
                            pa1_pitcher_hand AS hand
                        FROM ohtani_game_details

                        UNION ALL

                        SELECT pa2_pitcher,
                            pa2_pitcher_hand
                        FROM ohtani_game_details

                        UNION ALL

                        SELECT pa3_pitcher,
                            pa3_pitcher_hand
                        FROM ohtani_game_details

                        UNION ALL

                        SELECT pa4_pitcher,
                            pa4_pitcher_hand
                        FROM ohtani_game_details

                        UNION ALL

                        SELECT pa5_pitcher,
                            pa5_pitcher_hand
                        FROM ohtani_game_details

                        UNION ALL

                        SELECT pa6_pitcher,
                            pa6_pitcher_hand
                        FROM ohtani_game_details
                    ) t
                    WHERE pitcher = ?
                    AND hand IS NOT NULL
                    LIMIT 1
                """;

        List<String> result = jdbcTemplate.queryForList(
                sql,
                String.class,
                pitcher);

        return result.isEmpty() ? null : result.get(0);
    }
}
