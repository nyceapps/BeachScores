package com.nyceapps.beachscores.provider;

import android.os.AsyncTask;

import com.nyceapps.beachscores.entity.Event;
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

public class FivbEventList extends AsyncTask<Void, Void, List<Event>> {
    private final static String BASE_URL = "http://www.fivb.org/Vis2009/XmlRequest.asmx";

    private EventListResponse delegate;

    public FivbEventList(EventListResponse pDelegate) {
        delegate = pDelegate;
    }

    @Override
    protected List<Event> doInBackground(Void... params) {
        String response = ServiceUtils.getResponseString(BASE_URL, "Request", getBodyContent());

        List<Event> eventList = processXml(response);
        Collections.sort(eventList, new Comparator<Event>() {
            @Override
            public int compare(Event e0, Event e1) {
                return e0.getStartDate().compareTo(e1.getStartDate());
            }
        });
        return eventList;
    }

    private List<Event> processXml(String pResponse) {
        List<Event> eventList = new ArrayList<>();
        Set<String> tourneyNos = new HashSet<>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(pResponse));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("Event".equals(xpp.getName())) {
                        Event event = new Event();
                        for (int i = 0; i < xpp.getAttributeCount(); i++) {
                            String attrName = xpp.getAttributeName(i);
                            String attrValue = xpp.getAttributeValue(i);
                            if ("No".equals(attrName)) {
                                    event.setNo(attrValue);
                            } else if ("Code".equals(attrName)) {
                                event.setCode(attrValue);
                            } else if ("Name".equals(attrName)) {
                                event.setName(attrValue);
                            } else if ("StartDate".equals(attrName)) {
                                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                try {
                                    Date start = df.parse(attrValue);
                                    event.setStartDate(start);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else if ("EndDate".equals(attrName)) {
                                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                try {
                                    Date end = df.parse(attrValue);
                                    event.setEndDate(end);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else if ("Content".equals(attrName)) {
                                processTournamentData(event, attrValue, tourneyNos);
                            }
                        }
                        eventList.add(event);
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        processAdditionalTournamentData(eventList, tourneyNos);

        return eventList;
    }

    private void processTournamentData(Event pEvent, String pXml, Set<String> pTourneyNos) {
        Map<String, String> tournaments = new HashMap<>();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(pXml));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("BeachTournament".equals(xpp.getName())) {
                        String key = null;
                        String value = null;
                        for (int i = 0; i < xpp.getAttributeCount(); i++) {
                            String attrName = xpp.getAttributeName(i);
                            String attrValue = xpp.getAttributeValue(i);
                            if ("Gender".equals(attrName)) {
                                key = attrValue;
                            } else if ("No".equals(attrName)) {
                                value = attrValue;
                                pTourneyNos.add(attrValue);
                            }
                        }
                        if (key != null && value != null) {
                            tournaments.put(key, value);
                        }
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        pEvent.setTournaments(tournaments);
    }

    private void processAdditionalTournamentData(List<Event> pEventList, Set<String> pTourneyNos) {
        if (pTourneyNos.size() > 0) {
            StringBuilder tourneyReqs = new StringBuilder();
            // <Request Type="GetBeachTournament" No="2" Fields="NoEvent Name Title Status Type"/>
            Map<String, String> reqVals = new HashMap<>();
            reqVals.put("Type", "GetBeachTournament");
            reqVals.put("Fields", "NoEvent Name Title Status Type");
            for (String tourneyNo : pTourneyNos) {
                reqVals.put("No", tourneyNo);
                tourneyReqs.append(FivbXmlUtils.getSingleRequestString(reqVals, null));
            }
            String reqBody = FivbXmlUtils.getRequestString(tourneyReqs.toString());
            String response = ServiceUtils.getResponseString(BASE_URL, "Request", reqBody);
        }
    }

    @Override
    protected void onPostExecute(List<Event> pEvents) {
        delegate.processEventList(pEvents);
    }

    private String getBodyContent() {
        Map<String, String> reqVals = new HashMap<>();
        reqVals.put("Type", "GetEventList");
        reqVals.put("Fields", "Code Name StartDate EndDate Content");
        Map<String, String> filtVals = new HashMap<>();
        filtVals.put("IsVisManaged", "true");
        filtVals.put("HasBeachTournament", "true");
        int year = Calendar.getInstance().get(Calendar.YEAR);
        filtVals.put("FirstDate", String.valueOf(year) + "-01-01");
        filtVals.put("LastDate", String.valueOf(year) + "-12-31");

        String requestBody = FivbXmlUtils.getRequestString(FivbXmlUtils.getSingleRequestString(reqVals, filtVals));
        return requestBody;
    }
}
