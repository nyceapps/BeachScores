package com.nyceapps.beachscores.provider;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.nyceapps.beachscores.entity.Event;
import com.nyceapps.beachscores.entity.Match;
import com.nyceapps.beachscores.entity.MatchMap;
import com.nyceapps.beachscores.util.FivbXmlUtils;
import com.nyceapps.beachscores.util.ServiceUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lugosi on 21.05.17.
 */

public class FivbMatchList extends AsyncTask<Event, Void, MatchMap> {
    private Event event;
    private MatchListResponse delegate;

    public FivbMatchList(MatchListResponse pDelegate) {
        delegate = pDelegate;
    }

    @Override
    protected MatchMap doInBackground(Event... params) {
        event = params[0];

        String response = ServiceUtils.getResponseString(FivbXmlUtils.getRequestBaseUrl(), "Request", getBodyContent());

        MatchMap matchMap = processXml(response);
        /*
        Collections.sort(matchList, new Comparator<Event>() {
            @Override
            public int compare(Event e0, Event e1) {
                return e0.getStartDate().compareTo(e1.getStartDate());
            }
        });
        */
        return matchMap;
    }

    private MatchMap processXml(String pResponse) {
        MatchMap matchMap = new MatchMap();
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
                                    match.setStatus(status);
                                }
                            } else if ("LocalDate".equals(attrName)) {
                                localDateStr = attrValue;
                            } else if ("LocalTime".equals(attrName)) {
                                localTimeStr = attrValue;
                            } else if ("TeamAName".equals(attrName)) {
                                match.setTeamAName(attrValue);
                            } else if ("TeamBName".equals(attrName)) {
                                match.setTeamBName(attrValue);
                            } else if ("Court".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    int court = Integer.parseInt(attrValue);
                                    match.setCourt(court);
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
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                Date localDate = df.parse(localDateTimeStr);
                                match.setLocalDate(localDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
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

    @Override
    protected void onPostExecute(MatchMap pMatchMap) {
        delegate.processMatchList(pMatchMap);
    }

    private String getBodyContent() {
        StringBuilder matchListReqs = new StringBuilder();
        Map<String, String> reqVals = new HashMap<>();
        reqVals.put("Type", "GetBeachMatchList");
        reqVals.put("Fields", "No NoTournament NoInTournament RoundName RoundPhase Status LocalDate LocalTime TeamAName TeamBName Court PointsTeamASet1 PointsTeamBSet1 PointsTeamASet2 PointsTeamBSet2 PointsTeamASet3 PointsTeamBSet3 DurationSet1 DurationSet2 DurationSet3");
        Map<String, String> filtVals = new HashMap<>();
        if (event.hasWomenTournament()) {
            filtVals.put("NoTournament", String.valueOf(event.getWomenTournamentNo()));
            matchListReqs.append(FivbXmlUtils.getSingleRequestString(reqVals, filtVals));
        }
        if (event.hasMenTournament()) {
            filtVals.put("NoTournament", String.valueOf(event.getMenTournamentNo()));
            matchListReqs.append(FivbXmlUtils.getSingleRequestString(reqVals, filtVals));
        }
        String requestBody = FivbXmlUtils.getRequestString(matchListReqs.toString());
        return requestBody;
    }
}
