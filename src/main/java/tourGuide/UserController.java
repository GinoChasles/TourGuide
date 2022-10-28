package tourGuide;

import com.jsoniter.output.JsonStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tourGuide.service.UserService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;

@RestController
public class UserController {
    @Autowired
    private UserService userService;


    @PutMapping("/setUserPreferences")
    public String setUserPreferences(@RequestParam String username, @RequestBody UserPreferences userPreferences) {
        return JsonStream.serialize(userService.setUserPreferences(username, userPreferences));
    }
}
