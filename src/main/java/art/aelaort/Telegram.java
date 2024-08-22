package art.aelaort;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;

import static art.aelaort.TelegramClientHelpers.execute;

@Component
@RequiredArgsConstructor
public class Telegram implements SpringAdminBot {
	@Getter
	@Value("${telegram.bot.token}")
	private String botToken;
	@Getter
	@Value("${telegram.admin.id}")
	private long adminId;

	private final QBitTorrentClient qBitTorrentClient;
	private final TelegramClient telegramClient;

	@AllArgsConstructor
	@Getter
	private enum Commands implements Command {
		START("/start"),
		GET_LIMIT("/get_limit"),
		SET_LIMIT_TO_1_MB("/set_limit_to_1_mb"),
		SET_LIMIT_TO_8_MB("/set_limit_to_8_mb"),
		DELETE_LIMIT("/delete_limit");
		final String command;
	}

	private final Map<String, Commands> commandsMap = Command.buildMap(Commands.class);

	@Override
	public void consumeAdmin(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();

			if (commandsMap.containsKey(messageText)) {
				handleCommand(messageText);
			} else {
				handleText(messageText);
			}
		}
	}

	private void handleText(String messageText) {
		send("команды:\n" + String.join("\n", commandsMap.keySet()));
	}

	private void handleCommand(String messageText) {
		handleCommand(commandsMap.get(messageText));
	}

	private void handleCommand(Commands command) {
		switch (command) {
			case START -> send("команды:\n" + String.join("\n", commandsMap.keySet()));
			case GET_LIMIT -> send(qBitTorrentClient.getUploadLimitString());
			case DELETE_LIMIT -> {
				qBitTorrentClient.deleteUploadLimit();
				send("ok");
				handleCommand(Commands.GET_LIMIT);
			}
			case SET_LIMIT_TO_1_MB -> {
				qBitTorrentClient.setUploadLimitInMB(1);
				send("limit set to 1 MB/sec");
				handleCommand(Commands.GET_LIMIT);
			}
			case SET_LIMIT_TO_8_MB -> {
				qBitTorrentClient.setUploadLimitInMB(8);
				send("limit set to 8 MB/sec");
				handleCommand(Commands.GET_LIMIT);
			}
		}
	}

	private void send(String text) {
		execute(SendMessage.builder()
				.chatId(adminId)
				.text(text)
				.build(), telegramClient);
	}
}
