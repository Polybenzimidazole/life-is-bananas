package be.lifeisbananas.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Wordt opgeworpen wanneer een opzoeking niets oplevert. Door de annotatie
 * antwoordt de toepassing met een 404 in plaats van een 500.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NietGevondenException extends RuntimeException {

	public NietGevondenException(String message) {
		super(message);
	}
}
