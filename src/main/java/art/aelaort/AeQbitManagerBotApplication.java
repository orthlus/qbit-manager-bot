package art.aelaort;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AeQbitManagerBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(AeQbitManagerBotApplication.class, args);
	}

}
