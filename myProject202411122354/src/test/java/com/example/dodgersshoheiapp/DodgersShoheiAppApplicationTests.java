package com.example.dodgersshoheiapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class DodgersShoheiAppApplicationTests {

	@Test
	void contextLoads() {
		// コンテキストのロードが成功することを確認
	}
}
