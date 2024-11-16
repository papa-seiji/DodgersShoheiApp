package com.example.dodgersshoheiapp.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 主キー

    @Column(nullable = false, unique = true)
    private String username; // ユーザー名

    @Column(nullable = false)
    private String password; // パスワード

    @Column(nullable = true)
    private String role = "USER"; // ロール (デフォルト: USER)

    @Column(name = "created_at", nullable = true, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt; // 作成日時

    @Column(nullable = true)
    private String icon; // アイコンのパス

    // コンストラクタ
    public User() {
    }

    // Getter and Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    // UserDetailsインターフェースの実装
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 権限リスト（今回は空）
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // アカウントが有効期限切れではない
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // アカウントがロックされていない
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 資格情報が有効期限切れではない
    }

    @Override
    public boolean isEnabled() {
        return true; // ユーザーが有効
    }
}
