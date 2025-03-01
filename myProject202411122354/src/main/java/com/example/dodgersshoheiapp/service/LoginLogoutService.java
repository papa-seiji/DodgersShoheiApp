package com.example.dodgersshoheiapp.service;

import com.example.dodgersshoheiapp.model.LoginLogoutLog;
import com.example.dodgersshoheiapp.repository.LoginLogoutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginLogoutService {

    private static final Logger logger = LoggerFactory.getLogger(LoginLogoutService.class);

    @Autowired
    private LoginLogoutRepository loginLogoutRepository;

    @Transactional
    public void logAction(String username, String action, String ipAddress, String userAgent) {
        try {
            LoginLogoutLog log = new LoginLogoutLog();
            log.setUsername(username);
            log.setAction(action);
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);
            log.setTimestamp(LocalDateTime.now());

            loginLogoutRepository.save(log);
            logger.debug("★★★ ログイン・ログアウト情報をDBに保存成功: {}", log);
        } catch (Exception e) {
            logger.error("ログイン・ログアウト情報の保存に失敗しました", e);
        }
    }
}