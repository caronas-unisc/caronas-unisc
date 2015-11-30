package br.unisc.caronasuniscegm.rest;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import br.unisc.caronasuniscegm.R;

public class User {

    private int id;
    private String name;
    private String email;
    private String rideIntention;

    public User(JSONObject userJsonObject) {
        try
        {
            id = userJsonObject.getInt("id");
            name = userJsonObject.getString("name");
            email = userJsonObject.getString("email");
            rideIntention = userJsonObject.getString("ride_intention");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRideIntention() {
        return rideIntention;
    }

    public void setRideIntention(String rideIntention) {
        this.rideIntention = rideIntention;
    }

    public static User getCurrent(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        JSONObject userJson = null;
        String jsonText = sharedPref.getString(context.getString(R.string.preference_user_object),
                null);

        if (jsonText != null) {
            try {
                userJson = new JSONObject(jsonText);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return userJson != null ? new User(userJson) : null;
    }

}
