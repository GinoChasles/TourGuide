package tourGuide.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The type User preferences exception.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserPreferencesException extends RuntimeException{
    private final Logger logger = LoggerFactory.getLogger(UserPreferencesException.class);

    /**
     * Instantiates a new User preferences exception.
     */
    public UserPreferencesException() {
        super("UserPreferences is empty");
        logger.error("UserPreferences is empty");
    }
}