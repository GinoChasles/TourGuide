package tourGuide;

import com.jsoniter.output.JsonStream;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tourGuide.exceptions.UserNameException;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

import java.util.List;

/**
 * The type Tour guide controller.
 */
@RestController
public class TourGuideController {

    /**
     * The Tour guide service.
     */
    @Autowired
    TourGuideService tourGuideService;

    /**
     * Index string.
     *
     * @return the string
     */
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * Gets location.
     *
     * @param userName the user name
     * @return the location
     * @throws UserNameException the user name exception
     */
    @RequestMapping("/getLocation")
    public String getLocation(@RequestParam String userName) throws UserNameException {
        if (!tourGuideService.checkIfUserNameExists(userName)) {
            throw new UserNameException(userName);
        }
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(visitedLocation.location);
    }

    /**
     * Gets nearby attractions.
     *
     * @param userName the user name
     * @return the nearby attractions
     * @throws UserNameException the user name exception
     */
//  TODO: Change this method to no longer return a List of Attractions.
    //  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
    //  Return a new JSON object that contains:
    // Name of Tourist attraction,
    // Tourist attractions lat/long,
    // The user's location lat/long,
    // The distance in miles between the user's location and each of the attractions.
    // The reward points for visiting each Attraction.
    //    Note: Attraction reward points can be gathered from RewardsCentral
    @RequestMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam String userName) throws UserNameException {
        if (!tourGuideService.checkIfUserNameExists(userName)) {
            throw new UserNameException(userName);
        }
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
//    	return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
        return JsonStream.serialize(tourGuideService.getNearbyAttractions(visitedLocation));
    }

    /**
     * Gets rewards.
     *
     * @param userName the user name
     * @return the rewards
     * @throws UserNameException the user name exception
     */
    @RequestMapping("/getRewards")
    public String getRewards(@RequestParam String userName) throws UserNameException {
        if (!tourGuideService.checkIfUserNameExists(userName)) {
            throw new UserNameException(userName);
        }
        return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }

    /**
     * Gets all current locations.
     *
     * @return the all current locations
     */
    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
        // TODO: Get a list of every user's most recent location as JSON
        //- Note: does not use gpsUtil to query for their current location,
        //        but rather gathers the user's current location from their stored location history.
        //
        // Return object should be the just a JSON mapping of userId to Locations similar to:
        //     {
    	//        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371} 
    	//        ...
    	//     }
    	
    	return JsonStream.serialize(tourGuideService.getAllCurrentLocations());
    }

    /**
     * Gets trip deals.
     *
     * @param userName the user name
     * @return the trip deals
     * @throws UserNameException the user name exception
     */
    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) throws UserNameException {
        if (!tourGuideService.checkIfUserNameExists(userName)) {
            throw new UserNameException(userName);
        }
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
        return JsonStream.serialize(providers);
    }
    
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
   

}