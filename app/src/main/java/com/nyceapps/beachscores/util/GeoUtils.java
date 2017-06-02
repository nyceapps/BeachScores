package com.nyceapps.beachscores.util;

import android.text.TextUtils;

import org.joda.time.DateTimeZone;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.TimeZone;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by lugosi on 02.06.17.
 */

public class GeoUtils {
    private final static String BASE_URL_LAT_LNG = "https://maps.googleapis.com/maps/api/geocode/xml?address=%s&components=country:%s&sensor=false";
    private final static String BASE_URL_TIME_ZONE = "https://maps.googleapis.com/maps/api/timezone/xml?location=%s,%s&timestamp=1458000000";

    private GeoUtils() {
    }

    public static DateTimeZone getTimeZoneForCityAndCountryCode(String pCity, String pCountryCode) {
        if (!TextUtils.isEmpty(pCity) && !TextUtils.isEmpty(pCountryCode)) {
            String latLngUrl = String.format(BASE_URL_LAT_LNG, pCity, pCountryCode);
            String latLngResponse = ServiceUtils.getPostResponseString(latLngUrl, null, null);
            if (!TextUtils.isEmpty(latLngResponse)) {
                XPath latLngXpath = XPathFactory.newInstance().newXPath();
                String latExpr = "/GeocodeResponse[status = 'OK']/result/geometry/location/lat";
                String lngExpr = "/GeocodeResponse[status = 'OK']/result/geometry/location/lng";
                try {
                    InputSource latIS = new InputSource(new StringReader(latLngResponse));
                    String latStr = latLngXpath.evaluate(latExpr, latIS);
                    InputSource lngIS = new InputSource(new StringReader(latLngResponse));
                    String lngStr = latLngXpath.evaluate(lngExpr, lngIS);
                    if (!TextUtils.isEmpty(latStr) && !TextUtils.isEmpty(lngStr)) {
                        String timeZoneUrl = String.format(BASE_URL_TIME_ZONE, latStr, lngStr);
                        String timeZoneResponse = ServiceUtils.getPostResponseString(timeZoneUrl, null, null);
                        if (!TextUtils.isEmpty(timeZoneResponse)) {
                            XPath timeZoneXpath = XPathFactory.newInstance().newXPath();
                            String timeZoneExpr = "/TimeZoneResponse[status = 'OK']/time_zone_id";
                            InputSource timezoneIS = new InputSource(new StringReader(timeZoneResponse));
                            String timeZoneId = timeZoneXpath.evaluate(timeZoneExpr, timezoneIS);
                            if (!TextUtils.isEmpty(timeZoneId)) {
                                return DateTimeZone.forID(timeZoneId);
                            }
                        }
                    }
                } catch (XPathExpressionException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
