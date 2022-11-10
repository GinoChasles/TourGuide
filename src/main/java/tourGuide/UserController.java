package tourGuide;

import com.jsoniter.output.JsonStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tourGuide.exceptions.UserNameException;
import tourGuide.exceptions.UserPreferencesException;
import tourGuide.service.UserService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;

/**
 * The type User controller.
 */
@RestController
public class UserController {
    @Autowired
    private UserService userService;


    /**
     * Sets user preferences.
     *
     * @param username        the username
     * @param userPreferences the user preferences
     * @return the user preferences
     * @throws UserNameException        the user name exception
     * @throws UserPreferencesException the user preferences exception
     */
    @PutMapping("/setUserPreferences")
    public String setUserPreferences(@RequestParam String username, @RequestBody UserPreferences userPreferences) throws UserNameException, UserPreferencesException {
        if(userPreferences == null) {
            throw new UserPreferencesException();
        }
        if(!userService.getUserNameExist(username)) {
            throw new UserNameException(username);
        }

        return JsonStream.serialize(userService.setUserPreferences(username, userPreferences));
    }
}
