package com.example.dodgersshoheiapp.controller;

import com.example.dodgersshoheiapp.service.LoginLogoutService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/batch-auth")
public class BatchAuthController {

    @Autowired
    private LoginLogoutService loginLogoutService;

    @PostMapping("/log-action")
    public ResponseEntity<String> logAction(
            @RequestParam String username,
            @RequestParam String action,
            HttpServletRequest request) {

        try {
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            loginLogoutService.logAction(username, action, ipAddress, userAgent);
            return ResponseEntity.ok("Log saved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving log");
        }
    }
}
