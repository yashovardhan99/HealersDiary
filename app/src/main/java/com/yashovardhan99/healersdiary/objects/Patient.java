package com.yashovardhan99.healersdiary.objects;

/**
 * Created by Yashovardhan99 on 23-05-2018 as a part of HealersDiary.
 * This is a class meant for creating patient objects. To be modified as needed.
 */
public class Patient {
    public String name,uid;
    public Patient() {
        uid = "";
        name = "";
    }
    //default constructor
    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }
}
