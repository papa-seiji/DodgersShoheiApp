package com.example.dodgersshoheiapp.config;

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
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    // ユーザー認証に必要なサービス
    private final UserDetailsServiceImpl userDetailsService;

    // 訪問者カウンターのサービス
    private final VisitorCounterService visitorCounterService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, VisitorCounterService visitorCounterService) {
        this.userDetailsService = userDetailsService;
        this.visitorCounterService = visitorCounterService;
    }

    /**
     * アプリケーション全体のセキュリティ設定を行うメインのフィルタチェーン
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF保護を無効化
                .authorizeHttpRequests(auth -> auth
                        // 特定のエンドポイントは認証なしでアクセス可能に設定
                        .requestMatchers("/", "/auth/signup", "/auth/login", "/css/**", "/js/**", "/images/**",
                                "/comments",
                                "/links", "/auth/userinfo", "/api/visitorCounter/**", "/api/proud/**", "/proud",
                                "/stats", "/api/stats", // 既存の許可
                                "/notifications/subscribe", "/notifications/send", "/icon.png", "/sw.js",
                                "/notifications/**", "/subscriptions/**", "/notifications/comments") // 追加: このエンドポイントを許可
                        .permitAll()
                        // それ以外のリクエストは認証が必要
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/auth/login") // カスタムログインページ
                        .successHandler(visitorCounterSuccessHandler()) // ログイン成功時のハンドラー
                        .failureHandler(authenticationFailureHandler()) // ログイン失敗時のハンドラー
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") // カスタムログアウトURL
                        .logoutSuccessHandler(logoutSuccessHandler()) // ログアウト成功時のハンドラー
                        .invalidateHttpSession(true) // セッションを無効化
                        .deleteCookies("JSESSIONID") // クッキー削除
                        .permitAll())
                .sessionManagement(session -> session
                        .invalidSessionUrl("/auth/login") // セッションが無効になった場合のリダイレクト先
                        .maximumSessions(1) // ユーザーごとの最大セッション数を1に設定
                                            // 1セッション制:
                                            // 1 人のユーザーが複数のデバイスやブラウザで同時ログインすることを禁止します。
                                            // 存在のセッションがある場合、新しいログインが行われる場合は古いセッションが有効化されます。
                        .expiredUrl("/auth/login")); // セッションが期限切れになった場合のリダイレクト先

        return http.build();
    }

    /**
     * ログアウト成功時の処理
     * 例：ログイン画面にリダイレクトしてメッセージを表示
     */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            System.out.println(
                    "DEBUG: ログアウトしたユーザー: " + (authentication != null ? authentication.getName() : "不明"));
            response.sendRedirect("/auth/login?logout=true"); // ログアウト後にログイン画面へリダイレクト
        };
    }

    /**
     * パスワードのエンコーダー（暗号化と検証に使用）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCryptを使用
    }

    /**
     * 認証マネージャーの設定
     * 例：カスタムUserDetailsServiceとパスワードエンコーダーを使用
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService) // カスタムUserDetailsServiceを使用
                .passwordEncoder(passwordEncoder()) // パスワードエンコーダーを使用
                .and()
                .build();
    }

    /**
     * ログイン成功時の処理
     * 例：訪問者カウンターを増加させ、ホーム画面へリダイレクト
     */
    @Bean
    public AuthenticationSuccessHandler visitorCounterSuccessHandler() {
        return (request, response, authentication) -> {
            visitorCounterService.incrementVisitorCounter(); // 訪問者カウントを増加
            response.sendRedirect("/home"); // ホーム画面へリダイレクト
        };
    }

    /**
     * ログイン失敗時の処理
     * 例：エラーメッセージを設定し、再度ログイン画面へリダイレクト
     */
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            System.out.println("DEBUG: ログイン失敗 - " + exception.getMessage());
            request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception.getMessage()); // エラーメッセージをセッションに設定
            response.sendRedirect("/auth/login?error=true"); // ログイン画面にエラーを付けてリダイレクト
        };
    }
}
