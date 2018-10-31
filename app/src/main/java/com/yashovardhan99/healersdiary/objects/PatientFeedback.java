package com.yashovardhan99.healersdiary.objects;

import com.google.firebase.Timestamp;

import java.text.DateFormat;

/**
 * Created by Yashovardhan99 on 28-07-2018 as a part of HealersDiary.
 */
public class PatientFeedback {
    public String feedback, Uid;
    public Timestamp timestamp;
    public Boolean verified;
    public PatientFeedback(){
        feedback="";
        Uid = "";
        timestamp=Timestamp.now();
        verified=false;
    }

    public String getFeedback() {
        return feedback;
    }

    public Boolean getVerified() {
        return verified;
    }

    public String getTimestamp() {
        return DateFormat.getDateInstance().format(timestamp.toDate());
    }

    public String getUid() {
        return Uid;
    }
}
