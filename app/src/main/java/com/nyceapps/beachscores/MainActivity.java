package com.nyceapps.beachscores;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.nyceapps.beachscores.entity.Event;
import com.nyceapps.beachscores.provider.FivbEventList;
import com.nyceapps.beachscores.provider.EventListResponse;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EventListResponse {
    private EventListAdapter eventListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView eventListView = (RecyclerView) findViewById(R.id.event_list_view);

        eventListView.setHasFixedSize(true);

        LinearLayoutManager eventListLayoutManager = new LinearLayoutManager(this);
        eventListView.setLayoutManager(eventListLayoutManager);

        List<Event> dummyEventList = new ArrayList<>();
        eventListAdapter = new EventListAdapter(dummyEventList);
        eventListView.setAdapter(eventListAdapter);

        FivbEventList fivb = new FivbEventList(this);
        fivb.execute();
    }

    @Override
    public void processEventList(List<Event> pEventList) {
        eventListAdapter.updateList(pEventList);
    }
}
