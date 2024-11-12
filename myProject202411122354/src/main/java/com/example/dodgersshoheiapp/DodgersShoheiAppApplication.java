package com.example.dodgersshoheiapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.example.dodgersshoheiapp" })
public class DodgersShoheiAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(DodgersShoheiAppApplication.class, args);
	}
}