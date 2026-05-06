package com.example.dodgersshoheiapp.config;

import com.example.dodgersshoheiapp.service.LoginLogoutService;
import com.example.dodgersshoheiapp.service.VisitorCounterService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.config.Customizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // ← ★これを追加
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

                        // ✅ ★これを追加（最優先で通す）
                        .requestMatchers("/health").permitAll()

                        .requestMatchers("/api/wbc/tournament/**").permitAll()

                        // 🔓 認証不要（入口ページ・静的リソース）
                        .requestMatchers(
                                "/widgets/**", // ← ★これを追加
                                "/",
                                "/home",
                                "/fragments/**", // ← これ！！

                                // WBC（閲覧専用）
                                "/wbc/**",

                                // 入口コンテンツ
                                "/ohtani-vs-judge",
                                "/postseason",
                                "/WorldBaseballClassic",
                                "/archive",
                                "/links",
                                "/kike",
                                "/hogehoge_01",
                                "/hogehoge_02",
                                "/hogehoge_03",
                                "/hogehoge_04",
                                "/batting/filter",
                                "/api/pitchers",

                                // 認証関連
                                "/auth/login",
                                "/auth/signup",
                                "/signup-success",

                                // 静的リソース
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/icon.png",
                                "/sw.js",

                                // 参照系API（未ログインOK）
                                "/api/ohtani-vs-judge/stats",
                                "/api/mlb/**",
                                "/api/news",
                                "/api/dodgers/standings",
                                "/api/stats",
                                "/api/visitorCounter/**",
                                "/hogehoge_01",
                                "/hogehoge_02",
                                "/hogehoge_03",
                                "/hogehoge_04",

                                // 参加するならログイン必須
                                "/comments",
                                "/proud",
                                "/yosou")
                        .permitAll()

                        // 🔐 管理者専用
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 🔐 それ以外はログイン必須
                        .anyRequest().authenticated())

                // --- ログイン ---
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(loginSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                        .permitAll())

                // --- ログアウト ---
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))

                // --- セッション管理 ---
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/auth/login")
                        .maximumSessions(1)
                        .expiredUrl("/auth/login"));

        return http.build();
    }

    /**
     * favicon.ico は Spring Security 対象外
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/favicon.ico");
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
    public AuthenticationSuccessHandler loginSuccessHandler() {

        SavedRequestAwareAuthenticationSuccessHandler handler = new SavedRequestAwareAuthenticationSuccessHandler();

        // ✅ ログイン成功後は必ずここ
        handler.setDefaultTargetUrl("/home?login=success");
        handler.setAlwaysUseDefaultTargetUrl(true);

        return (request, response, authentication) -> {

            if (authentication != null) {
                loginLogoutService.logAction(
                        authentication.getName(),
                        "LOGIN",
                        request.getRemoteAddr(),
                        request.getHeader("User-Agent"));
            }

            visitorCounterService.incrementVisitorCounter();

            // ✅ Spring Security に完全委譲
            handler.onAuthenticationSuccess(request, response, authentication);
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
}
