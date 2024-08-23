package art.aelaort;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class SchedulerLimitStore {
	private final AtomicLong currentUploadLimitInBytes = new AtomicLong();
	private final AtomicBoolean isWait = new AtomicBoolean(false);
	private final AtomicReference<LocalDateTime> whenUndoLimit = new AtomicReference<>();

	public long currentUploadLimitInBytes() {
		return currentUploadLimitInBytes.get();
	}

	public void currentUploadLimitInBytes(long currentUploadLimitInBytes) {
		this.currentUploadLimitInBytes.set(currentUploadLimitInBytes);
	}

	public void whenUndoLimit(LocalDateTime time) {
		whenUndoLimit.set(time);
	}

	public LocalDateTime whenUndoLimit() {
		return whenUndoLimit.get();
	}

	public void isWait(boolean isWait) {
		this.isWait.set(isWait);
	}

	public boolean isWait() {
		return isWait.get();
	}
}
