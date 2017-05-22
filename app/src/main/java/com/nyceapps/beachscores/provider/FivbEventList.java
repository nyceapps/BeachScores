package com.nyceapps.beachscores.provider;

import android.os.AsyncTask;

import com.nyceapps.beachscores.entity.Event;
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
import java.util.List;
import java.util.Map;

/**
 * Created by lugosi on 21.05.17.
 */

public class FivbEventList extends AsyncTask<Void, Void, List<Event>> {
    private EventListResponse delegate;

    public FivbEventList(EventListResponse pDelegate) {
        delegate = pDelegate;
    }

    @Override
    protected List<Event> doInBackground(Void... params) {
        String baseUrl = "http://www.fivb.org/Vis2009/XmlRequest.asmx";
        String response = ServiceUtils.getResponseString(baseUrl, "Request", getBodyContent());

        List<Event> eventList = processXml(response);
        Collections.sort(eventList, new Comparator<Event>() {
            @Override
            public int compare(Event e0, Event e1) {
                return e0.getStart().compareTo(e1.getStart());
            }
        });
        return eventList;
    }

    private List<Event> processXml(String pResponse) {
        List<Event> eventList = new ArrayList<>();

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
                                    event.setStart(start);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else if ("EndDate".equals(attrName)) {
                                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                try {
                                    Date end = df.parse(attrValue);
                                    event.setEnd(end);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else if ("Content".equals(attrName)) {
                                setTournamentData(event, attrValue);
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

        return eventList;
    }

    private void setTournamentData(Event pEvent, String pXml) {
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

    @Override
    protected void onPostExecute(List<Event> pEvents) {
        delegate.processEventList(pEvents);
    }

    private String getBodyContent() {
        String content = "<Requests><Request Type=\"GetEventList\" Fields=\"Code Name StartDate EndDate Content\"><Filter IsVisManaged=\"true\" HasBeachTournament=\"true\" FirstDate=\"%d-01-01\" LastDate=\"%d-12-31\"/></Request></Requests>";
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String requestBody = String.format(content, year, year);
        return requestBody;
    }
}
