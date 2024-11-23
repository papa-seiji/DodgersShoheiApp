package com.example.dodgersshoheiapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRFを無効化（必要に応じて変更）
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/signup", "/auth/login", "/css/**", "/js/**", "/images/**", "/comments",
                                "/links")
                        .permitAll() // 公開URL
                        .anyRequest().authenticated() // その他は認証が必要
                )
                .formLogin(form -> form
                        .loginPage("/auth/login") // カスタムログインページ
                        .defaultSuccessUrl("/home", true) // ログイン成功後の遷移先
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(logoutSuccessHandler()) // カスタムログアウト成功ハンドラーを設定
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());

        return http.build();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            // ログアウト成功後にリダイレクトしてログアウトメッセージを送信
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
                .userDetailsService(userDetailsService) // UserDetailsServiceをセットアップ
                .passwordEncoder(passwordEncoder()) // パスワードエンコーダを指定
                .and()
                .build();
    }
}
