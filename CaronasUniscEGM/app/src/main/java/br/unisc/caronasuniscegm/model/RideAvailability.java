package br.unisc.caronasuniscegm.model;

import java.util.Date;

/**
 * Created by henrique on 11/6/15.
 */
public class RideAvailability {
    private Integer availabilityId;
    private String nmUserRequest;
    private String nmUserResponse;
    private Integer remainingPlacesInCar;

    private Date date;
    private String period;
    private String type; //give or receive


    private Ride ride;
    // 0 - Carona não requisitada
    // 1 - Carona pendente
    // 2 - Carona confirmada


    public Integer getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(Integer availabilityId) {
        this.availabilityId = availabilityId;
    }

    public String getNmUserRequest() {
        return nmUserRequest;
    }

    public void setNmUserRequest(String nmUserRequest) {
        this.nmUserRequest = nmUserRequest;
    }

    public String getNmUserResponse() {
        return nmUserResponse;
    }

    public void setNmUserResponse(String nmUserResponse) {
        this.nmUserResponse = nmUserResponse;
    }

    public Integer getRemainingPlacesInCar() {
        return remainingPlacesInCar;
    }

    public void setRemainingPlacesInCar(Integer remainingPlacesInCar) {
        this.remainingPlacesInCar = remainingPlacesInCar;
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

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName(){
        if (type.equals("give")){
            return nmUserRequest;
        } else {
            return nmUserResponse;
        }
    }


}
