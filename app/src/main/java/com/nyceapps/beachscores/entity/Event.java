package com.nyceapps.beachscores.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by lugosi on 21.05.17.
 */

public class Event implements Parcelable {
    private int type;
    private int status;
    private long no;
    private String code;
    private String name;
    private String title;
    private Date startDate;
    private Date endDate;
    private long womenTournamentNo = -1;
    private long menTournamentNo = -1;

    public Event() {
    }

    public int getType() {
        return type;
    }

    public void setType(int pType) {
        type = pType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int pStatus) {
        status = pStatus;
    }

    public long getNo() {
        return no;
    }

    public void setNo(long pNo) {
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

    public long getWomenTournamentNo() {
        return womenTournamentNo;
    }

    public void setWomenTournamentNo(long pWomenTournamentNo) {
        womenTournamentNo = pWomenTournamentNo;
    }

    public boolean hasWomenTournament() {
        return (womenTournamentNo > -1);
    }

    public long getMenTournamentNo() {
        return menTournamentNo;
    }

    public void setMenTournamentNo(long pMenTournamentNo) {
        menTournamentNo = pMenTournamentNo;
    }

    public boolean hasMenTournament() {
        return (menTournamentNo > -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(type);
        out.writeInt(status);
        out.writeLong(no);
        out.writeString(code);
        out.writeString(name);
        out.writeString(title);
        out.writeLong(startDate.getTime());
        out.writeLong(endDate.getTime());
        out.writeLong(womenTournamentNo);
        out.writeLong(menTournamentNo);
    }

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public Event(Parcel in) {
        type = in.readInt();
        status = in.readInt();
        no = in.readLong();
        code = in.readString();
        name = in.readString();
        title = in.readString();
        startDate = new Date(in.readLong());
        endDate = new Date(in.readLong());
        womenTournamentNo = in.readLong();
        menTournamentNo = in.readLong();
    }
}
