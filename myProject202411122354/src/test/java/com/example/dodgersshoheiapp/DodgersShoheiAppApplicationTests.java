package com.example.dodgersshoheiapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import com.example.dodgersshoheiapp.repository.CommentRepository;
import com.example.dodgersshoheiapp.repository.SubscriptionRepository;
import com.example.dodgersshoheiapp.service.PushNotificationService;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class DodgersShoheiAppApplicationTests {

	@MockBean
	private PushNotificationService pushNotificationService;

	@MockBean
	private SubscriptionRepository subscriptionRepository;

	@MockBean
	private CommentRepository commentRepository;

	@Test
	void contextLoads() {
		// ApplicationContext のロードをテスト
		System.out.println("ApplicationContext loaded successfully.");
	}
}
