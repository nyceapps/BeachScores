package com.nyceapps.beachscores.provider;

import android.os.AsyncTask;
import android.text.TextUtils;

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
                            if (TextUtils.isDigitsOnly(value)) {
                                long genderNo = Long.parseLong(value);
                                if ("W".equalsIgnoreCase(key)) {
                                    pEvent.setWomenTournamentNo(genderNo);
                                } else {
                                    pEvent.setMenTournamentNo(genderNo);
                                }
                            }
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
    }

    private void processAdditionalTournamentData(List<Event> pEventList, Set<String> pTourneyNos) {
        if (pTourneyNos.size() > 0) {
            StringBuilder tourneyReqs = new StringBuilder();
            Map<String, String> reqVals = new HashMap<>();
            reqVals.put("Type", "GetBeachTournament");
            reqVals.put("Fields", "NoEvent Name Title Status Type");
            for (String tourneyNo : pTourneyNos) {
                reqVals.put("No", tourneyNo);
                tourneyReqs.append(FivbXmlUtils.getSingleRequestString(reqVals, null));
            }
            String reqBody = FivbXmlUtils.getRequestString(tourneyReqs.toString());
            String response = ServiceUtils.getResponseString(BASE_URL, "Request", reqBody);

            Map<String, Event> tourneyData = new HashMap<>();
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new StringReader(response));
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if ("BeachTournament".equals(xpp.getName())) {
                            Event event = new Event();
                            String eventNo = null;
                            for (int i = 0; i < xpp.getAttributeCount(); i++) {
                                String attrName = xpp.getAttributeName(i);
                                String attrValue = xpp.getAttributeValue(i);
                                if ("NoEvent".equals(attrName)) {
                                    eventNo = attrValue;
                                } else if ("Status".equals(attrName)) {
                                    event.setStatus(attrValue);
                                } else if ("Type".equals(attrName)) {
                                    event.setType(attrValue);
                                } else if ("Name".equals(attrName)) {
                                    event.setName(attrValue);
                                } else if ("Title".equals(attrName)) {
                                    event.setTitle(attrValue);
                                }
                            }

                            if (isEventQualified(event)) {
                                if (tourneyData.get(eventNo) == null) {
                                    processNameAndTitle(event);
                                    tourneyData.put(eventNo, event);
                                }
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

            for (int i = pEventList.size() - 1; i >= 0; i--) {
                Event mainEvent = pEventList.get(i);
                Event tourneyEvent = tourneyData.get(mainEvent.getNo());
                if (tourneyEvent != null) {
                    mainEvent.setName(tourneyEvent.getName());
                    mainEvent.setTitle(tourneyEvent.getTitle());
                    mainEvent.setType(tourneyEvent.getType());
                    mainEvent.setStatus(tourneyEvent.getStatus());
                    pEventList.set(i, mainEvent);
                } else {
                    pEventList.remove(i);
                }
            }
        }
    }

    private boolean isEventQualified(Event pEvent) {
        String status = pEvent.getStatus();
        if (!"1".equals(status) && !"6".equals(status) && !"7".equals(status) && !"8".equals(status) && !"8".equals(status)) {
            return false;
        }
        String type = pEvent.getType();
        if ("35".equals(type)) {
            return false;
        }
        String name = pEvent.getName();
        String title = pEvent.getTitle();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(title)) {
            return false;
        }
        name = name.toLowerCase();
        title = title.toLowerCase();
        if (name.contains("cancel") || title.contains("cancel")) {
            return false;
        }
        return true;
    }

    private void processNameAndTitle(Event pEvent) {
        String name = pEvent.getName();
        name = name.replaceAll("(?i)women's", "").replaceAll("(?i)men's", "").replaceAll(" {2,}", " ");
        pEvent.setName(name);
        String title = pEvent.getTitle();
        title = title.replaceAll("(?i)women's", "").replaceAll("(?i)men's", "").replaceAll(" {2,}", " ");
        pEvent.setTitle(title);
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
