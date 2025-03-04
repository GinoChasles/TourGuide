package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.dto.NearbyAttraction;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The type Tour guide service.
 */
@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	/**
	 * The Tracker.
	 */
	public final Tracker tracker;
	@Autowired
	private RewardCentral rewardCentral;

	/**
	 * The Test mode.
	 */
	boolean testMode = true;

	/**
	 * Instantiates a new Tour guide service.
	 *
	 * @param gpsUtil        the gps util
	 * @param rewardsService the rewards service
	 */
	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	/**
	 * Gets user rewards.
	 *
	 * @param user the user
	 * @return the user rewards
	 */
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	/**
	 * Gets user location.
	 *
	 * @param user the user
	 * @return the user location
	 */
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
		return visitedLocation;
	}

	/**
	 * Gets user.
	 *
	 * @param userName the user name
	 * @return the user
	 */
	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	/**
	 * Gets all users.
	 *
	 * @return the all users
	 */
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	/**
	 * Add user.
	 *
	 * @param user the user
	 */
	public void addUser(User user) {
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	/**
	 * Gets trip deals.
	 *
	 * @param user the user
	 * @return the trip deals
	 */
	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	/**
	 * Track user location visited location.
	 *
	 * @param user the user
	 * @return the visited location
	 */
	public VisitedLocation trackUserLocation(User user) {
//		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
//		user.addToVisitedLocations(visitedLocation);

		Locale.setDefault(Locale.US);
		final VisitedLocation[] visitedLocationReturn = new VisitedLocation[1];
		ExecutorService executorService = Executors.newFixedThreadPool(300);
		executorService.execute(new Runnable() {
			@Override
			public void run() {

				VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
				user.addToVisitedLocations(visitedLocation);

				rewardsService.calculateRewards(user);
				visitedLocationReturn[0] = visitedLocation;
			}
		});
		executorService.shutdown();
		return visitedLocationReturn[0];

	}


	/**
	 * Gets near by attractions.
	 *
	 * @param visitedLocation the visited location
	 * @return the near by attractions
	 */
	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> nearbyAttractions = new ArrayList<>();
		for (Attraction attraction : gpsUtil.getAttractions()) {
			if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}

		return nearbyAttractions;
	}


	/**
	 * Gets nearby attractions.
	 *
	 * @param visitedLocation the visited location
	 * @return the nearby attractions
	 */
	public List<NearbyAttraction> getNearbyAttractions(VisitedLocation visitedLocation) {
		List<NearbyAttraction> result = new ArrayList<>();
		List<Attraction> fiveClosestAttractions = getFiveNearestAttraction(visitedLocation);

		for (Attraction attraction : fiveClosestAttractions) {
			Location location = new Location(attraction.latitude, attraction.longitude);
			NearbyAttraction nearbyAttraction = new NearbyAttraction();
			nearbyAttraction.setAttractionName(attraction.attractionName);
			nearbyAttraction.setAttractionLocation(location);
			nearbyAttraction.setAttractionRewardPoints(rewardCentral.getAttractionRewardPoints(attraction.attractionId, visitedLocation.userId));
			nearbyAttraction.setDistance(rewardsService.getDistance(visitedLocation.location, attraction));
			nearbyAttraction.setUserLocation(visitedLocation.location);

			result.add(nearbyAttraction);
		}

		return result;
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	/**
	 * Gets all current locations.
	 *
	 * @return the all current locations
	 */
	public Map<String, Location> getAllCurrentLocations() {
		Map<String, Location> result = new HashMap<>();

		List<User> allUsers = getAllUsers();
		for(User user : allUsers) {
			result.put(user.getUserId().toString(), user.getLastVisitedLocation().location);
		}
		return result;
	}

	/**
	 * Get five nearest attraction list.
	 *
	 * @param visitedLocation the visited location
	 * @return the list
	 */
	public List<Attraction> getFiveNearestAttraction(VisitedLocation visitedLocation){


		List<Attraction> result = new ArrayList<>();
		List<Attraction> getNearByAttractions = getNearByAttractions(visitedLocation);
		List<Attraction> sortedList = getNearByAttractions.stream().sorted(Comparator.comparingDouble(o -> rewardsService.getDistance(visitedLocation.location, o))).collect(Collectors.toList());
		result = sortedList.subList(0,4);

		return result;
	}
	
	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);
			
			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}
	
	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	/**
	 * Check if user name exists boolean.
	 *
	 * @param username the username
	 * @return the boolean
	 */
	public boolean checkIfUserNameExists(String username) {
		return internalUserMap.containsKey(username) ? true : false;
	}
	
	private double generateRandomLongitude() {
		double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
	
}
