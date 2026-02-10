package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=test-secret-key-for-testing-purposes-only"
})
class BlogrApplicationTests {

	@Test
	void contextLoads() {
	}

}
