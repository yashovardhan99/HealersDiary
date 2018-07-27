package com.yashovardhan99.healersdiary.Objects;

import com.google.firebase.Timestamp;

/**
 * Created by Yashovardhan99 on 28-07-2018 as a part of HealersDiary.
 */
public class PatientFeedback {
    String feedback;
    Timestamp timestamp;
    Boolean verified;
    public PatientFeedback(){
        feedback="";
        timestamp=Timestamp.now();
        verified=false;
    }

    public String getFeedback() {
        return feedback;
    }

    public Boolean getVerified() {
        return verified;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
