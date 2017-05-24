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
    private Map<Integer, Map<Integer, List<Match>>> matchMap;

    public MatchMap() {
        matchMap = new HashMap<>();
    }

    public void put(int pGender, int pPhase, Match pMatch) {
        Map<Integer, List<Match>> genderMap = matchMap.get(pGender);
        if (genderMap == null) {
            genderMap = new HashMap<>();
        }
        List<Match> phaseList = genderMap.get(pPhase);
        if (phaseList == null) {
            phaseList = new ArrayList<>();
        }
        phaseList.add(pMatch);

        genderMap.put(pPhase, phaseList);
        matchMap.put(pGender, genderMap);
    }

    public List<Match> getList(int pGender, int pPhase) {
        Map<Integer, List<Match>> genderMap = matchMap.get(pGender);
        if (genderMap == null) {
            return new ArrayList<>();
        }
        List<Match> phaseList = genderMap.get(pPhase);
        if (phaseList == null) {
            return new ArrayList<>();
        }
        return phaseList;
    }

    public void sort() {
        for (Map.Entry<Integer, Map<Integer, List<Match>>> mainMap : matchMap.entrySet()) {
            Integer currGender = mainMap.getKey();
            Map<Integer, List<Match>> genderMap = mainMap.getValue();
            for (Map.Entry<Integer, List<Match>> phaseMap : genderMap.entrySet()) {
                Integer currPhase = phaseMap.getKey();
                List<Match> phaseList = phaseMap.getValue();
                Collections.sort(phaseList, new Comparator<Match>() {
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
                genderMap.put(currPhase, phaseList);
                matchMap.put(currGender, genderMap);
            }
        }
    }
}
