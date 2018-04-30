package com.example.gpepayawal.v2;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by gpepayawal on 3/20/18.
 */

public class Tutorials extends AppCompatActivity{
   @Override
    protected void onCreate(Bundle bundledInstance){
       super.onCreate(bundledInstance);
       setContentView(R.layout.layout_tutorial_2);

       Button next = (Button) findViewById(R.id.btn_next);
       next.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View arg0){
               Intent i = new Intent(getApplicationContext(), Tutorials3.class);
               startActivity(i);
               finish();
           }
       });
   }
}
