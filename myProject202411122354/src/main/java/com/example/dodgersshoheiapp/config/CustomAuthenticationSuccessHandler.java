package com.example.dodgersshoheiapp.config;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.dodgersshoheiapp.service.VisitorCounterService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final VisitorCounterService visitorCounterService;

    public CustomAuthenticationSuccessHandler(VisitorCounterService visitorCounterService) {
        this.visitorCounterService = visitorCounterService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            org.springframework.security.core.Authentication authentication)
            throws IOException, ServletException {
        // カウンターをインクリメント
        visitorCounterService.incrementVisitorCounter();
        // デフォルトの処理
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
