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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.Customizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final UserDetailsServiceImpl userDetailsService;
    private final VisitorCounterService visitorCounterService;
    private final LoginLogoutService loginLogoutService;

    // ★ `LoginLogoutService` をコンストラクタで受け取る
    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
            VisitorCounterService visitorCounterService,
            LoginLogoutService loginLogoutService) {
        this.userDetailsService = userDetailsService;
        this.visitorCounterService = visitorCounterService;
        this.loginLogoutService = loginLogoutService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF保護を無効化
                .cors(Customizer.withDefaults()) // CORS設定を有効化
                .headers(headers -> headers.frameOptions(Customizer.withDefaults())) // X-Frame-Optionsを無効化
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/auth/signup", "/auth/login", "/css/**", "/js/**", "/images/**",
                                "/comments", "/links", "/auth/userinfo", "/api/visitorCounter/**", "/api/proud/**",
                                "/proud", "/stats", "/api/stats", "/notifications/subscribe", "/notifications/send",
                                "/icon.png", "/sw.js", "/notifications/**", "/subscriptions/**",
                                "/notifications/comments", "/api/news", "/api/dodgers/standings", "/yosou")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // 管理者専用エンドポイントを保護
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .successHandler(visitorCounterSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(session -> session
                        .invalidSessionUrl("/auth/login")
                        .maximumSessions(1)
                        .expiredUrl("/auth/login"));

        return http.build();
    }

    /**
     * favicon.icoリクエストを無視する設定を追加
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/favicon.ico");
    }

    /**
     * ログアウト時の処理（ログ記録 & リダイレクト）
     */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            if (authentication != null) {
                String username = authentication.getName();
                String ipAddress = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");

                // 🔹 ログアウト情報を記録
                loginLogoutService.logAction(username, "LOGOUT", ipAddress, userAgent);

                logger.info("✅ ログアウト: ユーザー名={}, IP={}, ユーザーエージェント={}",
                        username, ipAddress, userAgent);
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
     * ログイン成功時の処理（カウンター更新 & ログ記録）
     */
    @Bean
    public AuthenticationSuccessHandler visitorCounterSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication != null) {
                String username = authentication.getName();
                String ipAddress = request.getRemoteAddr();
                String userAgent = request.getHeader("User-Agent");

                // 🔹 ログイン情報を記録
                loginLogoutService.logAction(username, "LOGIN", ipAddress, userAgent);

                logger.info("✅ ログイン成功: ユーザー名={}, IP={}, ユーザーエージェント={}",
                        username, ipAddress, userAgent);
            }

            visitorCounterService.incrementVisitorCounter();
            response.sendRedirect("/home");
        };
    }

    /**
     * ログイン失敗時の処理（ログ記録 & エラーメッセージ表示）
     */
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            String username = request.getParameter("username"); // 入力されたユーザー名
            String ipAddress = request.getRemoteAddr(); // クライアントのIPアドレス
            String userAgent = request.getHeader("User-Agent"); // ユーザーエージェント

            // 🔹 ログイン失敗の記録
            loginLogoutService.logAction(username != null ? username : "UNKNOWN", "FAILED_LOGIN", ipAddress, userAgent);

            logger.warn("⚠️ ログイン失敗: ユーザー名={}, IP={}, ユーザーエージェント={}, エラー={}",
                    username, ipAddress, userAgent, exception.getMessage());

            request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception.getMessage());
            response.sendRedirect("/auth/login?error=true");
        };
    }
}
