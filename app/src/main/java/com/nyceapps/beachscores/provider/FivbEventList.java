package com.nyceapps.beachscores.provider;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.nyceapps.beachscores.entity.Event;
import com.nyceapps.beachscores.util.FivbUtils;
import com.nyceapps.beachscores.util.GeoUtils;
import com.nyceapps.beachscores.util.PreferencesUtils;
import com.nyceapps.beachscores.util.ServiceUtils;
import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lugosi on 21.05.17.
 */

public class FivbEventList extends AsyncTask<Void, Integer, List<Event>> {
    private EventListResponse delegate;
    private final Context context;
    private DateTimeFormatter eventDateFormatter;

    public FivbEventList(EventListResponse pDelegate, Context pContext) {
        delegate = pDelegate;
        context = pContext;
    }

    @Override
    protected List<Event> doInBackground(Void... params) {
        eventDateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");

        String response = ServiceUtils.getPostResponseString(FivbUtils.getRequestBaseUrl(), "Request", getBodyContent());

        List<Event> eventList = processXml(response);
        Collections.sort(eventList, new Comparator<Event>() {
            @Override
            public int compare(Event e0, Event e1) {
                return e0.getStartDateTime().compareTo(e1.getStartDateTime());
            }
        });

        return eventList;
    }

    private List<Event> processXml(String pResponse) {
        List<Event> eventList = new ArrayList<>();

        if (TextUtils.isEmpty(pResponse)) {
            return eventList;
        }

        Set<Long> tourneyNos = new HashSet<>();

        try {
            VTDGen vg = new VTDGen();
            vg.setDoc(pResponse.getBytes());
            vg.parse(true);

            VTDNav vn = vg.getNav();
            AutoPilot ap = new AutoPilot(vn);

            ap.selectXPath("/Responses/Events/@NbItems");
            int eventTotal = (int) ap.evalXPathToNumber();
            ap.resetXPath();

            ap.selectXPath("/Responses/Events/Event");

            int eventCount = 0;
            publishProgress(eventCount, eventTotal);

            int i = -1;
            while ((i = ap.evalXPath()) != -1) {
                Event event = new Event();

                eventCount++;
                publishProgress(eventCount, eventTotal);

                long no = vn.parseLong(vn.getAttrVal("No"));
                event.setNo(no);

                String code = vn.toString(vn.getAttrVal("Code"));
                event.setCode(code);

                String countryCode = vn.toString(vn.getAttrVal("CountryCode"));
                event.setCountryCode(countryCode);

                String name = vn.toString(vn.getAttrVal("Name"));
                event.setName(name);

                String startDateTimeStr = vn.toString(vn.getAttrVal("StartDate"));
                DateTime startDateTime = DateTime.parse(startDateTimeStr, eventDateFormatter);
                event.setStartDateTime(startDateTime);

                String endDateTimeStr = vn.toString(vn.getAttrVal("EndDate"));
                DateTime endDateTime = DateTime.parse(endDateTimeStr, eventDateFormatter);
                event.setEndDateTime(endDateTime);

                String content = vn.toString(vn.getAttrVal("Content"));
                processTournamentData(event, content, tourneyNos);

                eventList.add(event);
            }
        } catch (com.ximpleware.ParseException e) {
            e.printStackTrace();
        } catch (XPathParseException e) {
            e.printStackTrace();
        } catch (XPathEvalException e) {
            e.printStackTrace();
        } catch (NavException e) {
            e.printStackTrace();
        }

        processAdditionalTournamentData(eventList, tourneyNos);

        return eventList;
    }

    private void processTournamentData(Event pEvent, String pXml, Set<Long> pTourneyNos) {
        try {
            VTDGen vg = new VTDGen();
            vg.setDoc(pXml.getBytes());
            vg.parse(true);

            VTDNav vn = vg.getNav();
            AutoPilot ap = new AutoPilot(vn);

            ap.selectXPath("/Event/BeachTournament");

            int i = -1;
            while ((i = ap.evalXPath()) != -1) {
                String gender = vn.toString(vn.getAttrVal("Gender"));

                long no = vn.parseLong(vn.getAttrVal("No"));

                pTourneyNos.add(no);

                if ("W".equalsIgnoreCase(gender)) {
                    pEvent.setWomenTournamentNo(no);
                } else {
                    pEvent.setMenTournamentNo(no);
                }
            }
        } catch (com.ximpleware.ParseException e) {
            e.printStackTrace();
        } catch (XPathParseException e) {
            e.printStackTrace();
        } catch (XPathEvalException e) {
            e.printStackTrace();
        } catch (NavException e) {
            e.printStackTrace();
        }
    }

    private void processAdditionalTournamentData(List<Event> pEventList, Set<Long> pTourneyNos) {
        if (pTourneyNos.size() > 0) {
            StringBuilder tourneyReqs = new StringBuilder();
            Map<String, String> reqVals = new HashMap<>();
            reqVals.put("Type", "GetBeachTournament");
            reqVals.put("Fields", "NoEvent Name Title Status Type");
            for (long tourneyNo : pTourneyNos) {
                reqVals.put("No", String.valueOf(tourneyNo));
                tourneyReqs.append(FivbUtils.getSingleRequestString(reqVals, null));
            }
            String reqBody = FivbUtils.getRequestString(tourneyReqs.toString());
            String response = ServiceUtils.getPostResponseString(FivbUtils.getRequestBaseUrl(), "Request", reqBody);

            Map<Long, Event> tourneyData = new HashMap<>();
            try {
                VTDGen vg = new VTDGen();
                vg.setDoc(response.getBytes());
                vg.parse(true);

                VTDNav vn = vg.getNav();
                AutoPilot ap = new AutoPilot(vn);

                ap.selectXPath("/Responses/BeachTournament");

                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    Event event = new Event();

                    long noEvent = vn.parseLong(vn.getAttrVal("NoEvent"));

                    int type =  vn.parseInt(vn.getAttrVal("Type"));
                    setTournamentValue(event, type);

                    int status = vn.parseInt(vn.getAttrVal("Status"));

                    String name = vn.toString(vn.getAttrVal("Name"));
                    event.setName(name);

                    String title = vn.toString(vn.getAttrVal("Title"));
                    event.setTitle(title);

                    if (isEventQualified(event, type, status)) {
                        processNameAndTitle(event);
                        tourneyData.put(noEvent, event);
                    }
                }
            } catch (com.ximpleware.ParseException e) {
                e.printStackTrace();
            } catch (XPathParseException e) {
                e.printStackTrace();
            } catch (XPathEvalException e) {
                e.printStackTrace();
            } catch (NavException e) {
                e.printStackTrace();
            }

            Map<String, DateTimeZone> timeZones = PreferencesUtils.getTimeZones(context);
            boolean hasNewTimeZone = false;

            for (int i = pEventList.size() - 1; i >= 0; i--) {
                Event mainEvent = pEventList.get(i);
                Event tourneyEvent = tourneyData.get(mainEvent.getNo());
                if (tourneyEvent != null) {
                    mainEvent.setName(tourneyEvent.getName());
                    mainEvent.setLocation(tourneyEvent.getName());
                    String timeZoneKey = mainEvent.getCountryCode() + "|" + mainEvent.getLocation();
                    DateTimeZone timeZone = timeZones.get(timeZoneKey);
                    if (timeZone == null) {
                        timeZone = GeoUtils.getTimeZoneForCityAndCountryCode(mainEvent.getLocation(), mainEvent.getCountryCode());
                        if (timeZone != null) {
                            hasNewTimeZone = true;
                            timeZones.put(timeZoneKey, timeZone);
                        }
                    }
                    mainEvent.setTimeZone(timeZone);

                    mainEvent.setTitle(tourneyEvent.getTitle());
                    mainEvent.setValue(tourneyEvent.getValue());
                    pEventList.set(i, mainEvent);
                } else {
                    pEventList.remove(i);
                }
            }

            if (hasNewTimeZone) {
                PreferencesUtils.setTimeZones(timeZones, context);
            }
        }
    }

    private void setTournamentValue(Event pEvent, int pType) {
        if (pType >= 38 && pType <= 42) {
            int value = -1 * (pType - 42) + 1;
            pEvent.setValue(value);
        }
    }

    private boolean isEventQualified(Event pEvent, int pType, int pStatus) {
        if (pType == 35) {
            return false;
        }
        if (pStatus != 1 && pStatus != 6 && pStatus != 7 && pStatus != 8 && pStatus != 9) {
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
    protected void onProgressUpdate(Integer... values) {
        delegate.processEventProgress(values[0], values[1]);
    }

    @Override
    protected void onPostExecute(List<Event> pEventList) {
        delegate.processEventList(pEventList);
    }

    private String getBodyContent() {
        Map<String, String> reqVals = new HashMap<>();
        reqVals.put("Type", "GetEventList");
        reqVals.put("Fields", "No Code CountryCode Name StartDate EndDate Content");
        Map<String, String> filtVals = new HashMap<>();
        filtVals.put("IsVisManaged", "true");
        filtVals.put("HasBeachTournament", "true");
        int year = Calendar.getInstance().get(Calendar.YEAR);
        filtVals.put("FirstDate", String.valueOf(year) + "-01-01");
        filtVals.put("LastDate", String.valueOf(year) + "-12-31");

        String requestBody = FivbUtils.getRequestString(FivbUtils.getSingleRequestString(reqVals, filtVals));
        return requestBody;
    }
}
