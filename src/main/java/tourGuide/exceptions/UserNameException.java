package tourGuide.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UserNameException extends RuntimeException{
    private final Logger logger = LoggerFactory.getLogger(UserNameException.class);

    public UserNameException(String username) {
        super("Username not found : " + username);
        logger.error("Username not found : " + username);
    }
}
