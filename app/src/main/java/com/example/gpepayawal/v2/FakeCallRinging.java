package com.example.gpepayawal.v2;

/**
 * Created by gpepayawal on 3/13/18.
 */

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class FakeCallRinging extends AppCompatActivity {
    private String networkCarrier;
    private MediaPlayer ringTone;
    TextView titleBar;
    TextView fakeNumber;
    Button answerCall;
    Button rejectCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_call_ringing);

        InitialiseControl();

        GetNetworkOperatorName();
        AssignFakeNumberAndDisplay();
        StartRingTone();

        answerCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ringTone.stop();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        rejectCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ringTone.stop();
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //startActivity(homeIntent);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
    private void InitialiseControl() {
        fakeNumber = (TextView)findViewById(R.id.chosenfakenumber);
        answerCall = (Button)findViewById(R.id.answercall);
        rejectCall = (Button)findViewById(R.id.rejectcall);
    }

    private void StartRingTone() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringTone = MediaPlayer.create(getApplicationContext(), notification);
        ringTone.start();
    }

    private void AssignFakeNumberAndDisplay() {
        String number = getContactNumber();
        fakeNumber.setText(number);
    }

    private void GetNetworkOperatorName() {
        final TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        networkCarrier = tm.getNetworkOperatorName();

        titleBar = (TextView)findViewById(R.id.textView1);
        if(networkCarrier != null){
            titleBar.setText("Incoming call - " + networkCarrier);
        }else{
            titleBar.setText("Incoming call");
        }
    }

    private String getContactNumber(){
        String contact = null;
        Intent myIntent = getIntent();
        Bundle mIntent = myIntent.getExtras();
        if(mIntent != null){
            contact  = mIntent.getString("fakeNumber");
        }
        return contact;
    }
}
