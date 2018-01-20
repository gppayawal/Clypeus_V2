package com.example.gpepayawal.v2;

/*import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import static android.Manifest.permission.READ_CONTACTS;

public class MainActivity extends Activity {
    private static final int REQUEST_READ_CONTACTS = 444;
    private ListView mListView;
    private ProgressDialog pDialog;
    private Handler updateBarHandler;

    ArrayList<String> contactList;
    Map<String, ArrayList> directory = new HashMap<String, ArrayList>();
    Cursor cursor;
    int counter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //on create: show landing page Emergency Contacts
        showLanding();
    }

    protected void showLanding(){
        if(directory.isEmpty()) Toast.makeText(this, "Emergency Contacts empty!", Toast.LENGTH_SHORT).show();
        else{
            Toast.makeText(this, "Confirm? Edit?", Toast.LENGTH_SHORT).show();
        }
    }

    protected void initialize(){
        Log.i("INITIALIZING", "inside initiliaze()");
        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setMessage("Reading contacts...");
        pDialog.setCancelable(false);
        pDialog.show();

        mListView = (ListView) findViewById(R.id.list);
        updateBarHandler = new Handler();

        // Since reading contacts takes more time, let's run it on a separate thread.
        new Thread(new Runnable() {

            @Override
            public void run() {
                getContacts();
            }
        }).start();

        // Set onclicklistener to the list item.
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //TODO Do whatever you want with the list data
                Toast.makeText(getApplicationContext(), "item clicked : \n" + contactList.get(position), Toast.LENGTH_SHORT).show();
                String name = contactList.get(position);
                Toast.makeText(getApplicationContext(), name, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getContacts();
            }
        }
    }

    public void getContacts() {

        if (!mayRequestContacts()) {
            return;
        }

        contactList = new ArrayList<String>();

        String phoneNumber = null;
        String email = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Uri EmailCONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;

        StringBuffer output;

        ContentResolver contentResolver = getContentResolver();

        cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {

            counter = 0;
            while (cursor.moveToNext()) {
                output = new StringBuffer();

                // Update the progress message
                updateBarHandler.post(new Runnable() {
                    public void run() {
                        pDialog.setMessage("Reading contacts : " + counter++ + "/" + cursor.getCount());
                    }
                });

                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {

                    output.append("\n First Name:" + name);

                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);

                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        output.append("\n Phone number:" + phoneNumber);

                    }

                    phoneCursor.close();

                    // Read every email id associated with the contact
                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI, null, EmailCONTACT_ID + " = ?", new String[]{contact_id}, null);

                    while (emailCursor.moveToNext()) {

                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));

                        output.append("\n Email:" + email);

                    }

                    emailCursor.close();

                    String columns[] = {
                            ContactsContract.CommonDataKinds.Event.START_DATE,
                            ContactsContract.CommonDataKinds.Event.TYPE,
                            ContactsContract.CommonDataKinds.Event.MIMETYPE,
                    };

                    String where = ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
                            " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' and " + ContactsContract.Data.CONTACT_ID + " = " + contact_id;

                    String[] selectionArgs = null;
                    String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;

                    Cursor birthdayCur = contentResolver.query(ContactsContract.Data.CONTENT_URI, columns, where, selectionArgs, sortOrder);
                    Log.d("BDAY", birthdayCur.getCount()+"");
                    if (birthdayCur.getCount() > 0) {
                        while (birthdayCur.moveToNext()) {
                            String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                            output.append("Birthday :" + birthday);
                            Log.d("BDAY", birthday);
                        }
                    }
                    birthdayCur.close();
                }

                // Add the contact to the ArrayList
                contactList.add(output.toString());
            }

            // ListView has to be updated using a ui thread
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_layout, R.id.text1, contactList);
                    mListView.setAdapter(adapter);
                }
            });

            // Dismiss the progressbar after 500 millisecondds
            updateBarHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    pDialog.cancel();
                }
            }, 500);
        }

    }

}*/


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_READ_CONTACTS = 444;
    private ProgressDialog pDialog;
    private ListView mobileContactsListView;
    private Handler updateBarHandler;

    ArrayList<String> mobileContactsList;
    Cursor cursor;
    int counter;

    private static final String TAG = "pinguMessage";
    Button samp = null;
    TableLayout contacts_tbl = null;
    Map<String, String> directory = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("CREATION", "onCreate() is being executed");

        initializeDirectory();
    }

    protected void initializeDirectory() {
        Toast.makeText(this, "initializing contacts...", Toast.LENGTH_SHORT).show();

        if (directory.isEmpty()) {
            Toast.makeText(this, "No contacts found!", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, "Confirm your emergenct contacts", Toast.LENGTH_SHORT).show();

    }

    protected void readContacts(){
        setContentView(R.layout.read_contacts_layout);

        /*ListView listView = (ListView) findViewById(R.id.list);
        ArrayList<String> list = new ArrayList<>();

        list.add("nani??");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_layout, R.id.text1, list);
        listView.setAdapter(adapter);*/

        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setMessage("Reading contacts...");
        pDialog.setCancelable(false);
        pDialog.show();

        mobileContactsListView = (ListView) findViewById(R.id.list);
        updateBarHandler = new Handler();

        // Since reading contacts takes more time, let's run it on a separate thread.
        new Thread(new Runnable(){
            @Override
            public void run(){
                getContacts();
            }
        }).start();

        // Set onclicklistener to the list item.
        int count = 6;
        for(int i = 0; i < 6; i++){
            mobileContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //TODO Do whatever you want with the list data
                    Toast.makeText(getApplicationContext(), "item clicked : \n" + mobileContactsList.get(position), Toast.LENGTH_SHORT).show();

                    String clicked = mobileContactsList.get(position);
                    Toast.makeText(getApplicationContext(), clicked, Toast.LENGTH_LONG).show();

                    String[] parts = clicked.split(" ");
                    String name = parts[0];
                    String num = parts[1];

                    addContactPerson(name, num);
                }
            });
        }
    }

    private boolean mayRequestContacts(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) return true;
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)){
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        } else{
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }

        return false;
    }

    /*** Callback received when a permissions request has been completed. */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if (requestCode == REQUEST_READ_CONTACTS){
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getContacts();
        }
    }

    public void getContacts(){
        if (!mayRequestContacts()) return;

        mobileContactsList = new ArrayList<String>();

        String phoneNumber = null;
        String email = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Uri EmailCONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;

        StringBuffer output;

        ContentResolver contentResolver = getContentResolver();

        cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Iterate every contact in the phone
        if(cursor.getCount() > 0){
            counter = 0;

            while(cursor.moveToNext()){
                output = new StringBuffer();

                //Update the progress message
                updateBarHandler.post(new Runnable(){
                    public void run(){
                        pDialog.setMessage("Reading contacts : " + counter++ + "/" + cursor.getCount());
                    }
                });

                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if(hasPhoneNumber > 0){
                    output.append(name + " ");

                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);

                    while(phoneCursor.moveToNext()){
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        output.append(phoneNumber);
                    }

                    phoneCursor.close();

                    // Read every email id associated with the contact
                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI, null, EmailCONTACT_ID + " = ?", new String[]{contact_id}, null);

                    while(emailCursor.moveToNext()){
                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
                        output.append("\n Email:" + email);
                    }

                    emailCursor.close();

                    String columns[] = {
                            ContactsContract.CommonDataKinds.Event.START_DATE,
                            ContactsContract.CommonDataKinds.Event.TYPE,
                            ContactsContract.CommonDataKinds.Event.MIMETYPE,
                    };

                    String where = ContactsContract.CommonDataKinds.Event.TYPE + "=" + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY +
                            " and " + ContactsContract.CommonDataKinds.Event.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' and " + ContactsContract.Data.CONTACT_ID + " = " + contact_id;

                    String[] selectionArgs = null;
                    String sortOrder = ContactsContract.Contacts.DISPLAY_NAME;

                    /*Cursor birthdayCur = contentResolver.query(ContactsContract.Data.CONTENT_URI, columns, where, selectionArgs, sortOrder);
                    Log.d("BDAY", birthdayCur.getCount()+"");
                    if (birthdayCur.getCount() > 0) {
                        while (birthdayCur.moveToNext()) {
                            String birthday = birthdayCur.getString(birthdayCur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
                            output.append("Birthday :" + birthday);
                            Log.d("BDAY", birthday);
                        }
                    }
                    birthdayCur.close();*/
                }

                // Add the contact to the ArrayList
                mobileContactsList.add(output.toString());
            }

            // ListView has to be updated using a ui thread
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_layout, R.id.text1, mobileContactsList);
                    mobileContactsListView.setAdapter(adapter);
                }
            });

            // Dismiss the progressbar after 500 millisecondds
            updateBarHandler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    pDialog.cancel();
                }
            }, 500);
        }

    }


    protected void addContactPerson(String name, String number) { //Add selected contact person in our Emergency List
        /*setContentView(R.layout.contact_person_form);

        final EditText nameField = (EditText) findViewById(R.id.EditTextName);
        String name = nameField.getText().toString();

        final EditText numberField = (EditText) findViewById(R.id.EditTextNumber);
        String number = numberField.getText().toString(); */

        setContentView(R.layout.activity_main);
        Toast.makeText(this, name + " --- " + number, Toast.LENGTH_LONG).show();


    }

    /*@Override
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
    }*/

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        Toast.makeText(this, "Pressed for a long time =) ", Toast.LENGTH_SHORT).show();
        //vibe.vibrate(50);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                Toast.makeText(this, "Volume Up pressed", Toast.LENGTH_SHORT).show();
                event.startTracking();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Toast.makeText(this, "Volume Down pressed", Toast.LENGTH_SHORT).show();
                event.startTracking();
                return true;
            case KeyEvent.KEYCODE_HOME:
                Toast.makeText(this, "Home button pressed", Toast.LENGTH_SHORT).show();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void confirmContacts(View v) {
        Toast.makeText(this, "Contacts Confirmed!", Toast.LENGTH_SHORT).show();
    }

    public void editContactsList(View v) {

       /* ContactPerson person = new ContactPerson("pau", "09153325018");


        TextView samp = new TextView(MainActivity.this);
        contacts_tbl = (TableLayout) findViewById(R.id.contacts_tbl);

        Resources res = getResources();
        String newContact = res.getString(R.string.newContact, person.name, person.number);

        Toast.makeText(this, person.name, Toast.LENGTH_SHORT).show();


        samp.setText(newContact);
        contacts_tbl.addView(samp);*/
        readContacts();
    }
}

