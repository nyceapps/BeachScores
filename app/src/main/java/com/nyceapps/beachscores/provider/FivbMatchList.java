package com.nyceapps.beachscores.provider;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.nyceapps.beachscores.entity.Event;
import com.nyceapps.beachscores.entity.Match;
import com.nyceapps.beachscores.entity.MatchMap;
import com.nyceapps.beachscores.util.FivbUtils;
import com.nyceapps.beachscores.util.ServiceUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lugosi on 21.05.17.
 */

public class FivbMatchList extends AsyncTask<Event, Void, MatchMap> {
    private Event event;
    private DateTimeZone eventTimeZone;
    private DateTimeFormatter eventDateFormatter;
    private DateTimeZone myTimeZone;
    private MatchListResponse delegate;
    private final Context context;
    private Map<String, Drawable> fedFlagMap = new HashMap<>();

    public FivbMatchList(MatchListResponse pDelegate, Context pContext) {
        delegate = pDelegate;
        context = pContext;
    }

    @Override
    protected MatchMap doInBackground(Event... params) {
        event = params[0];

        eventTimeZone = event.getTimeZone();
        eventDateFormatter = null;
        if (eventTimeZone != null) {
            eventDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(eventTimeZone);
        } else {
            eventDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(eventTimeZone);
        }
        myTimeZone = DateTimeZone.getDefault();

        String response = ServiceUtils.getPostResponseString(FivbUtils.getRequestBaseUrl(), "Request", getBodyContent());

        MatchMap matchMap = processXml(response);
        matchMap.sort();
        return matchMap;
    }

    private MatchMap processXml(String pResponse) {
        MatchMap matchMap = new MatchMap();

        if (TextUtils.isEmpty(pResponse)) {
            return matchMap;
        }

        int currGender = -1;
        int currPhase = -1;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(pResponse));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("BeachMatches".equals(xpp.getName())) {
                        currGender++;
                    } else if ("BeachMatch".equals(xpp.getName())) {
                        Match match = new Match();

                        String localDateStr = null;
                        String localTimeStr = null;
                        for (int i = 0; i < xpp.getAttributeCount(); i++) {
                            String attrName = xpp.getAttributeName(i);
                            String attrValue = xpp.getAttributeValue(i);
                            if ("No".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    long no = Long.parseLong(attrValue);
                                    match.setNo(no);
                                }
                            } else if ("NoTournament".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    long tournamentNo = Long.parseLong(attrValue);
                                    match.setTournamentNo(tournamentNo);
                                }
                            } else if ("NoInTournament".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    int noInTournament = Integer.parseInt(attrValue);
                                    match.setNoInTournament(noInTournament);
                                }
                            } else if ("RoundName".equals(attrName)) {
                                match.setRoundName(attrValue);
                            } else if ("RoundPhase".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    int roundPhase = Integer.parseInt(attrValue);
                                    match.setRoundPhase(roundPhase);
                                    currPhase = roundPhase;
                                }
                            } else if ("Status".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    int status = Integer.parseInt(attrValue);
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
                                }
                            } else if ("LocalDate".equals(attrName)) {
                                localDateStr = attrValue;
                            } else if ("LocalTime".equals(attrName)) {
                                localTimeStr = attrValue;
                            } else if ("NoTeamA".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue)) {
                                    if (TextUtils.isDigitsOnly(attrValue)) {
                                        long noTeamA = Long.parseLong(attrValue);
                                        match.setNoTeamA(noTeamA);
                                        if (noTeamA == 0) {
                                            match.setTeamAName("TBA");
                                        }
                                    } else {
                                        match.setBye(true);
                                        match.setTeamAName("BYE");
                                    }
                                }
                            } else if ("TeamAName".equals(attrName)) {
                                if (TextUtils.isEmpty(match.getTeamAName())) {
                                    match.setTeamAName(attrValue);
                                }
                            } else if ("TeamAFederationCode".equals(attrName)) {
                                match.setTeamAFederationCode(attrValue);
                                Drawable federationDrawable = getFederationDrawable(attrValue);
                                match.setTeamAFederationFlag(federationDrawable);
                            } else if ("NoTeamB".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue)) {
                                    if (TextUtils.isDigitsOnly(attrValue)) {
                                        long noTeamB = Long.parseLong(attrValue);
                                        match.setNoTeamB(noTeamB);
                                        if (noTeamB == 0) {
                                            match.setTeamBName("TBA");
                                        }
                                    } else {
                                        match.setBye(true);
                                        match.setTeamBName("BYE");
                                    }
                                }
                            } else if ("TeamBName".equals(attrName)) {
                                if (TextUtils.isEmpty(match.getTeamBName())) {
                                    match.setTeamBName(attrValue);
                                }
                            } else if ("TeamBFederationCode".equals(attrName)) {
                                match.setTeamBFederationCode(attrValue);
                                Drawable federationDrawable = getFederationDrawable(attrValue);
                                match.setTeamBFederationFlag(federationDrawable);
                            } else if ("Court".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    match.setCourt(attrValue);
                                }
                            } else if ("PointsTeamASet1".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    int points = Integer.parseInt(attrValue);
                                    match.setPointsTeamASet1(points);
                                }
                            } else if ("PointsTeamBSet1".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    int points = Integer.parseInt(attrValue);
                                    match.setPointsTeamBSet1(points);
                                }
                            } else if ("PointsTeamASet2".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    int points = Integer.parseInt(attrValue);
                                    match.setPointsTeamASet2(points);
                                }
                            } else if ("PointsTeamBSet2".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    int points = Integer.parseInt(attrValue);
                                    match.setPointsTeamBSet2(points);
                                }
                            } else if ("PointsTeamASet3".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    int points = Integer.parseInt(attrValue);
                                    match.setPointsTeamASet3(points);
                                }
                            } else if ("PointsTeamBSet3".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    int points = Integer.parseInt(attrValue);
                                    match.setPointsTeamBSet3(points);
                                }
                            } else if ("DurationSet1".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    int duration = Integer.parseInt(attrValue);
                                    match.setDurationSet1(duration);
                                }
                            } else if ("DurationSet2".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    int duration = Integer.parseInt(attrValue);
                                    match.setDurationSet2(duration);
                                }
                            } else if ("DurationSet3".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    int duration = Integer.parseInt(attrValue);
                                    match.setDurationSet3(duration);
                                }
                            }
                        }
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

                        matchMap.put(currGender, currPhase, match);
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return matchMap;
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
    protected void onPostExecute(MatchMap pMatchMap) {
        delegate.processMatchList(pMatchMap);
    }

    private String getBodyContent() {
        StringBuilder matchListReqs = new StringBuilder();
        Map<String, String> reqVals = new HashMap<>();
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
