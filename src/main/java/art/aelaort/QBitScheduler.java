package art.aelaort;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class QBitScheduler {
	private final QBitTorrentClient qBitTorrentClient;
	private final SchedulerLimitStore schedulerLimitStore;
	private final Telegram telegram;

	@Scheduled(fixedRate = 20, timeUnit = TimeUnit.MINUTES)
	public void checkToUndoLimit() {
		if (schedulerLimitStore.isWait()) {
			if (schedulerLimitStore.whenUndoLimit().isBefore(LocalDateTime.now())) {
				setSavedLimit();
				schedulerLimitStore.isWait(false);
			}
		}
	}

	public void setSavedLimit() {
		long bytes = schedulerLimitStore.currentUploadLimitInBytes();
		qBitTorrentClient.setUploadLimitInBytes(bytes);
		telegram.send("scheduled: set limit to %d mb".formatted(qBitTorrentClient.bytes2MB(bytes)));
	}
}
