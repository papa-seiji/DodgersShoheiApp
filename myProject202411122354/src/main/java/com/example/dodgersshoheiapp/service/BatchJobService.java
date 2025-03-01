package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.repository.LoginLogoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BatchJobService {

    private static final Logger logger = LoggerFactory.getLogger(BatchJobService.class);

    @Autowired
    private LoginLogoutRepository loginLogoutRepository;

    @Scheduled(cron = "0 */2 * * * ?") // 2分ごとに実行（開発用）
    // @Scheduled(cron = "0 0 6,18 * * ?") // 毎日 6:00 & 18:00 実行（本番用）
    public void processLoginLogoutLogs() {
        logger.debug("★★★ バッチ処理開始: processLoginLogoutLogs() ★★★");

        long logCount = loginLogoutRepository.count();
        logger.debug("現在のログ件数: {}", logCount);

        if (logCount > 0) {
            loginLogoutRepository.deleteAll();
            logger.debug("DB内のログを削除しました。");
        } else {
            logger.debug("ログデータが存在しないため、削除をスキップしました。");
        }

        logger.debug("★★★ バッチ処理終了: processLoginLogoutLogs() ★★★");
    }
}
