package br.unisc.caronasuniscegm.rest;

import java.util.Date;

public class RideIntention {

    public static final String GIVE_AND_RECEIVE_RIDE = "give_and_receive_ride";
    public static final String GIVE_RIDE = "give_ride";
    public static final String RECEIVE_RIDE = "receive_ride";
    public static final String AVAIBILITY_TYPE_RECEIVE = "receive";
    public static final String AVAIBILITY_TYPE_GIVE = "give";

    private Date date;
    private String period;
    private String availabilityType;
    private String startingLocationAddress;
    private Double startingLocationLatitude;
    private Double startingLocationLongitude;
    private int availablePlacesInCar;

    public static String getValue(boolean giveRide, boolean receiveRide) {
        if (giveRide && receiveRide)
            return GIVE_AND_RECEIVE_RIDE;

        return giveRide ? GIVE_RIDE : RECEIVE_RIDE;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getAvailabilityType() {
        return availabilityType;
    }

    public void setAvailabilityType(String availabilityType) {
        this.availabilityType = availabilityType;
    }

    public String getStartingLocationAddress() {
        return startingLocationAddress;
    }

    public void setStartingLocationAddress(String startingLocationAddress) {
        this.startingLocationAddress = startingLocationAddress;
    }

    public Double getStartingLocationLatitude() {
        return startingLocationLatitude;
    }

    public void setStartingLocationLatitude(Double startingLocationLatitude) {
        this.startingLocationLatitude = startingLocationLatitude;
    }

    public Double getStartingLocationLongitude() {
        return startingLocationLongitude;
    }

    public void setStartingLocationLongitude(Double startingLocationLongitude) {
        this.startingLocationLongitude = startingLocationLongitude;
    }

    public int getAvailablePlacesInCar() {
        return availablePlacesInCar;
    }

    public void setAvailablePlacesInCar(int availablePlacesInCar) {
        this.availablePlacesInCar = availablePlacesInCar;
    }
}
