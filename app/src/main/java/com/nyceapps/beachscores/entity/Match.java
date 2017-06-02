package com.nyceapps.beachscores.entity;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by lugosi on 21.05.17.
 */

public class Match implements Parcelable {
    private long no;
    private long tournamentNo;
    private int noInTournament;
    private String roundName;
    private int roundPhase = -1;
    private int status;
    private Date localDate;
    private Date myDate;
    private long noTeamA = -1;
    private String teamAName;
    private String teamAFederationCode;
    private Drawable teamAFederationFlag;
    private long noTeamB = -1;
    private String teamBName;
    private String teamBFederationCode;
    private Drawable teamBFederationFlag;
    private int court = -1;
    private int pointsTeamASet1 = -1;
    private int pointsTeamBSet1 = -1;
    private int pointsTeamASet2 = -1;
    private int pointsTeamBSet2 = -1;
    private int pointsTeamASet3 = -1;
    private int pointsTeamBSet3 = -1;
    private int durationSet1 = -1;
    private int durationSet2 = -1;
    private int durationSet3 = -1;

    public Match() {
    }

    public long getNo() {
        return no;
    }

    public void setNo(long pNo) {
        no = pNo;
    }

    public long getTournamentNo() {
        return tournamentNo;
    }

    public void setTournamentNo(long pTournamentNo) {
        tournamentNo = pTournamentNo;
    }

    public int getNoInTournament() {
        return noInTournament;
    }

    public void setNoInTournament(int pNoInTournament) {
        noInTournament = pNoInTournament;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String pRoundName) {
        roundName = pRoundName;
    }

    public int getRoundPhase() {
        return roundPhase;
    }

    public void setRoundPhase(int pRoundPhase) {
        roundPhase = pRoundPhase;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int pStatus) {
        status = pStatus;
    }

    public Date getLocalDate() {
        return localDate;
    }

    public void setLocalDate(Date pLocalDate) {
        localDate = pLocalDate;
    }

    public Date getMyDate() {
        return myDate;
    }

    public void setMyDate(Date pMyDate) {
        myDate = pMyDate;
    }

    public long getNoTeamA() {
        return noTeamA;
    }

    public void setNoTeamA(long pNoTeamA) {
        noTeamA = pNoTeamA;
    }

    public String getTeamAName() {
        return teamAName;
    }

    public void setTeamAName(String pTeamAName) {
        teamAName = pTeamAName;
    }

    public String getTeamAFederationCode() {
        return teamAFederationCode;
    }

    public void setTeamAFederationCode(String pTeamAFederationCode) {
        teamAFederationCode = pTeamAFederationCode;
    }

    public Drawable getTeamAFederationFlag() {
        return teamAFederationFlag;
    }

    public void setTeamAFederationFlag(Drawable pTeamAFederationFlag) {
        teamAFederationFlag = pTeamAFederationFlag;
    }

    public long getNoTeamB() {
        return noTeamB;
    }

    public void setNoTeamB(long pNoTeamB) {
        noTeamB = pNoTeamB;
    }

    public String getTeamBName() {
        return teamBName;
    }

    public void setTeamBName(String pTeamBName) {
        teamBName = pTeamBName;
    }

    public String getTeamBFederationCode() {
        return teamBFederationCode;
    }

    public void setTeamBFederationCode(String pTeamBFederationCode) {
        teamBFederationCode = pTeamBFederationCode;
    }

    public Drawable getTeamBFederationFlag() {
        return teamBFederationFlag;
    }

    public void setTeamBFederationFlag(Drawable pTeamBFederationFlag) {
        teamBFederationFlag = pTeamBFederationFlag;
    }

    public int getCourt() {
        return court;
    }

    public void setCourt(int pCourt) {
        court = pCourt;
    }

    public int getPointsTeamASet1() {
        return pointsTeamASet1;
    }

    public void setPointsTeamASet1(int pPointsTeamASet1) {
        pointsTeamASet1 = pPointsTeamASet1;
    }

    public int getPointsTeamBSet1() {
        return pointsTeamBSet1;
    }

    public void setPointsTeamBSet1(int pPointsTeamBSet1) {
        pointsTeamBSet1 = pPointsTeamBSet1;
    }

    public int getPointsTeamASet2() {
        return pointsTeamASet2;
    }

    public void setPointsTeamASet2(int pPointsTeamASet2) {
        pointsTeamASet2 = pPointsTeamASet2;
    }

    public int getPointsTeamBSet2() {
        return pointsTeamBSet2;
    }

    public void setPointsTeamBSet2(int pPointsTeamBSet2) {
        pointsTeamBSet2 = pPointsTeamBSet2;
    }

    public int getPointsTeamASet3() {
        return pointsTeamASet3;
    }

    public void setPointsTeamASet3(int pPointsTeamASet3) {
        pointsTeamASet3 = pPointsTeamASet3;
    }

    public int getPointsTeamBSet3() {
        return pointsTeamBSet3;
    }

    public void setPointsTeamBSet3(int pPointsTeamBSet3) {
        pointsTeamBSet3 = pPointsTeamBSet3;
    }

    public int getDurationSet1() {
        return durationSet1;
    }

    public void setDurationSet1(int pDurationSet1) {
        durationSet1 = pDurationSet1;
    }

    public int getDurationSet2() {
        return durationSet2;
    }

    public void setDurationSet2(int pDurationSet2) {
        durationSet2 = pDurationSet2;
    }

    public int getDurationSet3() {
        return durationSet3;
    }

    public void setDurationSet3(int pDurationSet3) {
        durationSet3 = pDurationSet3;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(no);
        out.writeLong(tournamentNo);
        out.writeInt(noInTournament);
        out.writeString(roundName);
        out.writeInt(roundPhase);
        out.writeInt(status);
        out.writeLong(localDate.getTime());
        out.writeString(teamAName);
        out.writeString(teamBName);
        out.writeInt(court);
        out.writeInt(pointsTeamASet1);
        out.writeInt(pointsTeamBSet1);
        out.writeInt(pointsTeamASet2);
        out.writeInt(pointsTeamBSet2);
        out.writeInt(pointsTeamASet3);
        out.writeInt(pointsTeamBSet3);
        out.writeInt(durationSet1);
        out.writeInt(durationSet2);
        out.writeInt(durationSet3);
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
        no = in.readLong();
        tournamentNo = in.readLong();
        noInTournament = in.readInt();
        roundName = in.readString();
        roundPhase = in.readInt();
        status = in.readInt();
        localDate = new Date(in.readLong());
        teamAName = in.readString();
        teamBName = in.readString();
        court = in.readInt();
        pointsTeamASet1 = in.readInt();
        pointsTeamBSet1 = in.readInt();
        pointsTeamASet2 = in.readInt();
        pointsTeamBSet2 = in.readInt();
        pointsTeamASet3 = in.readInt();
        pointsTeamBSet3 = in.readInt();
        durationSet1 = in.readInt();
        durationSet2 = in.readInt();
        durationSet3 = in.readInt();
    }
}
