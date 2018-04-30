package com.example.gpepayawal.v2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import com.synnapps.carouselview.CarouselView;
import org.w3c.dom.Text;

public class Login extends AppCompatActivity {
    EditText uname, pin;
    Button loginBtn;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(getApplicationContext());
        uname = (EditText) findViewById(R.id.name);
        pin = (EditText) findViewById(R.id.pin);
        Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener(){
          @Override
          public void onClick(View arg0){
              if(TextUtils.isEmpty(uname.getText()) || TextUtils.isEmpty(pin.getText())) Toast.makeText(getApplicationContext(), "Please enter details needed", Toast.LENGTH_SHORT).show();
              else {
                  session.createLoginSession(uname.getText().toString(), pin.getText().toString());
                  Intent i = new Intent(getApplicationContext(), MainActivity.class);
                  //Intent i = new Intent(getApplicationContext(), Tutorials.class);
                  startActivity(i);
                  finish();
              }
          }
        });
    }
}
