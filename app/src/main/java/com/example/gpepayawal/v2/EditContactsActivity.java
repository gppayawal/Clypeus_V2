package com.example.gpepayawal.v2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EditContactsActivity extends AppCompatActivity {
    TableLayout contactsTable;
    Map<String, ContactPerson> directory;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contacts);

        Intent in = getIntent();
        Toast.makeText(EditContactsActivity.this, "HI", Toast.LENGTH_SHORT).show();

       pref = getApplication().getSharedPreferences("myEC", 0); // 0 - for private mode
        directory = (Map<String, ContactPerson>) pref.getAll();

        contactsTable = (TableLayout) findViewById(R.id.contacts_tbl);
        int size = directory.size();
        Toast.makeText(this, "size " + size, Toast.LENGTH_SHORT).show();

       Set<Map.Entry<String, ContactPerson>> set = directory.entrySet();
        for(Map.Entry<String, ContactPerson> entry : set){
            /*String key = entry.getKey();
            ContactPerson p = entry.getValue();

            Toast.makeText(this, key + " || " + p.number, Toast.LENGTH_SHORT).show();
            TextView person = new TextView(EditContactsActivity.this);
            person.setText(key + " "  + p.number);

            contactsTable.addView(person);*/
            //Log.i("myEC", entry.getKey() + ": " + entry.getValue().toString());
        }
    }
}
