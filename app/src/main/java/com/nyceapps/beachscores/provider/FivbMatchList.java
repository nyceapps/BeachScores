package com.nyceapps.beachscores.provider;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.nyceapps.beachscores.entity.Event;
import com.nyceapps.beachscores.entity.Match;
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

public class FivbMatchList extends AsyncTask<Event, Void, List<Match>> {
    private Event event;
    private MatchListResponse delegate;

    public FivbMatchList(MatchListResponse pDelegate) {
        delegate = pDelegate;
    }

    @Override
    protected List<Match> doInBackground(Event... params) {
        event = params[0];

        String response = ServiceUtils.getResponseString(FivbXmlUtils.getRequestBaseUrl(), "Request", getBodyContent());

        List<Match> matchList = processXml(response);
        /*
        Collections.sort(matchList, new Comparator<Event>() {
            @Override
            public int compare(Event e0, Event e1) {
                return e0.getStartDate().compareTo(e1.getStartDate());
            }
        });
        */
        return matchList;
    }

    private List<Match> processXml(String pResponse) {
        List<Match> matchList = new ArrayList<>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(pResponse));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("Event".equals(xpp.getName())) {
                        Match match = new Match();
                        for (int i = 0; i < xpp.getAttributeCount(); i++) {
                            String attrName = xpp.getAttributeName(i);
                            String attrValue = xpp.getAttributeValue(i);
                            if ("TeamAName".equals(attrName)) {
                                match.setTeamAName(attrValue);
                            } else if ("TeamBName".equals(attrName)) {
                                match.setTeamBName(attrValue);
                            }
                        }
                        matchList.add(match);
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return matchList;
    }

    @Override
    protected void onPostExecute(List<Match> pMatchList) {
        delegate.processMatchList(pMatchList);
    }

    private String getBodyContent() {
        Map<String, String> reqVals = new HashMap<>();
        reqVals.put("Type", "GetBeachMatchList");
        reqVals.put("Fields", "NoInTournament LocalDate LocalTime TeamAName TeamBName Court MatchPointsA MatchPointsB PointsTeamASet1 PointsTeamBSet1 PointsTeamASet2 PointsTeamBSet2 PointsTeamASet3 PointsTeamBSet3 DurationSet1 DurationSet2 DurationSet3");
        Map<String, String> filtVals = new HashMap<>();
        filtVals.put("NoTournament", String.valueOf(event.getNo()));

        String requestBody = FivbXmlUtils.getRequestString(FivbXmlUtils.getSingleRequestString(reqVals, filtVals));
        return requestBody;
    }
}
