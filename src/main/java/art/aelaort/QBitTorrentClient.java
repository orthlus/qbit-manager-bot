package art.aelaort;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class QBitTorrentClient {
	private final RestTemplate restTemplate;

	public String getUploadLimitString() {
		long limitInMB = getUploadLimitInMB();
		return limitInMB == 0 ?
				"current - no limit" :
				"current limit %d MB/sec".formatted(limitInMB);
	}

	public long bytes2MB(long bytes) {
		return bytes / 1024 / 1024;
	}

	public long getUploadLimitInMB() {
		return bytes2MB(getUploadLimitInBytes());
	}

	public long getUploadLimitInBytes() {
		String bytesLimit = restTemplate.getForObject("/api/v2/transfer/uploadLimit", String.class);
		return Long.parseLong(bytesLimit);
	}

	public void deleteUploadLimit() {
		String url = "/api/v2/transfer/setUploadLimit?limit=0";
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new RuntimeException(response.getBody());
		}
	}

	public void setUploadLimitInMB(long bytes) {
		setUploadLimitInBytes(bytes * 1024 * 1024);
	}

	public void setUploadLimitInBytes(long bytes) {
		String url = "/api/v2/transfer/setUploadLimit?limit=" + bytes;
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new RuntimeException(response.getBody());
		}
	}
}
