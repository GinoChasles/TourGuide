package tourGuide.dto;

import gpsUtil.location.Location;

public class NearbyAttraction {
    private String attractionName;
    private Location attractionLocation;
    private Location userLocation;
    private double distance;
    private int attractionRewardPoints;

    public NearbyAttraction(String attractionName, Location attractionLocation, Location userLocation, double distance, int attractionRewardPoints) {
        this.attractionName = attractionName;
        this.attractionLocation = attractionLocation;
        this.userLocation = userLocation;
        this.distance = distance;
        this.attractionRewardPoints = attractionRewardPoints;
    }

    public NearbyAttraction() {
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public Location getAttractionLocation() {
        return attractionLocation;
    }

    public void setAttractionLocation(Location attractionLocation) {
        this.attractionLocation = attractionLocation;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getAttractionRewardPoints() {
        return attractionRewardPoints;
    }

    public void setAttractionRewardPoints(int attractionRewardPoints) {
        this.attractionRewardPoints = attractionRewardPoints;
    }
}
