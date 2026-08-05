package ktb.week04.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"jwt.secret=test-secret-key-must-be-at-least-32-bytes-long"
})
class SpringbootApplicationTests {

	@Test
	void contextLoads() {
	}

}
