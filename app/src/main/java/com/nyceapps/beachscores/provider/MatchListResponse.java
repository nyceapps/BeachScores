package com.nyceapps.beachscores.provider;

import com.nyceapps.beachscores.entity.Event;
import com.nyceapps.beachscores.entity.Match;

import java.util.List;

/**
 * Created by lugosi on 21.05.17.
 */

public interface MatchListResponse {
    void processMatchList(List<Match> pMatchList);
}
