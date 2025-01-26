package com.example.dodgersshoheiapp;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // これが必要
@SpringBootApplication
@ComponentScan(basePackages = { "com.example.dodgersshoheiapp" })
public class DodgersShoheiAppApplication {
	public static void main(String[] args) {
		// Bouncy Castleプロバイダーを登録
		Security.addProvider(new BouncyCastleProvider());
		SpringApplication.run(DodgersShoheiAppApplication.class, args);
	}
}
