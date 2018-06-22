package com.yashovardhan99.healersdiary.Objects;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Yashovardhan99 on 23-05-2018 as a part of HealersDiary.
 * This is a class meant for creating patient objects.
 */
public class Patient {
    String name,disease,phone,address,uid;
    int rate, healings, unpaidHealings, amountDue;
    Date startDate, lastPaid;
    ArrayList<String> remarks;
    ArrayList<Date> healingDates;
    Patient(){
        uid="0";
        name="";
        disease="";
        phone="";
        address="";
        rate=0;
        healings=0;
        unpaidHealings=0;
        amountDue=0;
        startDate=new Date();
        lastPaid=new Date();
        remarks = new ArrayList<>(healings);
        healingDates = new ArrayList<>(healings);
    }
    Patient(String nm, int r){
        this();
        name = nm;
        rate = r;
    }
    int getAmountDue(){
        amountDue = rate*unpaidHealings;
        return amountDue;
    }
}
