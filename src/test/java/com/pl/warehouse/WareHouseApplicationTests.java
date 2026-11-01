package com.pl.warehouse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * STUDY: Full application context smoke test.
 * Uses H2 via profile "test" — no real Postgres required.
 */
@SpringBootTest
@ActiveProfiles("test")
class WareHouseApplicationTests {

	@Test
	void contextLoads() {
		// If Spring fails to wire beans, this test fails.
	}
}
