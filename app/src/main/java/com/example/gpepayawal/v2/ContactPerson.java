package com.example.gpepayawal.v2;

import android.content.res.Resources;
import android.util.Log;

/**
 * Created by gpepayawal on 9/30/17.
 */

public class ContactPerson {
    private static final String TAG = "pinguMessage";
    protected String name, number;

    public ContactPerson(String name, String number){
        this.name = name;
        this.number = number;
    }

    public void test(){
        Log.d(TAG, this.name);
    }

}
