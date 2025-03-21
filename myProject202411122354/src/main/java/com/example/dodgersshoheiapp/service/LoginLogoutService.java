package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.LoginLogoutLog;
import com.example.dodgersshoheiapp.repository.LoginLogoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.stream.Collectors;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class LoginLogoutService {

    private static final Logger logger = LoggerFactory.getLogger(LoginLogoutService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private LoginLogoutRepository loginLogoutRepository;

    private static final String LOG_FILE_PATH = "login_logout_logs.json"; // .txt → .json に変更
    private static final String ARCHIVE_FOLDER = "logs_archive";
    private static final long MAX_LOG_FILE_SIZE = 1 * 1024 * 1024; // 1MB

    public void logAction(String username, String action, String ipAddress, String userAgent) {
        try {
            LoginLogoutLog log = new LoginLogoutLog();
            log.setUsername(username);
            log.setAction(action);
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);
            log.setTimestamp(LocalDateTime.now());

            loginLogoutRepository.save(log);

            String formattedLogEntry = formatLogEntry(log);
            writeLogToFile(formattedLogEntry);

            if ("FAILED_LOGIN".equals(action)) {
                logger.warn("⚠️【ログイン失敗】ユーザー: {}, IP: {}, UserAgent: {}", username, ipAddress, userAgent);
            } else {
                logger.debug("★★★ ログイン・ログアウト情報をDB & ファイルに保存成功: {}", log);
            }
        } catch (Exception e) {
            logger.error("ログイン・ログアウト情報の保存に失敗しました", e);
        }
    }

    /**
     * JSONフォーマットでログを作成
     */
    private String formatLogEntry(LoginLogoutLog log) {
        ObjectNode logJson = objectMapper.createObjectNode();
        logJson.put("username", log.getUsername());
        logJson.put("action", log.getAction());
        logJson.put("ipAddress", normalizeIpAddress(log.getIpAddress()));
        logJson.put("userAgent", log.getUserAgent());
        logJson.put("timestamp", log.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return logJson.toString() + System.lineSeparator();
    }

    /**
     * IPアドレスを整形
     */
    private String normalizeIpAddress(String ipAddress) {
        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            return "localhost";
        }
        return ipAddress;
    }

    private void writeLogToFile(String logEntry) {
        try {
            File logFile = new File(LOG_FILE_PATH);

            // サイズチェックしてアーカイブ
            if (logFile.exists() && logFile.length() > MAX_LOG_FILE_SIZE) {
                archiveLogFile();
            }

            // ログファイルに書き込み（JSON形式）
            Files.write(Paths.get(LOG_FILE_PATH), logEntry.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("ログファイルへの書き込みに失敗しました", e);
        }
    }

    private void archiveLogFile() {
        try {
            // アーカイブフォルダ作成
            Files.createDirectories(Paths.get(ARCHIVE_FOLDER));

            // アーカイブファイル名を作成（日時を含める）
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String zipFilePath = ARCHIVE_FOLDER + "/login_logs_" + timestamp + ".zip";

            try (FileOutputStream fos = new FileOutputStream(zipFilePath);
                    ZipOutputStream zos = new ZipOutputStream(fos);
                    FileInputStream fis = new FileInputStream(LOG_FILE_PATH)) {

                ZipEntry zipEntry = new ZipEntry("login_logout_logs.json");
                zos.putNextEntry(zipEntry);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }

            // 元のログファイルを削除して、新しく作成
            Files.delete(Paths.get(LOG_FILE_PATH));
            Files.createFile(Paths.get(LOG_FILE_PATH));

            logger.info("★★★ ログをアーカイブしました: {}", zipFilePath);

            // 古いログを削除（3世代保持）
            cleanOldArchives();

        } catch (IOException e) {
            logger.error("ログアーカイブ処理に失敗しました", e);
        }
    }

    private void cleanOldArchives() {
        try {
            List<Path> archiveFiles = Files.list(Paths.get(ARCHIVE_FOLDER))
                    .filter(path -> path.toString().endsWith(".zip"))
                    .sorted((p1, p2) -> {
                        try {
                            return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .collect(Collectors.toList());

            // 最新3つだけ保持
            for (int i = 3; i < archiveFiles.size(); i++) {
                Files.delete(archiveFiles.get(i));
                logger.info("★★★ 古いアーカイブを削除しました: {}", archiveFiles.get(i));
            }
        } catch (IOException e) {
            logger.error("古いアーカイブの削除に失敗しました", e);
        }
    }
}
