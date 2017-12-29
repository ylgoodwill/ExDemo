package com.voicerecognition.android.voicedemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import com.baidu.speech.recognizerdemo.R;

public class ActivityMain extends AppCompatActivity {
    Button voice_Re;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        voice_Re = (Button) findViewById(R.id.voice_re);
        voice_Re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityMain.this,ActivityOnline.class));
            }
        });
    }
}

