package com.example.dodgersshoheiapp.config;

import com.example.dodgersshoheiapp.service.LoginLogoutService;
import com.example.dodgersshoheiapp.service.VisitorCounterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.config.Customizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final UserDetailsServiceImpl userDetailsService;
    private final VisitorCounterService visitorCounterService;
    private final LoginLogoutService loginLogoutService;

    public SecurityConfig(
            UserDetailsServiceImpl userDetailsService,
            VisitorCounterService visitorCounterService,
            LoginLogoutService loginLogoutService) {
        this.userDetailsService = userDetailsService;
        this.visitorCounterService = visitorCounterService;
        this.loginLogoutService = loginLogoutService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // --- 基本設定 ---
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions(Customizer.withDefaults()))

                // --- 認可設定 ---
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", // トップ
                                "/home", // Home画面
                                "/auth/login",
                                "/auth/signup",
                                "/signup-success",

                                // 静的リソース
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/icon.png",
                                "/sw.js",

                                // 公開ページ
                                "/comments",
                                "/links",
                                "/proud",
                                "/archive",
                                "/yosou",
                                "/kike",
                                "/postseason",
                                "/ohtani-vs-judge",
                                "/WorldBaseballClassic",

                                // API（公開）
                                "/auth/userinfo",
                                "/api/visitorCounter/**",
                                "/api/proud/**",
                                "/api/stats",
                                "/api/news",
                                "/api/dodgers/standings",
                                "/api/mlb/**",
                                "/api/ohtani-vs-judge/stats",

                                // 通知・Push
                                "/notifications/**",
                                "/notifications/subscribe",
                                "/notifications/send",
                                "/notifications/comments",
                                "/subscriptions/**")
                        .permitAll()

                        // 管理者専用
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // その他は認証必須
                        .anyRequest().authenticated())

                // --- ログイン ---
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .successHandler(visitorCounterSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                        .permitAll())

                // --- ログアウト ---
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())

                // --- セッション管理 ---
                .sessionManagement(session -> session
                        .invalidSessionUrl("/auth/login")
                        .maximumSessions(1)
                        .expiredUrl("/auth/login"));

        return http.build();
    }

    /**
     * favicon.ico は Spring Security の対象外
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/favicon.ico");
    }

    /**
     * ログアウト成功時
     */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            if (authentication != null) {
                loginLogoutService.logAction(
                        authentication.getName(),
                        "LOGOUT",
                        request.getRemoteAddr(),
                        request.getHeader("User-Agent"));
            }
            response.sendRedirect("/auth/login?logout=true");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }

    /**
     * ログイン成功時
     */
    @Bean
    public AuthenticationSuccessHandler visitorCounterSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication != null) {
                loginLogoutService.logAction(
                        authentication.getName(),
                        "LOGIN",
                        request.getRemoteAddr(),
                        request.getHeader("User-Agent"));
            }
            visitorCounterService.incrementVisitorCounter();
            response.sendRedirect("/home");
        };
    }

    /**
     * ログイン失敗時
     */
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            loginLogoutService.logAction(
                    request.getParameter("username") != null ? request.getParameter("username") : "UNKNOWN",
                    "FAILED_LOGIN",
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"));
            logger.warn("⚠️ ログイン失敗: {}", exception.getMessage());
            response.sendRedirect("/auth/login?error=true");
        };
    }
}
