package com.nyceapps.beachscores.provider;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.nyceapps.beachscores.R;
import com.nyceapps.beachscores.entity.Event;
import com.nyceapps.beachscores.entity.Match;
import com.nyceapps.beachscores.entity.MatchMap;
import com.nyceapps.beachscores.util.FivbUtils;
import com.nyceapps.beachscores.util.ServiceUtils;
import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.PilotException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lugosi on 21.05.17.
 */

public class FivbMatchList extends AsyncTask<Event, Integer, MatchMap> {
    private final static String TAG = FivbMatchList.class.getSimpleName();

    private MatchListResponse delegate;
    private final Context context;
    private final boolean firstLoad;

    private Event event;
    private DateTimeZone eventTimeZone;
    private DateTimeFormatter eventDateFormatter;
    private DateTimeZone myTimeZone;
    private Map<String, Drawable> fedFlagMap = new HashMap<>();
    private String teamNameBye;
    private String teamNameTba;
    private List<Integer> existingRounds = new ArrayList<>();

    public FivbMatchList(MatchListResponse pDelegate, Context pContext, boolean pFirstLoad) {
        delegate = pDelegate;
        context = pContext;
        firstLoad = pFirstLoad;

        teamNameBye = context.getString(R.string.team_name_bye);
        teamNameTba = context.getString(R.string.team_name_tba);
    }

    @Override
    protected MatchMap doInBackground(Event... params) {
        event = params[0];

        eventTimeZone = event.getTimeZone();
        eventDateFormatter = null;
        if (eventTimeZone != null) {
            eventDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(eventTimeZone);
        } else {
            eventDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        }
        myTimeZone = DateTimeZone.getDefault();

        String response = ServiceUtils.getPostResponseString(FivbUtils.getRequestBaseUrl(), "Request", getBodyContent());

        MatchMap matchMap = processXml(response);
        matchMap.sort();

        if (event.hasWomenTournament()) {
            matchMap.addGender(0, context.getString(R.string.gender_women));
        }
        if (event.hasMenTournament()) {
            matchMap.addGender(1, context.getString(R.string.gender_men));
        }

        if (existingRounds.contains(4)) {
            matchMap.addRound(4, context.getString(R.string.round_name_main_draw));
        }
        if (existingRounds.contains(3)) {
            matchMap.addRound(3, context.getString(R.string.round_name_qualification));
        }
        if (existingRounds.contains(2)) {
            matchMap.addRound(2, context.getString(R.string.round_name_federation_quota));
        }
        if (existingRounds.contains(1)) {
            matchMap.addRound(1, context.getString(R.string.round_name_confederation_quota));
        }

        return matchMap;
    }

    private MatchMap processXml(String pResponse) {
        MatchMap matchMap = new MatchMap(firstLoad);

        if (TextUtils.isEmpty(pResponse)) {
            return matchMap;
        }

        long womenTournamentNo = event.getWomenTournamentNo();
        long menTournamentNo = event.getMenTournamentNo();

        int matchTotal = 0;

        try {
            long parseStart = System.currentTimeMillis();

            VTDGen vg = new VTDGen();
            vg.setDoc(pResponse.getBytes());
            vg.parse(true);

            VTDNav vn = vg.getNav();
            AutoPilot ap = new AutoPilot(vn);

            if (event.hasWomenTournament()) {
                ap.selectXPath("/Responses/BeachTournament[@No=\"" + event.getWomenTournamentNo() + "\"]/@Status");
                int womenStatus = (int) ap.evalXPathToNumber();
                ap.resetXPath();

                if (womenStatus < 6) {
                    matchMap.setWomenScheduled(true);
                } else if (womenStatus == 6) {
                    matchMap.setWomenRunning(true);
                } else {
                    matchMap.setWomenFinished(true);
                }
            }
            if (event.hasMenTournament()) {
                ap.selectXPath("/Responses/BeachTournament[@No=\"" + event.getMenTournamentNo() + "\"]/@Status");
                int menStatus = (int) ap.evalXPathToNumber();
                ap.resetXPath();

                if (menStatus < 6) {
                    matchMap.setMenScheduled(true);
                } else if (menStatus == 6) {
                    matchMap.setMenRunning(true);
                } else {
                    matchMap.setMenFinished(true);
                }
            }

            ap.selectXPath("/Responses/BeachMatches[1]/@NbItems");
            matchTotal += (int) ap.evalXPathToNumber();
            ap.resetXPath();
            ap.selectXPath("/Responses/BeachMatches[2]/@NbItems");
            matchTotal += (int) ap.evalXPathToNumber();
            ap.resetXPath();

            ap.selectXPath("/Responses/BeachMatches/BeachMatch");

            int matchCount = 0;
            publishProgress(matchCount, matchTotal);

            int i = -1;
            while ((i = ap.evalXPath()) != -1) {
                Match match = new Match();

                matchCount++;
                publishProgress(matchCount, matchTotal);

                long no = vn.parseLong(vn.getAttrVal("No"));
                match.setNo(no);

                long tournamentNo = vn.parseLong(vn.getAttrVal("NoTournament"));
                match.setTournamentNo(tournamentNo);

                int noInTournament = vn.parseInt(vn.getAttrVal("NoInTournament"));
                match.setNoInTournament(noInTournament);

                int roundPhase = vn.parseInt(vn.getAttrVal("RoundPhase"));
                match.setRoundPhase(roundPhase);
                existingRounds.add(roundPhase);
                String roundName = vn.toString(vn.getAttrVal("RoundName"));
                match.setRoundName(roundName);

                int status = vn.parseInt(vn.getAttrVal("Status"));
                if (status == 1) {
                    match.setScheduled(true);
                } else if (status >= 2 && status <= 11) {
                    match.setRunning(true);
                } else if (status >= 12) {
                    match.setFinished(true);
                }
                switch (status) {
                    case 3:
                        match.setSet1Running(true);
                        break;
                    case 4:
                        match.setSet1Finished(true);
                        break;
                    case 5:
                        match.setSet1Finished(true);
                        match.setSet2Running(true);
                        break;
                    case 6:
                        match.setSet1Finished(true);
                        match.setSet2Finished(true);
                        break;
                    case 7:
                        match.setSet1Finished(true);
                        match.setSet2Finished(true);
                        match.setSet3running(true);
                        break;
                    default:
                        if (status >= 8) {
                            match.setSet1Finished(true);
                            match.setSet2Finished(true);
                            match.setSet3Finished(true);
                        }
                        break;
                }

                String localDateStr = vn.toString(vn.getAttrVal("LocalDate"));
                String localTimeStr = vn.toString(vn.getAttrVal("LocalTime"));
                if (!TextUtils.isEmpty(localDateStr)) {
                    String localDateTimeStr = localDateStr + " ";
                    if (!TextUtils.isEmpty(localTimeStr)) {
                        localDateTimeStr += localTimeStr;
                    } else {
                        localDateTimeStr += "00:00:00";
                    }

                    DateTime localDateTime = DateTime.parse(localDateTimeStr, eventDateFormatter);
                    DateTime myDateTime = null;
                    if (eventTimeZone != null) {
                        myDateTime = localDateTime.toDateTime(myTimeZone);
                    }
                    if (myDateTime != null) {
                        match.setMyDateTime(null);
                    }
                    match.setLocalDateTime(localDateTime);
                    match.setMyDateTime(myDateTime);
                }

                long noTeamA = vn.parseLong(vn.getAttrVal("NoTeamA"));
                match.setNoTeamA(noTeamA);
                if (noTeamA == -1) {
                    match.setBye(true);
                    match.setTeamAName(teamNameBye);
                } else  if (noTeamA == 0) {
                    match.setTeamAName(teamNameTba);
                } else {
                    String teamAName = vn.toString(vn.getAttrVal("TeamAName"));
                    match.setTeamAName(teamAName);
                }
                String teamAFederationCode = vn.toString(vn.getAttrVal("TeamAFederationCode"));
                match.setTeamAFederationCode(teamAFederationCode);
                Drawable federationADrawable = getFederationDrawable(teamAFederationCode);
                match.setTeamAFederationFlag(federationADrawable);

                long noTeamB = vn.parseLong(vn.getAttrVal("NoTeamB"));
                match.setNoTeamB(noTeamB);
                if (noTeamB == -1) {
                    match.setBye(true);
                    match.setTeamBName(teamNameBye);
                } else  if (noTeamB == 0) {
                    match.setTeamBName(teamNameTba);
                } else {
                    String teamBName = vn.toString(vn.getAttrVal("TeamBName"));
                    match.setTeamBName(teamBName);
                }
                String teamBFederationCode = vn.toString(vn.getAttrVal("TeamBFederationCode"));
                match.setTeamBFederationCode(teamBFederationCode);
                Drawable federationBDrawable = getFederationDrawable(teamBFederationCode);
                match.setTeamBFederationFlag(federationBDrawable);


                String court = vn.toString(vn.getAttrVal("Court"));
                match.setCourt(court);

                int pointsTeamASet1 = parseInt(vn, "PointsTeamASet1");
                match.setPointsTeamASet1(pointsTeamASet1);
                int pointsTeamBSet1 = parseInt(vn, "PointsTeamBSet1");
                match.setPointsTeamBSet1(pointsTeamBSet1);
                int pointsTeamASet2 = parseInt(vn, "PointsTeamASet2");
                match.setPointsTeamASet2(pointsTeamASet2);
                int pointsTeamBSet2 = parseInt(vn, "PointsTeamBSet2");
                match.setPointsTeamBSet2(pointsTeamBSet2);
                int pointsTeamASet3 = parseInt(vn, "PointsTeamASet3");
                match.setPointsTeamASet3(pointsTeamASet3);
                int pointsTeamBSet3 = parseInt(vn, "PointsTeamBSet3");
                match.setPointsTeamBSet3(pointsTeamBSet3);

                int durationSet1 = parseInt(vn, "DurationSet1");
                match.setDurationSet1(durationSet1);
                int durationSet2 = parseInt(vn, "DurationSet2");
                match.setDurationSet2(durationSet2);
                int durationSet3 = parseInt(vn, "DurationSet3");
                match.setDurationSet3(durationSet3);

                int currGender = -1;
                if (tournamentNo == womenTournamentNo) {
                    currGender = 0;
                } else if (tournamentNo == menTournamentNo) {
                    currGender = 1;
                }

                matchMap.put(currGender, roundPhase, match);
            }

            long parseTime = System.currentTimeMillis() - parseStart;
            Log.i(TAG, String.format("parseTime = %d", parseTime));
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (XPathParseException e) {
            e.printStackTrace();
        } catch (PilotException e) {
            e.printStackTrace();
        } catch (NavException e) {
            e.printStackTrace();
        } catch (XPathEvalException e) {
            e.printStackTrace();
        }

        return matchMap;
    }

    private int parseInt(VTDNav pNav, String pName) {
        int i = -1;

        try {
            String valStr = pNav.toString(pNav.getAttrVal(pName));
            if (!TextUtils.isEmpty(valStr)) {
                i = Integer.parseInt(valStr);
            }
        } catch (NavException e) {
            e.printStackTrace();
        }

        return i;
    }

    private Drawable getFederationDrawable(String pFederationCode) {
        Drawable fedBitmap = fedFlagMap.get(pFederationCode);

        if (fedBitmap == null) {
            String flagUrl = FivbUtils.getFederationFlagUrl(pFederationCode);
            if (!TextUtils.isEmpty(flagUrl)) {
                InputStream in = null;
                try {
                    in = new java.net.URL(flagUrl).openStream();
                    fedBitmap = new BitmapDrawable(context.getResources(), in);
                    fedFlagMap.put(pFederationCode, fedBitmap);
                } catch (Exception e) {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e1) {
                            //
                        }
                    }
                }
            }
        }

        return fedBitmap;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        delegate.processMatchProgress(values[0], values[1]);
    }

    @Override
    protected void onPostExecute(MatchMap pMatchMap) {
        delegate.processMatchList(pMatchMap);
    }

    private String getBodyContent() {
        StringBuilder matchListReqs = new StringBuilder();
        Map<String, String> reqVals = new HashMap<>();
        reqVals.put("Type", "GetBeachTournament");
        reqVals.put("Fields", "No Status");
        if (event.hasWomenTournament()) {
            reqVals.put("No", String.valueOf(event.getWomenTournamentNo()));
            matchListReqs.append(FivbUtils.getSingleRequestString(reqVals, null));
        }
        if (event.hasMenTournament()) {
            reqVals.put("No", String.valueOf(event.getMenTournamentNo()));
            matchListReqs.append(FivbUtils.getSingleRequestString(reqVals, null));
        }
        reqVals = new HashMap<>();
        reqVals.put("Type", "GetBeachMatchList");
        reqVals.put("Fields", "No NoTournament NoInTournament RoundName RoundPhase Status LocalDate LocalTime NoTeamA TeamAName TeamAFederationCode NoTeamB TeamBName TeamBFederationCode Court PointsTeamASet1 PointsTeamBSet1 PointsTeamASet2 PointsTeamBSet2 PointsTeamASet3 PointsTeamBSet3 DurationSet1 DurationSet2 DurationSet3");
        Map<String, String> filtVals = new HashMap<>();
        if (event.hasWomenTournament()) {
            filtVals.put("NoTournament", String.valueOf(event.getWomenTournamentNo()));
            matchListReqs.append(FivbUtils.getSingleRequestString(reqVals, filtVals));
        }
        if (event.hasMenTournament()) {
            filtVals.put("NoTournament", String.valueOf(event.getMenTournamentNo()));
            matchListReqs.append(FivbUtils.getSingleRequestString(reqVals, filtVals));
        }
        String requestBody = FivbUtils.getRequestString(matchListReqs.toString());
        return requestBody;
    }
}
