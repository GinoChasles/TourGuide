package tourGuide.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.TripPricer;

@Service
public class UserService {
    private TripPricer tripPricer;

    @Autowired
    private TourGuideService tourGuideService;

    public User setUserPreferences(String username, UserPreferences userPreferences) {
        User user = tourGuideService.getUser(username);
        user.setUserPreferences(userPreferences);
        return user;
    }

}
