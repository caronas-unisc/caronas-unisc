package br.unisc.caronasuniscegm.rest;

public class RideIntention {

    public static final String GIVE_AND_RECEIVE_RIDE = "give_and_receive_ride";
    public static final String GIVE_RIDE = "give_ride";
    public static final String RECEIVE_RIDE = "receive_ride";

    private Double latitude;
    private Double longitude;


    public static String getValue(boolean giveRide, boolean receiveRide) {
        if (giveRide && receiveRide)
            return GIVE_AND_RECEIVE_RIDE;

        return giveRide ? GIVE_RIDE : RECEIVE_RIDE;
    }

}
