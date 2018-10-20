package com.yashovardhan99.healersdiary.objects;

/**
 * Created by Yashovardhan99 on 23-05-2018 as a part of HealersDiary.
 * This is a class meant for creating patient objects. To be modified as needed.
 */
public class Patient {
    public String name,uid,disease;
    public int healingsToday;
    public double rate,due;
    public Patient() {
        uid = "";
        name = "";
        disease = "";
        rate = 0.0;
        due = 0.0;
        healingsToday = 0;
    }
    //default constructor
    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public int getHealingsToday() {
        return healingsToday;
    }

    public String getDisease() {
        return disease;
    }

    public double getDue() {
        return due;
    }

    public double getRate() {
        return rate;
    }
}
