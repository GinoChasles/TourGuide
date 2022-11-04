package tourGuide;

import com.jsoniter.output.JsonStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tourGuide.exceptions.UserNameException;
import tourGuide.exceptions.UserPreferencesException;
import tourGuide.service.UserService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;

@RestController
public class UserController {
    @Autowired
    private UserService userService;


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
