package com.nyceapps.beachscores.provider;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.nyceapps.beachscores.entity.Event;
import com.nyceapps.beachscores.entity.Match;
import com.nyceapps.beachscores.entity.MatchMap;
import com.nyceapps.beachscores.util.FivbUtils;
import com.nyceapps.beachscores.util.ServiceUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lugosi on 21.05.17.
 */

public class FivbMatchList extends AsyncTask<Event, Void, MatchMap> {
    private Event event;
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

        String response = ServiceUtils.getResponseString(FivbUtils.getRequestBaseUrl(), "Request", getBodyContent());

        MatchMap matchMap = processXml(response);
        matchMap.sort();
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
                            } else if ("NoTeamA".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    long noTeamA = Long.parseLong(attrValue);
                                    match.setNoTeamA(noTeamA);
                                }
                            } else if ("TeamAName".equals(attrName)) {
                                match.setTeamAName(attrValue);
                            } else if ("TeamAFederationCode".equals(attrName)) {
                                match.setTeamAFederationCode(attrValue);
                                Drawable federationDrawable = getFederationDrawable(attrValue);
                                match.setTeamAFederationFlag(federationDrawable);
                            } else if ("NoTeamB".equals(attrName)) {
                                if (!TextUtils.isEmpty(attrValue) && TextUtils.isDigitsOnly(attrValue)) {
                                    long noTeamB = Long.parseLong(attrValue);
                                    match.setNoTeamB(noTeamB);
                                }
                            } else if ("TeamBName".equals(attrName)) {
                                match.setTeamBName(attrValue);
                            } else if ("TeamBFederationCode".equals(attrName)) {
                                match.setTeamBFederationCode(attrValue);
                                Drawable federationDrawable = getFederationDrawable(attrValue);
                                match.setTeamBFederationFlag(federationDrawable);
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

    private Drawable getFederationDrawable(String pFederationCode) {
        Drawable fedBitmap = null;

        if (!TextUtils.isEmpty(pFederationCode)) {
            fedBitmap = fedFlagMap.get(pFederationCode);

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
