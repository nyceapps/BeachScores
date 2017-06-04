package com.nyceapps.beachscores.provider;

import com.nyceapps.beachscores.entity.Event;
import com.nyceapps.beachscores.entity.Match;
import com.nyceapps.beachscores.entity.MatchMap;

import java.util.List;

/**
 * Created by lugosi on 21.05.17.
 */

public interface MatchListResponse {
    void processMatchProgress(int pMatchCount, int pMatchTotal);

    void processMatchList(MatchMap pMatchMap);
}
