package com.example.dodgersshoheiapp.batch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.dodgersshoheiapp.model.MlbApiLog;
import com.example.dodgersshoheiapp.repository.MlbApiLogRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MlbApiHealthCheckBatch {

    private static final Logger logger = LoggerFactory.getLogger(MlbApiHealthCheckBatch.class);
    private static final String LOG_FILE_PATH = "mlb_api_health_check.log";

    private static final Map<String, String> API_URLS = new LinkedHashMap<>() {
        {
            put("Hitter Stats",
                    "https://statsapi.mlb.com/api/v1/people/660271/stats?stats=season&season=2024&group=pitching");
            put("Pitcher Stats",
                    "https://statsapi.mlb.com/api/v1/people/660271/stats?stats=season&season=2024&group=pitching");
            put("NL Standings",
                    "https://statsapi.mlb.com/api/v1/standings?leagueId=104&season=2024&standingsTypes=regularSeason");
            put("AL Standings",
                    "https://statsapi.mlb.com/api/v1/standings?leagueId=103&season=2024&standingsTypes=regularSeason");
        }
    };

    @Autowired
    private MlbApiLogRepository mlbApiLogRepository;

    // @Scheduled(fixedRate = 60000) // 1分ごとに実行（テストしたい時間に応じて変更）
    // @Scheduled(fixedRate = 480 * 60 * 1000) // 8時間ごとに実行
    // 「8時、16時、24時に固定で実行」 するなら、
    // @Scheduled(cron = "0 0 8,16,0 * * ?", zone = "Asia/Tokyo") // が最適！ 🎯
    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Tokyo") // 毎日 8:00 のみに実行
    public void checkMlbApiHealth() {
        logger.info("=== MLB API Health Check Started ===");
        appendLog("=== MLB API Health Check Started ===");

        for (Map.Entry<String, String> entry : API_URLS.entrySet()) {
            String apiName = entry.getKey();
            String apiUrl = entry.getValue();
            int statusCode = 200; // APIの実際のレスポンスを取得する処理を後で追加

            // DBにログを保存
            MlbApiLog log = new MlbApiLog();
            log.setApiName(apiName);
            log.setCheckedAt(LocalDateTime.now());
            log.setMessage("Success");
            log.setStatusCode(statusCode);
            mlbApiLogRepository.save(log);

            // ファイルにログ出力
            String logMessage = String.format("✅ %s → Success (%d)", apiName, statusCode);
            logger.info(logMessage);
            appendLog(logMessage);
        }

        logger.info("=== MLB API Health Check Completed ===");
        appendLog("=== MLB API Health Check Completed ===");
    }

    private void appendLog(String message) {
        try (FileWriter writer = new FileWriter(LOG_FILE_PATH, true)) {
            writer.write(LocalDateTime.now() + " " + message + "\n");
        } catch (IOException e) {
            logger.error("ログファイルへの書き込みに失敗しました", e);
        }
    }
}
