package com.example.dodgersshoheiapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing // これが必要
@SpringBootApplication
public class DodgersShoheiAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(DodgersShoheiAppApplication.class, args);
	}
}
