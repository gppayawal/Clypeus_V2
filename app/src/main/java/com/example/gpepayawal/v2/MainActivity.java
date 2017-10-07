package com.example.gpepayawal.v2;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "pinguMessage";
    Button samp = null;
    TableLayout contacts_tbl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onPause();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onPause();
        Log.i(TAG, "onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onPause();
        Log.i(TAG, "onDestroy");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreState");
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveState");
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        Toast.makeText(this, "Pressed for a long time =) ", Toast.LENGTH_SHORT).show();
        //vibe.vibrate(50);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_VOLUME_UP:
                Toast.makeText(this,"Volume Up pressed", Toast.LENGTH_SHORT).show();
                event.startTracking();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Toast.makeText(this,"Volume Down pressed", Toast.LENGTH_SHORT).show();
                event.startTracking();
                return true;
            case KeyEvent.KEYCODE_HOME:
                Toast.makeText(this, "Home button pressed", Toast.LENGTH_SHORT).show();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void confirmContacts(View v){
        Toast.makeText(this, "Contacts Confirmed!", Toast.LENGTH_SHORT).show();
    }

    public void editContactsList(View v){

        do{
            ContactPerson person = new ContactPerson("pau", "09153325018");

        } while();


        TextView samp = new TextView(MainActivity.this);
        contacts_tbl = (TableLayout) findViewById(R.id.contacts_tbl);

        Resources res = getResources();
        String newContact = res.getString(R.string.newContact, person.name, person.number);

        Toast.makeText(this, person.name, Toast.LENGTH_SHORT).show();


        samp.setText(newContact);
        contacts_tbl.addView(samp);

    }

    public void editContact(View v){
        Toast.makeText(this, "edit mo ba to?", Toast.LENGTH_SHORT).show();
    }
}

