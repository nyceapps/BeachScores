package com.nyceapps.beachscores.util;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lugosi on 20.05.17.
 */

public class FivbXmlUtils {
    private FivbXmlUtils() {
    }

    public static String getRequestBaseUrl() {
        return "http://www.fivb.org/Vis2009/XmlRequest.asmx";
    }

    public static String getSingleRequestString(Map<String, String> pRequestValues, Map<String, String> pFilterValues) {
        StringBuilder reqStr = new StringBuilder();

        reqStr.append("<Request");
        for (Map.Entry<String, String> entry : pRequestValues.entrySet()) {
            reqStr.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
        }
        if (pFilterValues != null && pFilterValues.size() > 0) {
            reqStr.append(">");
            reqStr.append("<Filter");
            for (Map.Entry<String, String> entry : pFilterValues.entrySet()) {
                reqStr.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
            }
            reqStr.append("/>");
            reqStr.append("</Request>");
        } else {
            reqStr.append("/>");
        }

        return reqStr.toString();
    }

    public static String getRequestString(String pSingleRequest) {
        List<String> singleReqs = new ArrayList<>();
        singleReqs.add(pSingleRequest);
        return getRequestString(singleReqs);
    }

    public static String getRequestString(List<String> pSingleRequests) {
        StringBuilder reqStr = new StringBuilder();

        reqStr.append("<Requests>");
        for (String singleReq : pSingleRequests) {
            reqStr.append(singleReq);
        }
        reqStr.append("</Requests>");

        return reqStr.toString();
    }
}
