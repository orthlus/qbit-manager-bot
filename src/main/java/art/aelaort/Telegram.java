package art.aelaort;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static art.aelaort.TelegramClientHelpers.execute;

@Component
@RequiredArgsConstructor
public class Telegram implements SpringAdminBot {
	private final SchedulerLimitStore schedulerLimitStore;
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
		SET_LIMIT_TO_1_MB_FOR_HOUR("/set_limit_to_1_mb_for_hour"),
		SET_LIMIT_TO_8_MB("/set_limit_to_8_mb"),
		DELETE_LIMIT("/delete_limit"),
		DELETE_LIMIT_FOR_HOUR("/delete_limit_for_hour"),
		NIGHT_SCHEDULE_DISABLE("/night_schedule_disable"),
		NIGHT_SCHEDULE_ENABLE("/night_schedule_enable");
		final String command;
	}

	private final Map<String, Commands> commandsMap = Command.buildMap(Commands.class);

	@PostConstruct
	private void init() {
		send("bot started, night schedule: " + schedulerLimitStore.isNightScheduled());
	}

	@Override
	public void consumeAdmin(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageText = update.getMessage().getText();

			if (commandsMap.containsKey(messageText)) {
				handleCommand(messageText);
				if (commandsMap.get(messageText) != Commands.START) {
					handleCommand(Commands.START);
				}
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
				send("limit deleted");
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
			case SET_LIMIT_TO_1_MB_FOR_HOUR -> {
				saveCurrentLimit();
				handleCommand(Commands.SET_LIMIT_TO_1_MB);
			}
			case DELETE_LIMIT_FOR_HOUR -> {
				saveCurrentLimit();
				handleCommand(Commands.DELETE_LIMIT);
			}
			case NIGHT_SCHEDULE_DISABLE -> {
				schedulerLimitStore.isNightScheduled(false);
				send("night schedule disabled");
			}
			case NIGHT_SCHEDULE_ENABLE -> {
				schedulerLimitStore.isNightScheduled(true);
				send("night schedule enabled");
			}
		}
	}

	private void saveCurrentLimit() {
		schedulerLimitStore.currentUploadLimitInBytes(qBitTorrentClient.getUploadLimitInBytes());
		schedulerLimitStore.isWait(true);
		schedulerLimitStore.whenUndoLimit(LocalDateTime.now().plusHours(1));
	}

	public void send(String text) {
		execute(SendMessage.builder()
				.chatId(adminId)
				.text(appendText(text))
				.build(), telegramClient);
	}

	private String appendText(String text) {
		return text;
//		return text + "\n/start";
	}
}
