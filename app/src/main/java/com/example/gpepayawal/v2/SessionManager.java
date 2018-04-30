package com.example.gpepayawal.v2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by gpepayawal on 2/23/18.
 */

public class SessionManager {
    SharedPreferences pref;
    static SharedPreferences.Editor editor;
    Context context;

    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "myPref";
    public static final String IS_LOGGEDIN = "isLoggedIn";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PIN = "pin";
    public static final String KEY_CONTACTS = "contacts";
    public static Set<String> emergencyContacts;
    private static boolean confirmedContacts = false;

    public SessionManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String username, String pin){
        editor.putBoolean(IS_LOGGEDIN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PIN, pin);
        editor.apply(); //editor.commit();
    }

    public HashMap<String, String> getUserLoginDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_USERNAME, pref.getString(KEY_USERNAME, null));
        user.put(KEY_PIN, pref.getString(KEY_PIN, null));
        return user;
    }

    public static void confirmedEmergencyContacts(Set<String> list){
        Set<String> set = new HashSet<>(list);
        editor.putStringSet(KEY_CONTACTS, set);
        editor.apply();
    }

    public Set<String> getSelectedContacts(){
        emergencyContacts = pref.getStringSet(KEY_CONTACTS, emergencyContacts);
        if(emergencyContacts == null) return null;
        return pref.getStringSet(KEY_CONTACTS, emergencyContacts);
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGGEDIN, false);
    }

    public void checkLogin() {
        if (!this.isLoggedIn()) {  //if not logged in
            Intent i = new Intent(context, Login.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

}
