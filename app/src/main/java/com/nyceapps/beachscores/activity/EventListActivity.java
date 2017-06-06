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

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventListActivity extends AppCompatActivity implements ActivityDelegate, EventListResponse {
    private static final String KEY_TIMESTAMP = "TIMESTAMP";
    private static final String KEY_EVENT_LIST = "EVENT_LIST";

    private List<Event> eventList;
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

        eventList = getSavedEventList(savedInstanceState);

        if (eventList != null) {
            processEventList(eventList);
        } else {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("loading events...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.setMax(0);
            progressDialog.show();

            FivbEventList fivb = new FivbEventList(this, this);
            fivb.execute();
        }
    }

    private List<Event> getSavedEventList(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            long timestamp = new Date().getTime();
            long savedTimestamp = savedInstanceState.getLong(KEY_TIMESTAMP);

            long diff = (timestamp - savedTimestamp) / 1000 / 60 / 60 / 24;

            if (diff < 1) {
                List<Event> savedEventList = savedInstanceState.getParcelableArrayList(KEY_EVENT_LIST);
                return savedEventList;
            }
        }

        return null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (eventList != null) {
            outState.putLong(KEY_TIMESTAMP, new Date().getTime());
            outState.putParcelableArrayList(KEY_EVENT_LIST, (ArrayList<Event>) eventList);
        }
    }

    @Override
    public void processEventProgress(int pEventCount, int pEventTotal) {
        if (progressDialog != null) {
            progressDialog.setProgress(pEventCount);
            progressDialog.setMax(pEventTotal);
        }
    }

    @Override
    public void processEventList(List<Event> pEventList) {
        eventList = pEventList;

        eventListAdapter.updateList(eventList);

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        int nextEventPos = getNextEventPosition(eventList);
        eventListLayoutManager.scrollToPositionWithOffset(nextEventPos, 0 );
    }

    private int getNextEventPosition(List<Event> pEventList) {
        // TODO: consider header in position count
        int nextEventPos =  0;

        DateTime now = new DateTime();
        for (int i = 0; i < pEventList.size(); i++) {
            Event event = pEventList.get(i);
            DateTime startDateTime = event.getStartDateTime();
            DateTime endDateTime = event.getEndDateTime();
            if (now.isBefore(startDateTime)) {
                nextEventPos = i;
            }
            if (now.isAfter(startDateTime) && now.isBefore(endDateTime)) {
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
