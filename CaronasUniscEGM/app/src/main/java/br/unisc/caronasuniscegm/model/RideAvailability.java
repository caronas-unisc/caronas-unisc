package br.unisc.caronasuniscegm.model;

import java.util.Date;

/**
 * Created by henrique on 11/6/15.
 */
public class RideAvailability {
    private String nmUserRequest;
    private String nmUserResponse;

    private Date date;
    private Integer period;

    private Integer status;
    // 0 - Carona não requisitada
    // 1 - Carona pendente
    // 2 - Carona confirmada

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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
