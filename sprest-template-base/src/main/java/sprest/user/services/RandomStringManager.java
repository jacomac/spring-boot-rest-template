package sprest.user.services;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class RandomStringManager {

	public String generateRandomAlphanumericString(int targetStringLength) {
		int leftLimit = 48; // numeral '0'
		int rightLimit = 122; // letter 'z'
		return generateRandomAlphanumericString(targetStringLength, leftLimit, rightLimit);
	}

	public String generateRandomAlphanumericString(int targetStringLength, int leftLimit, int rightLimit) {
		return random.ints(leftLimit, rightLimit + 1)
			.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
			.limit(targetStringLength)
			.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
			.toString();
	}

	private final SecureRandom random = new SecureRandom();

}
