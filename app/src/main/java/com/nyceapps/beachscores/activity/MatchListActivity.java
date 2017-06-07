package com.nyceapps.beachscores.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private final static String TAG = MatchListActivity.class.getSimpleName();

    private final static int UPDATE_INTERVAL = 30 * 1000;
    public final static String TIME_DISPLAY_TYPE_LOCAL = "LOCAL";
    public final static String TIME_DISPLAY_TYPE_MY = "MY";

    private Event event;
    private MatchMap matchMap;
    private MatchListAdapter matchListAdapter;
    private boolean loading = false;
    private ProgressDialog progressDialog;
    private RecyclerView matchListView;
    private Spinner genderSpinner;
    private Spinner roundSpinner;
    private View tournamentNotStartedMessage;
    private Timer updateTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_list);

        Intent intent = getIntent();
        event = intent.getParcelableExtra("event");

        String title = event.getTitle();
        setTitle(title);

        matchListView = (RecyclerView) findViewById(R.id.match_list_view);

        matchListView.setHasFixedSize(true);

        LinearLayoutManager matchListLayoutManager = new LinearLayoutManager(this);
        matchListView.setLayoutManager(matchListLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(matchListView.getContext(), matchListLayoutManager.getOrientation());
        matchListView.addItemDecoration(dividerItemDecoration);

        genderSpinner = (Spinner) findViewById(R.id.gender_dropdown);
        roundSpinner = (Spinner) findViewById(R.id.round_dropdown);

        tournamentNotStartedMessage = findViewById(R.id.tournament_not_started_message);

        List<Match> dummyMatchList = new ArrayList<>();
        matchListAdapter = new MatchListAdapter(dummyMatchList, this, this);
        matchListView.setAdapter(matchListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadMatches();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }

    private void loadMatches() {
        Log.i(TAG, "Loading...");
        loading = true;

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("loading matches...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(0);
        progressDialog.show();

        FivbMatchList fivb = new FivbMatchList(MatchListActivity.this, MatchListActivity.this, true);
        fivb.execute(event);
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

        initializeView();

        updateMatchList();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        loading = false;

        if (matchMap.isFirstLoad()) {
            if (matchMap.isWomenRunning() || matchMap.isMenRunning()) {
                setupUpdateTask();
            }
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
                        Log.i(TAG, "Updating...");
                        loading = true;

                        FivbMatchList fivb = new FivbMatchList(MatchListActivity.this, MatchListActivity.this, false);
                        fivb.execute(event);
                    }
                });
            }
        };
        updateTimer.schedule(task, UPDATE_INTERVAL, UPDATE_INTERVAL);
    }

    private void initializeView() {
        if (matchMap.isWomenRunning() || matchMap.isMenRunning() || matchMap.isWomenFinished() ||matchMap.isMenFinished()) {
            matchListView.setVisibility(View.VISIBLE);
            genderSpinner.setVisibility(View.VISIBLE);
            roundSpinner.setVisibility(View.VISIBLE);
            tournamentNotStartedMessage.setVisibility(View.INVISIBLE);
        } else {
            matchListView.setVisibility(View.INVISIBLE);
            genderSpinner.setVisibility(View.INVISIBLE);
            roundSpinner.setVisibility(View.INVISIBLE);
            tournamentNotStartedMessage.setVisibility(View.VISIBLE);
        }

        List<String> genderItems = new ArrayList<>();
        for (int gender : matchMap.getGenderList()) {
            genderItems.add(matchMap.getGenderName(gender));
        }

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, R.layout.spinner_row, genderItems);
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

        List<String> roundItems = new ArrayList<>();
        for (Integer round : matchMap.getRoundList()) {
            roundItems.add(matchMap.getRoundName(round));
        }

        ArrayAdapter<String> roundAdapter = new ArrayAdapter<>(this, R.layout.spinner_row, roundItems);
        roundSpinner.setAdapter(roundAdapter);
        roundSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    private int getCurrentGender() {
        if (genderSpinner != null) {
            int pos = genderSpinner.getSelectedItemPosition();
            if (pos > -1) {
                int gender = matchMap.getGenderList().get(pos);
                return gender;
            }
        }

        return -1;
    }

    private int getCurrentRound() {
        if (roundSpinner != null) {
            int pos = roundSpinner.getSelectedItemPosition();
            if (pos > -1) {
                int round = matchMap.getRoundList().get(pos);
                return round;
            }
        }

        return -1;
    }

    private void updateMatchList() {
        if (matchMap != null) {
            int currGender = getCurrentGender();
            int currRound = getCurrentRound();

            if (currGender > -1 && currRound > -1) {
                List<Match> matchList = matchMap.getList(currGender, currRound);
                matchListAdapter.updateList(matchList, TIME_DISPLAY_TYPE_LOCAL);
            }
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
