package com.example.gpepayawal.v2;

import android.content.res.Resources;
import android.util.Log;

/**
 * Created by gpepayawal on 9/30/17.
 */

public class ContactPerson extends Exception {
    private static final String TAG = "pinguMessage";
    String name, number;
    int count = 0;

    public ContactPerson(String name, String number){
        this.name = name;
        this.number = number;

        count++;
    }

    public String getContactPerson(ContactPerson p){
        return p.name + p.number;
    }

}
