package com.nyceapps.beachscores;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nyceapps.beachscores.entity.Event;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by lugosi on 22.05.17.
 */

class EventsListAdapter extends RecyclerView.Adapter<EventsListAdapter.ViewHolder> {
    List<Event> eventList;

    public EventsListAdapter(List<Event> pEventList) {
        eventList = pEventList;
    }

    @Override
    public EventsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.events_list_item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EventsListAdapter.ViewHolder holder, int position) {
        Event event = eventList.get(position);

        String eventName = event.getName();
        holder.nameTextView.setText(eventName);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String startStr = df.format(event.getStart());
        String endStr = df.format(event.getEnd());
        String eventPeriod = startStr + " - " + endStr;
        holder.periodTextView.setText(eventPeriod);
    }

    public void updateList(List<Event> pEventList) {
        if (pEventList.size() != eventList.size() || !eventList.containsAll(pEventList)) {
            eventList = pEventList;
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView nameTextView;
        public TextView periodTextView;

        public ViewHolder(View v) {
            super(v);
            nameTextView = (TextView) v.findViewById(R.id.event_name);
            periodTextView = (TextView) v.findViewById(R.id.event_period);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            /*
            ChoreItem chore = (ChoreItem) imageImageView.getTag();
            callingActivity.editChore(chore);
            */
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
