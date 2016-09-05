package com.faqih.md.locate.models;

import com.faqih.md.locate.init.Constants;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Faqih on 8/18/2016.
 */
public class User {
    private String password;
    private String status;
    private String uid;
    private String userName;
    private String userLastUpdates;
    private List<Double> userLocation;

    public User(String password, String status, String uid, String userName, String userLastUpdates, List<Double> userLocation) {
        this.password = password;
        this.status = status;
        this.uid = uid;
        this.userName = userName;
        this.userLastUpdates = userLastUpdates;
        this.userLocation = userLocation;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(Constants.password, this.password);
        result.put(Constants.status, this.status);
//        result.put(Constants.uid, this.uid);
//        result.put(Constants.userName, this.userName);
//        result.put(Constants.userLastUpdates, this.userLastUpdates);
//        result.put(Constants.userLocation, this.userLocation);
        return result;
    }
}
