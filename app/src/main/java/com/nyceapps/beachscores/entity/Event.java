package com.nyceapps.beachscores.entity;

import java.util.Date;
import java.util.Map;

/**
 * Created by lugosi on 21.05.17.
 */

public class Event {
    private String type;
    private String status;
    private String no;
    private String code;
    private String name;
    private String title;
    private Date startDate;
    private Date endDate;
    private Map<String, String> tournaments;

    public String getType() {
        return type;
    }

    public void setType(String pType) {
        type = pType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String pStatus) {
        status = pStatus;
    }

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String pTitle) {
        title = pTitle;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date pStartDate) {
        startDate = pStartDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date pEndDate) {
        endDate = pEndDate;
    }

    public Map<String, String> getTournaments() {
        return tournaments;
    }

    public void setTournaments(Map<String, String> pTournaments) {
        tournaments = pTournaments;
    }
}
