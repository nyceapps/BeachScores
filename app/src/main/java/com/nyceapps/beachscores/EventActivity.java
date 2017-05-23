package com.nyceapps.beachscores;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.nyceapps.beachscores.entity.Event;

public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Intent intent = getIntent();
        Event event = intent.getParcelableExtra("event");

        String no = event.getNo();
    }
}
