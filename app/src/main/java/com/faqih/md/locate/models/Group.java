package com.faqih.md.locate.models;

import com.faqih.md.locate.init.Constants;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Faqih on 8/11/2016.
 */
public class Group {
    private String password;
    private String uid;
    private String groupName;
    private String invitationCode;

    public Group(String password, String uid, String groupName, String invitationCode) {
        this.password = password;
        this.uid = uid;
        this.groupName = groupName;
        this.invitationCode = invitationCode;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(Constants.password, this.password);
        result.put(Constants.uid, this.uid);
        result.put(Constants.groupName, this.groupName);
        result.put(Constants.invitationCode, this.invitationCode);
        return result;
    }
}
