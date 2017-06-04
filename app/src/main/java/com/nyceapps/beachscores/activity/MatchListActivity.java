package com.nyceapps.beachscores.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.nyceapps.beachscores.R;
import com.nyceapps.beachscores.entity.Event;
import com.nyceapps.beachscores.entity.Match;
import com.nyceapps.beachscores.entity.MatchMap;
import com.nyceapps.beachscores.provider.FivbMatchList;
import com.nyceapps.beachscores.provider.MatchListResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MatchListActivity extends AppCompatActivity implements ActivityDelegate, MatchListResponse {
    public final static String TIME_DISPLAY_TYPE_LOCAL = "LOCAL";
    public final static String TIME_DISPLAY_TYPE_MY = "MY";

    private Event event;
    private MatchMap matchMap;
    private MatchListAdapter matchListAdapter;
    private boolean loading = false;
    private ProgressDialog progressDialog;
    private Spinner genderSpinner;
    private Spinner phaseSpinner;
    private Spinner timeSpinner;
    private Timer updateTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_list);

        Intent intent = getIntent();
        event = intent.getParcelableExtra("event");

        String title = event.getTitle();
        setTitle(title);

        RecyclerView matchListView = (RecyclerView) findViewById(R.id.match_list_view);

        matchListView.setHasFixedSize(true);

        LinearLayoutManager matchListLayoutManager = new LinearLayoutManager(this);
        matchListView.setLayoutManager(matchListLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(matchListView.getContext(), matchListLayoutManager.getOrientation());
        matchListView.addItemDecoration(dividerItemDecoration);

        List<Match> dummyMatchList = new ArrayList<>();
        matchListAdapter = new MatchListAdapter(dummyMatchList, this, this);
        matchListView.setAdapter(matchListAdapter);

        initializeDropdowns();

        loading = true;

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("loading matches...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupUpdateTask();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }

    private void setupUpdateTask() {
        final Handler handler = new Handler();
        updateTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        FivbMatchList fivb = new FivbMatchList(MatchListActivity.this, MatchListActivity.this);
                        fivb.execute(event);
                    }
                });
            }
        };
        updateTimer.schedule(task, 0, 30 * 1000);
    }

    private void initializeDropdowns() {
        genderSpinner = (Spinner) findViewById(R.id.gender_dropdown);

        List<String> genderItems = new ArrayList<>();
        if (event.hasWomenTournament()) {
            genderItems.add("Women's");
        }
        if (event.hasMenTournament()) {
            genderItems.add("Men's");
        }
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderItems);
        genderSpinner.setAdapter(genderAdapter);
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!loading) {
                    updateMatchList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        phaseSpinner = (Spinner) findViewById(R.id.phase_dropdown);

        String[] phaseItems = { "Main draw", "Qualification", "Country quota" };
        ArrayAdapter<String> phaseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, phaseItems);
        phaseSpinner.setAdapter(phaseAdapter);
        phaseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!loading) {
                    updateMatchList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        timeSpinner = (Spinner) findViewById(R.id.time_dropdown);

        String[] timeItems = { "Local time", "My time" };
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeItems);
        timeSpinner.setAdapter(timeAdapter);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!loading) {
                    updateMatchList();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public void processMatchProgress(int pMatchCount, int pMatchTotal) {
        if (progressDialog != null) {
            progressDialog.setProgress(pMatchCount);
            progressDialog.setMax(pMatchTotal);
        }
    }

    @Override
    public void processMatchList(MatchMap pMatchMap) {
        matchMap = pMatchMap;

        updateMatchList();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        loading = false;
    }

    private void updateMatchList() {
        if (matchMap != null) {
            int currGender = genderSpinner.getSelectedItemPosition();
            int currPhase = 4 - phaseSpinner.getSelectedItemPosition();
            int currTime = timeSpinner.getSelectedItemPosition();
            String currTimeDisplayType = TIME_DISPLAY_TYPE_LOCAL;
            if (currTime == 1) {
                currTimeDisplayType = TIME_DISPLAY_TYPE_MY;
            }

            List<Match> matchList = matchMap.getList(currGender, currPhase);
            matchListAdapter.updateList(matchList, currTimeDisplayType);

            // TODO: cancel timer when all games are finished
        }
    }

    @Override
    public void onClick(View v) {
        /*
        Event event = (Event) v.getTag();
        if (event != null) {
            Intent intent = new Intent(this, MatchListActivity.class);
            intent.putExtra("event", event);
            startActivity(intent);
        }
        */
    }
}
