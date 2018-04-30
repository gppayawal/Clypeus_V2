package com.example.gpepayawal.v2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by gpepayawal on 3/20/18.
 */

public class Tutorials3 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundledInstance){
        super.onCreate(bundledInstance);
        setContentView(R.layout.layout_title);

        Button back = (Button) findViewById(R.id.btn_back);
        Button gotIt = (Button) findViewById(R.id.btn_gotit);

        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i = new Intent(getApplicationContext(), Tutorials.class);
                startActivity(i);
                finish();
            }
        });

        gotIt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}
