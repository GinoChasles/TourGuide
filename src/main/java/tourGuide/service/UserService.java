package tourGuide.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.TripPricer;

/**
 * The type User service.
 */
@Service
public class UserService {
    private TripPricer tripPricer;

    @Autowired
    private TourGuideService tourGuideService;

    /**
     * Sets user preferences.
     *
     * @param username        the username
     * @param userPreferences the user preferences
     * @return the user preferences
     */
    public User setUserPreferences(String username, UserPreferences userPreferences) {
        User user = tourGuideService.getUser(username);
        user.setUserPreferences(userPreferences);
        return user;
    }

    /**
     * Gets user name exist.
     *
     * @param username the username
     * @return the user name exist
     */
    public boolean getUserNameExist(String username) {
        return tourGuideService.checkIfUserNameExists(username);
    }

}
