package com.example.gpepayawal.v2;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {
    static MainActivity instance;   //used by myService to cater for volume button events
    SessionManager session;  //for storing user preferences
    private static String username = "";
    private static String pin = "";

    private static final int REQUEST_SEND_SMS = 0;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CALL_PHONE = 2;
    private static final int REQUEST_READ_CONTACTS = 444;
    private ProgressDialog pDialog;
    private ListView mobileContactsListView;
    private Handler updateBarHandler;
    private boolean contactsConfirmed = false;
    Map<String, ContactPerson> directory = new HashMap<>();
    TableLayout contactsTable;
    ArrayList<String> mobileContactsList;
    Set<String> selectedContacts;
    Cursor cursor;
    int counter;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    LocationHelper.LocationResult locationResult;
    LocationHelper locationHelper;
    boolean hasConnection = false;
    //UserParams userParams;
    String city, state, zip, country, street;

    ImageView banner;
    int tapCounter = 0;
    boolean isSOSTriggered = false;

    Thread broadcastSOS;
    String message = "";
    double latitude = 0d;
    double longitude = 0d;
    AudioManager audioManager;
    boolean callGranted = false;

    TelephonyManager tm;
    private static final String TAG = "pinguMessage";
    StringBuffer output;
    String yourNumber = null;
    String phoneNumber = null;
    String email = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //HyperTrack.initialize(this, "pk_test_17f2c9a173f114534b14c79a0dddd2fb7e0253e3");
        tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);


        banner = (ImageView) findViewById(R.id.imageView);
        banner.setClickable(false);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();

        HashMap<String, String> user = session.getUserLoginDetails();
        username = user.get(SessionManager.KEY_USERNAME);
        pin = user.get(SessionManager.KEY_PIN);
        Toast.makeText(getApplicationContext(), "Hello! " + username + ". Remember your pin: " + pin, 15000).show();

        //For handling volume button triggers in background service
        instance = this;
        startService(new Intent(this, MyService.class));

        //For handling shake detection
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                if (count == 2) {
                    Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(1000);
                    fakeCall();
                }
            }
        });

        this.getLocationUpdates();
        contactsTable = (TableLayout) findViewById(R.id.contacts_tbl);
        selectedContacts = session.getSelectedContacts();
        if (selectedContacts == null) selectedContacts = new HashSet<String>();
        load();
    }

    protected void readContacts() {
        setContentView(R.layout.read_contacts_layout);

        pDialog = new ProgressDialog(MainActivity.this);
        pDialog.setMessage("Reading contacts...");
        pDialog.setCancelable(false);
        pDialog.show();

        mobileContactsListView = (ListView) findViewById(R.id.list);
        updateBarHandler = new Handler();

        // Since reading contacts takes more time, let's run it on a separate thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                getContacts();
            }
        }).start();

        // Set onclicklistener to the list item
        mobileContactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clicked = mobileContactsList.get(position);
                String[] parts = clicked.split("  ");
                String name = parts[0];
                String num = parts[1];

                if (num.startsWith("09")) {
                    String suff = num.substring(2);
                    num = "+639" + suff;
                }

                directory.put(name, new ContactPerson(name, num));
                addContactPerson();
            }
        });
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) return true;
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }

        return false;
    }

    private boolean maySendSMS() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        if (checkSelfPermission(SEND_SMS) == PackageManager.PERMISSION_GRANTED) return true;
        if (shouldShowRequestPermissionRationale(SEND_SMS)) {
            requestPermissions(new String[]{SEND_SMS}, REQUEST_SEND_SMS);
        } else {
            requestPermissions(new String[]{SEND_SMS}, REQUEST_SEND_SMS);
        }

        return false;
    }

    private boolean mayDropCall() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        if (checkSelfPermission(CALL_PHONE) == PackageManager.PERMISSION_GRANTED) return true;
        if (shouldShowRequestPermissionRationale(CALL_PHONE)) {
            requestPermissions(new String[]{SEND_SMS}, REQUEST_CALL_PHONE);
        } else {
            requestPermissions(new String[]{SEND_SMS}, REQUEST_CALL_PHONE);
        }

        return false;
    }

    public boolean mayAccessLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            return true;
        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        } else {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        }

        return false;
    }

    /*** Callback received when a permissions request has been completed. */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getContacts();
            }

            case REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    sendConfirmationMessage();
            }

            case REQUEST_ACCESS_FINE_LOCATION: {
                getLocationUpdates();
            }

            case REQUEST_CALL_PHONE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupDropCall();
                }
            }
        }
    }

    public void getContacts() {
        if (!mayRequestContacts()) return;

        mobileContactsList = new ArrayList<String>();
        yourNumber = tm.getLine1Number();

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

        ContentResolver contentResolver = getContentResolver();

        cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {
            counter = 0;

            while (cursor.moveToNext()) {
                output = new StringBuffer();

                //Update the progress message
                updateBarHandler.post(new Runnable() {
                    public void run() {
                        pDialog.setMessage("Reading contacts : " + counter++ + "/" + cursor.getCount());
                    }
                });

                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {
                    output.append(name.toUpperCase());
                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                            output.append("  " + phoneNumber);
                        }
                    phoneCursor.close();
                }

                // Add the contact to the ArrayList
                mobileContactsList.add(output.toString());
            }
            // ListView has to be updated using a ui threead
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Collections.sort(mobileContactsList, String.CASE_INSENSITIVE_ORDER);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_layout, R.id.text1, mobileContactsList);
                    mobileContactsListView.setAdapter(adapter);
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

    protected void addContactPerson() { //Add selected contact person in our Emergency List
        setContentView(R.layout.activity_main);
        int counter = 0;

        Set<Map.Entry<String, ContactPerson>> set = directory.entrySet();
        for (Map.Entry<String, ContactPerson> entry : set) {
            counter++;
            String key = entry.getKey();
            ContactPerson p = entry.getValue();

            final TextView person = new TextView(MainActivity.this);
            person.setTextColor(Color.WHITE);
            person.setTextSize(16);
            person.setId(counter);
            person.setText(key.toUpperCase() + " " + p.number);

            Button btn = new Button(MainActivity.this);
            btn.setText("Remove");
            btn.setWidth(20);
            btn.setBackgroundColor(Color.GRAY);

            selectedContacts.add(key + ">" + p.number);
            contactsTable.addView(person);
        }
        load();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int volume_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int volume_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onResume() {
        super.onPause();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int volume_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onRestart() {
        super.onPause();

        tapCounter = 0;
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int volume_level = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onDestroy() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void confirmContacts(View v) {
        boolean flag = false;

        if (selectedContacts.size() == 0)
            Toast.makeText(this, "Please select at least one Emergency Contact Person", 8000).show();
        else {
            contactsConfirmed = true;

            for(String s : selectedContacts){
                String[] parts = s.split(">");
                if(parts[1].equals(yourNumber)){
                    flag = true;
                    break;
                }
            }

            if(flag){
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Alert");
                alertDialog.setMessage("Just a reminder that you have selected yourself as an Emergency Contact!");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "Confirmation message sent to your selected Emergency Contacts!", Toast.LENGTH_SHORT).show();
                        session.confirmedEmergencyContacts(selectedContacts);
                        sendConfirmationMessage();
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       dialogInterface.cancel();
                    }
                });

                alertDialog.show();
            }
            else{
                Toast.makeText(MainActivity.this, "Confirmation message sent to your selected Emergency Contacts!", Toast.LENGTH_SHORT).show();
                session.confirmedEmergencyContacts(selectedContacts);
                sendConfirmationMessage();
            }

            locationHelper.getLocation(MainActivity.this, MainActivity.this.locationResult);
        }
    }

    public void load() {
        contactsTable = (TableLayout) findViewById(R.id.contacts_tbl);
        contactsTable.removeAllViews();
        for (final String s : selectedContacts) {
            counter++;
            String[] parts = s.split(">");
            TextView person = new TextView(MainActivity.this);
            person.setTextColor(Color.WHITE);
            person.setTextSize(16);
            person.setId(counter);
            person.setText(parts[0] + " " + parts[1]);

            final TableRow tr = new TableRow(MainActivity.this);
            Button btn = new Button(MainActivity.this);
            btn.setBackground(getResources().getDrawable(R.drawable.delete60));
            btn.setLayoutParams(new TableRow.LayoutParams(70, 70));

            tr.addView(person);
            tr.addView(btn);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    contactsTable.removeView(tr);
                    String[] parts = s.split(">");
                    directory.remove(directory.get(parts[0]));
                    selectedContacts.remove(s);
                }
            });
            contactsTable.addView(tr);
        }
    }

    public void editContactsList(View v) {
        if ((directory.size() == 0 || directory.size() < 3) && contactsConfirmed == false)
            readContacts();
        else {
            contactsConfirmed = false;
            directory.clear();
            readContacts();
        }
    }

    public void sendConfirmationMessage() {
        if (!maySendSMS()) return;
        String message = "Hi! You have been selected by " + username + " as her Emergency Contact via Clypeus Personal Safety App";

        if (selectedContacts == null) {
            Toast.makeText(MainActivity.this, "You have not selected any Emergency Contact yet", Toast.LENGTH_SHORT).show();
            return;
        }
        for (String s : selectedContacts) {
            String[] c = s.split(">");  //c[1] stores the number
            sendSMS(c[1], message);
        }
    }

    public void sendSilentSOSMessage() {
        if (!maySendSMS()) return;

        Date currentTime = Calendar.getInstance().getTime();
        if (AppStatus.getInstance(MainActivity.this).isOnline()) {
            message = username + " triggered the Silent SOS button on " + currentTime + ". Location points at " + street + " " + zip + " " + city + " " + state + " " + country;
            message = message.replaceAll("null", "");
            if (selectedContacts == null) {
                Toast.makeText(MainActivity.this, "You have not selected any Emergency Contact yet", Toast.LENGTH_SHORT).show();
                return;
            }
            for (String s : selectedContacts) {
                String[] c = s.split(">");  //c[1] stores the number
                sendSMS(c[1], message);
            }
        } else {
            message = username + " triggered the SOS button on " + currentTime + ". Location points at coordinates (" + String.valueOf(latitude) + "," + String.valueOf(longitude) + ")";
            if (selectedContacts == null) {
                Toast.makeText(MainActivity.this, "You have not selected any Emergency Contact yet", Toast.LENGTH_SHORT).show();
                return;
            }
            for (String s : selectedContacts) {
                String[] c = s.split(">");  //c[1] stores the number
                sendSMS(c[1], message);
            }
        }
    }

    public void sendLoudSOSMessage() {
        if (!maySendSMS()) return;

        Date currentTime = Calendar.getInstance().getTime();
        if (AppStatus.getInstance(MainActivity.this).isOnline()) {
            message = username + " triggered the Loud SOS button on " + currentTime  + ". Location points at " + street + " " + zip + " " + city + " " + state + " " + country;
            message = message.replaceAll("null", "");
            if (selectedContacts == null) {
                Toast.makeText(MainActivity.this, "You have not selected any Emergency Contact yet", Toast.LENGTH_SHORT).show();
                return;
            }
            for (String s : selectedContacts) {
                String[] c = s.split(">");  //c[1] stores the number
                sendSMS(c[1], message);
                //dropCall(c[1]);
            }
        } else {
            message = username + " triggered the SOS button on " + currentTime + ". Location points at coordinates (" + String.valueOf(latitude) + "," + String.valueOf(longitude) + ")";
            if (selectedContacts == null) {
                Toast.makeText(MainActivity.this, "You have not selected any Emergency Contact yet", Toast.LENGTH_SHORT).show();
                return;
            }
            for (String s : selectedContacts) {
                String[] c = s.split(">");  //c[1] stores the number
                sendSMS(c[1], message);
                //dropCall(c[1]);
            }
        }
    }

    public void setupDropCall(){
        for(String s: selectedContacts){
            String[] parts = s.split(">");
            dropCall(parts[1]);
            try {
                Thread.sleep(20000);
            } catch(Exception e){
                Toast.makeText(getApplicationContext(), "Error in your phone call"+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void dropCall(String phoneNumber) {
        if (!mayDropCall()) return;
        try {
            if(Build.VERSION.SDK_INT > 23) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 101);
                    return;
                }

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
                Thread.sleep(10000);
                Class c = Class.forName(tm.getClass().getName());
                Method m = c.getDeclaredMethod("getITelephony");
                m.setAccessible(true);
                Object telephonyService = m.invoke(tm); // Get the internal ITelephony object
                c = Class.forName(telephonyService.getClass().getName()); // Get its class
                m = c.getDeclaredMethod("endCall"); // Get the "endCall()" method
                m.setAccessible(true); // Make it accessible
                m.invoke(telephonyService); // invoke endCall()
            }
            else {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
                Thread.sleep(10000);
                Class c = Class.forName(tm.getClass().getName());
                Method m = c.getDeclaredMethod("getITelephony");
                m.setAccessible(true);
                Object telephonyService = m.invoke(tm); // Get the internal ITelephony object
                c = Class.forName(telephonyService.getClass().getName()); // Get its class
                m = c.getDeclaredMethod("endCall"); // Get the "endCall()" method
                m.setAccessible(true); // Make it accessible
                m.invoke(telephonyService); // invoke endCall()
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error in your phone call"+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void sendSMS(String contactNum, String message){
        boolean flag = false;
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(contactNum, null, message, sentPI, deliveredPI);
        flag = true;
    }

    public void playPanicAlert(){
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.mama_holdap);
        if(audioManager.isMusicActive()){
            int result = audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mediaPlayer.start();
            }
        }
        else{
            mediaPlayer.start();
        }
    }

    public void getLocationUpdates(){
        if(!mayAccessLocation()) return;

        this.locationResult = new LocationHelper.LocationResult(){
            @Override
            public void gotLocation(Location location){
                if(location!=null){
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    //Toast.makeText(getApplicationContext(), "lat: " + latitude + " long: " + longitude, Toast.LENGTH_SHORT).show();
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List<Address> addresses  = null;
                    try {
                        if(addresses == null) {
                            String msg = "List of addresses that matched is empty";
                        }
                        addresses = geocoder.getFromLocation(latitude,longitude, 1);
                        city = addresses.get(0).getLocality();
                        state = addresses.get(0).getAdminArea();
                        zip = addresses.get(0).getPostalCode();
                        street = addresses.get(0).getThoroughfare();
                        country = addresses.get(0).getCountryName();
                        latitude = addresses.get(0).getLatitude();
                        longitude = addresses.get(0).getLongitude();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else Toast.makeText(getApplicationContext(), "Location not found!", Toast.LENGTH_SHORT).show();
            }
        };
        this.locationHelper = new LocationHelper();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP){
            tapCounter++;
            checkForCancel();
        }
        return false;
    }

    public void checkForCancel(){
        if(tapCounter == 20 && isSOSTriggered == true) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Enter PIN to send Cancel SOS Message");
            final EditText input = new EditText(getApplicationContext());
            input.setTextColor(Color.BLACK);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String inputPin = input.getText().toString();
                    Toast.makeText(MainActivity.this, "Cancel SOS message sent to your Emergency Contacts", 8000).show();

                    if(inputPin.equals(pin)){
                        sendCancelSOSMessage();
                        tapCounter = 0;
                    }
                    else{
                        tapCounter = 0;
                        Toast.makeText(MainActivity.this, "Wrong PIN!", Toast.LENGTH_LONG).show();
                        dialogInterface.cancel();
                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            builder.show();
        }
    }

    public void sendCancelSOSMessage(){
        if (!maySendSMS()) return;
        String message = username + " cancelled the SOS alarm. Last location points at " + street + city + state + zip + country;
        message = message.replaceAll("null", "");
        if(selectedContacts == null){
            Toast.makeText(MainActivity.this, "You have not selected any Emergency Contact yet", Toast.LENGTH_SHORT).show();
            return;
        }
        for(String s : selectedContacts) {
            String[] c = s.split(">");  //c[1] stores the number
            sendSMS(c[1], message);
        }
        isSOSTriggered = false;
        Toast.makeText(getApplicationContext(), "Cancel SOS message sent to Emergency Contacts", 8000);
    }

    public void fakeCall(){
        String fakeNumber = "+639153325018";
        Intent fakeRing = new Intent(getApplicationContext(), FakeCallRinging.class);
        fakeRing.putExtra("fakeNumber", fakeNumber);
        startActivity(fakeRing);
    }

    public void showTutorials(View v){
        Intent i = new Intent(getApplicationContext(), Tutorials.class);
        startActivity(i);
    }
}