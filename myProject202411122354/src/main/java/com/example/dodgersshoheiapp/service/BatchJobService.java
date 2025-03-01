package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.LoginLogoutLog;
import com.example.dodgersshoheiapp.repository.LoginLogoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class BatchJobService {

    private static final Logger logger = LoggerFactory.getLogger(BatchJobService.class);

    @Autowired
    private LoginLogoutRepository loginLogoutRepository;

    // @Scheduled(cron = "0 */2 * * * ?") // 2分ごとに実行（開発用）
    @Scheduled(cron = "0 0 6,18 * * ?") // 毎日 6:00 & 18:00 実行（本番用）
    public void processLoginLogoutLogs() {
        logger.debug("★★★ バッチ処理開始: processLoginLogoutLogs() ★★★");

        List<LoginLogoutLog> logs = loginLogoutRepository.findAll();
        logger.debug("取得したログ数: {}", logs.size());

        if (!logs.isEmpty()) {
            writeLogsToFile(logs);
            loginLogoutRepository.deleteAll();
            logger.debug("ログファイルに出力後、DBのログを削除しました。");
        } else {
            logger.debug("ログデータが存在しないため、処理をスキップしました。");
        }

        logger.debug("★★★ バッチ処理終了: processLoginLogoutLogs() ★★★");
    }

    private void writeLogsToFile(List<LoginLogoutLog> logs) {
        String filePath = "login_logout_logs.txt";

        try (FileWriter writer = new FileWriter(filePath, true)) {
            for (LoginLogoutLog log : logs) {
                writer.write(String.format("Username: %s, Action: %s, IP: %s, UserAgent: %s, Timestamp: %s%n",
                        log.getUsername(), log.getAction(), log.getIpAddress(), log.getUserAgent(),
                        log.getTimestamp()));
            }
            logger.debug("ログデータをファイルに正常に書き込みました。");
        } catch (IOException e) {
            logger.error("ログファイルへの書き込みに失敗しました。", e);
        }
    }
}
