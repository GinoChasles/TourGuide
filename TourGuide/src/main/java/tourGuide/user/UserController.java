package tourGuide.user;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private UserService userService;
    UserController(UserService usrService) {
        userService = usrService;
    }
}
