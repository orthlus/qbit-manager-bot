package art.aelaort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

import static art.aelaort.TelegramBots.createTelegramInit;

@Configuration
public class Config {
	@Bean
	public RestTemplate qbit(RestTemplateBuilder restTemplateBuilder,
							 @Value("${qbit.url}") String url) {
		return restTemplateBuilder
				.rootUri(url)
				.build();
	}

	@Bean
	public TelegramClient telegramClient(@Value("${telegram.bot.token}") String token) {
		return TelegramClientBuilder.builder()
				.token(token)
				.build();
	}

	@Bean
	public TelegramInit telegramInit(List<SpringLongPollingBot> bots) {
		return createTelegramInit(bots);
	}
}
