package com.nyceapps.beachscores.provider;

import com.nyceapps.beachscores.entity.Event;

import java.util.List;

/**
 * Created by lugosi on 21.05.17.
 */

public interface EventListResponse {
    void processEventProgress(int pEventCount, int pEventTotal);

    void processEventList(List<Event> pEventList);
}
