package com.acxiom.crm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies that the Spring application context starts with every bean wired.
 * <p>
 * Runs against the test profile so that it uses the in-memory database rather than
 * requiring a live MySQL server, which keeps the build runnable anywhere. The
 * MySQL-backed startup is verified separately by running the application itself.
 */
@SpringBootTest
@ActiveProfiles("test")
class AcxiomcrmApplicationTests {

	@Test
	void contextLoads() {
	}

}
