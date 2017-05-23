package com.nyceapps.beachscores.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by lugosi on 21.05.17.
 */

public class Match implements Parcelable {
    private String teamAName;
    private String teamBName;

    public Match() {
    }

    public String getTeamAName() {
        return teamAName;
    }

    public void setTeamAName(String pTeamAName) {
        teamAName = pTeamAName;
    }

    public String getTeamBName() {
        return teamBName;
    }

    public void setTeamBName(String pTeamBName) {
        teamBName = pTeamBName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(teamAName);
        out.writeString(teamBName);
    }

    public static final Creator<Match> CREATOR = new Creator<Match>() {
        public Match createFromParcel(Parcel in) {
            return new Match(in);
        }

        public Match[] newArray(int size) {
            return new Match[size];
        }
    };

    public Match(Parcel in) {
        teamAName = in.readString();
        teamBName = in.readString();
    }
}
