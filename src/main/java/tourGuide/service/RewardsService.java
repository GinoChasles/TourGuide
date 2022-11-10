package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.List;
import java.util.concurrent.*;

/**
 * The type Rewards service.
 */
@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;


	/**
	 * Instantiates a new Rewards service.
	 *
	 * @param gpsUtil       the gps util
	 * @param rewardCentral the reward central
	 */
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}

	/**
	 * Sets proximity buffer.
	 *
	 * @param proximityBuffer the proximity buffer
	 */
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	/**
	 * Sets default proximity buffer.
	 */
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	/**
	 * Calculate rewards.
	 *
	 * @param user the user
	 */
	public void calculateRewards(User user) {
		ExecutorService executorService = Executors.newFixedThreadPool(300);
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				List<VisitedLocation> userLocations =
						new CopyOnWriteArrayList<>(user.getVisitedLocations());
				List<Attraction> attractions = new CopyOnWriteArrayList<>(gpsUtil.getAttractions());


				for (VisitedLocation visitedLocation : userLocations) {
					for (Attraction attraction : attractions) {
						if (user.getUserRewards().stream()
								.filter(r -> r.attraction.attractionName.equals(
										attraction.attractionName)).count() == 0) {
							if (nearAttraction(visitedLocation, attraction)) {
								user.addUserReward(new UserReward(visitedLocation, attraction,
										getRewardPoints(attraction, user)));
							}
						}
					}
				}

			}
		});

		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}

	}

	/**
	 * Is within attraction proximity boolean.
	 *
	 * @param attraction the attraction
	 * @param location   the location
	 * @return the boolean
	 */
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	/**
	 * Gets distance.
	 *
	 * @param loc1 the loc 1
	 * @param loc2 the loc 2
	 * @return the distance
	 */
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}
}
