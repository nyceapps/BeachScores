package com.nyceapps.beachscores.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nyceapps.beachscores.R;
import com.nyceapps.beachscores.entity.Event;
import com.nyceapps.beachscores.provider.EventListResponse;
import com.nyceapps.beachscores.provider.FivbEventList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventListActivity extends AppCompatActivity implements ActivityDelegate, EventListResponse {
    private LinearLayoutManager eventListLayoutManager;
    private EventListAdapter eventListAdapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        RecyclerView eventListView = (RecyclerView) findViewById(R.id.event_list_view);


        eventListView.setHasFixedSize(true);

        eventListLayoutManager = new LinearLayoutManager(this);
        eventListView.setLayoutManager(eventListLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(eventListView.getContext(), eventListLayoutManager.getOrientation());
        eventListView.addItemDecoration(dividerItemDecoration);

        List<Event> dummyEventList = new ArrayList<>();
        eventListAdapter = new EventListAdapter(dummyEventList, this, this);
        eventListView.setAdapter(eventListAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("loading...");
        progressDialog.show();

        FivbEventList fivb = new FivbEventList(this);
        fivb.execute();
    }

    @Override
    public void processEventList(List<Event> pEventList) {
        eventListAdapter.updateList(pEventList);
        progressDialog.dismiss();

        int nextEventPos = getNextEventPosition(pEventList);
        eventListLayoutManager.scrollToPositionWithOffset(nextEventPos, 0 );
    }

    private int getNextEventPosition(List<Event> pEventList) {
        // TODO: consider header in position count
        int nextEventPos =  0;

        Date now = new Date();
        for (int i = 0; i < pEventList.size(); i++) {
            Event event = pEventList.get(i);
            Date startDate = event.getStartDate();
            Date endDate = event.getEndDate();
            if (now.before(startDate)) {
                nextEventPos = i;
            }
            if (now.after(startDate) && now.before(endDate)) {
                nextEventPos = i;
                break;
            }
        }

        return nextEventPos;
    }

    @Override
    public void onClick(View v) {
        Event event = (Event) v.getTag();
        if (event != null) {
            Intent intent = new Intent(this, MatchListActivity.class);
            intent.putExtra("event", event);
            startActivity(intent);
        }
    }
}
