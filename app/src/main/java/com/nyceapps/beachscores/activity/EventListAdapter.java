package com.nyceapps.beachscores.activity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nyceapps.beachscores.R;
import com.nyceapps.beachscores.entity.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by lugosi on 22.05.17.
 */

class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder> {
    private List<Event> eventList;
    private ActivityDelegate delegate;

    public EventListAdapter(List<Event> pEventList, ActivityDelegate pDelegate) {
        eventList = pEventList;
        delegate = pDelegate;
    }

    @Override
    public EventListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EventListAdapter.ViewHolder holder, int position) {
        Event event = eventList.get(position);

        String eventTitle = event.getTitle();
        holder.titleView.setText(eventTitle);

        StringBuilder eventInfo = new StringBuilder();
        eventInfo.append(event.getName()).append(", ");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String startDateStr = df.format(event.getStartDate());
        String endDateStr = df.format(event.getEndDate());
        eventInfo.append(startDateStr + " - " + endDateStr).append(", ");
        if (event.hasWomenTournament()) {
            eventInfo.append("W");
            if (event.hasMenTournament()) {
                eventInfo.append("/");
            }
        }
        if (event.hasMenTournament()) {
            eventInfo.append("M");
        }
        holder.infoView.setText(eventInfo.toString());

        holder.itemView.setTag(event);
    }

    public void updateList(List<Event> pEventList) {
        if (pEventList.size() != eventList.size() || !eventList.containsAll(pEventList)) {
            eventList = pEventList;
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView titleView;
        public TextView infoView;
        public TextView locationTextView;

        public ViewHolder(View v) {
            super(v);
            titleView = (TextView) v.findViewById(R.id.event_title);
            infoView = (TextView) v.findViewById(R.id.event_info);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            delegate.onClick(v);
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
