package mx.unam.fciencias.cinema_ciencias.audit_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"eureka.client.enabled=false"
})
class AuditServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
