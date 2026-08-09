package ktb.week04.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

import java.util.TimeZone;


@EnableResilientMethods
@SpringBootApplication
public class SpringbootApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(SpringbootApplication.class, args);
	}

}
