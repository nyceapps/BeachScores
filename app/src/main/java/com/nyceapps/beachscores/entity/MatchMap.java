package com.nyceapps.beachscores.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lugosi on 23.05.17.
 */

public class MatchMap {
    private Map<Integer, Map<Integer, List<Match>>> matchMap = new HashMap<>();
    private List<Integer> genderList = new ArrayList<>();
    private Map<Integer, String> genderMap = new HashMap<>();
    private List<Integer> roundList = new ArrayList<>();
    private Map<Integer, String> roundMap = new HashMap<>();

    public void put(int pGender, int pRound, Match pMatch) {
        Map<Integer, List<Match>> gMap = matchMap.get(pGender);
        if (gMap == null) {
            gMap = new HashMap<>();
        }
        List<Match> pList = gMap.get(pRound);
        if (pList == null) {
            pList = new ArrayList<>();
        }
        pList.add(pMatch);

        gMap.put(pRound, pList);
        matchMap.put(pGender, gMap);
    }

    public List<Match> getList(int pGender, int pRound) {
        Map<Integer, List<Match>> gMap = matchMap.get(pGender);
        if (gMap == null) {
            return new ArrayList<>();
        }
        List<Match> pList = gMap.get(pRound);
        if (pList == null) {
            return new ArrayList<>();
        }
        return pList;
    }

    public void sort() {
        for (Map.Entry<Integer, Map<Integer, List<Match>>> mainMap : matchMap.entrySet()) {
            Integer currGender = mainMap.getKey();
            Map<Integer, List<Match>> gMap = mainMap.getValue();
            for (Map.Entry<Integer, List<Match>> phaseMap : gMap.entrySet()) {
                Integer currPhase = phaseMap.getKey();
                List<Match> pList = phaseMap.getValue();
                Collections.sort(pList, new Comparator<Match>() {
                    @Override
                    public int compare(Match m0, Match m1) {
                        if (m0.getNoInTournament() < m1.getNoInTournament()) {
                            return 1;
                        } else if (m0.getNoInTournament() > m1.getNoInTournament()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
                gMap.put(currPhase, pList);
                matchMap.put(currGender, gMap);
            }
        }
    }

    public void addGender(int pKey, String pName) {
        genderList.add(pKey);
        genderMap.put(pKey, pName);
    }

    public List<Integer> getGenderList() {
        return genderList;
    }

    public String getGenderName(int pGender) {
        return genderMap.get(pGender);
    }

    public void addRound(int pKey, String pName) {
        roundList.add(pKey);
        roundMap.put(pKey, pName);
    }

    public List<Integer> getRoundList() {
        return roundList;
    }

    public String getRoundName(int pRound) {
        return roundMap.get(pRound);
    }
}
