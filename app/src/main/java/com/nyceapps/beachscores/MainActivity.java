package com.nyceapps.beachscores;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.nyceapps.beachscores.entity.Event;
import com.nyceapps.beachscores.provider.FivbEventList;
import com.nyceapps.beachscores.provider.EventListResponse;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EventListResponse {
    private EventsListAdapter eventsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView eventsListView = (RecyclerView) findViewById(R.id.events_list_view);

        eventsListView.setHasFixedSize(true);

        LinearLayoutManager eventsListLayoutManager = new LinearLayoutManager(this);
        eventsListView.setLayoutManager(eventsListLayoutManager);

        List<Event> dummyEventList = new ArrayList<>();
        eventsListAdapter = new EventsListAdapter(dummyEventList);
        eventsListView.setAdapter(eventsListAdapter);

        FivbEventList fivb = new FivbEventList(this);
        fivb.execute();
    }

    @Override
    public void processEventList(List<Event> pEventList) {
        eventsListAdapter.updateList(pEventList);
    }
}
