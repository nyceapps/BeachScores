package com.nyceapps.beachscores.entity;

import java.util.Date;
import java.util.Map;

/**
 * Created by lugosi on 21.05.17.
 */

public class Event {
    private String no;
    private String code;
    private String name;
    private Date start;
    private Date end;
    private Map<String, String> tournaments;

    public String getNo() {
        return no;
    }

    public void setNo(String pNo) {
        no = pNo;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String pCode) {
        code = pCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date pStart) {
        start = pStart;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date pEnd) {
        end = pEnd;
    }

    public Map<String, String> getTournaments() {
        return tournaments;
    }

    public void setTournaments(Map<String, String> pTournaments) {
        tournaments = pTournaments;
    }
}
