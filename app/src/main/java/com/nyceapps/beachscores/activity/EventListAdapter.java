package com.nyceapps.beachscores.activity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nyceapps.beachscores.R;
import com.nyceapps.beachscores.entity.Event;
import com.truizlop.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lugosi on 22.05.17.
 */

class EventListAdapter extends SectionedRecyclerViewAdapter<EventListAdapter.HeaderViewHolder, EventListAdapter.ItemViewHolder, EventListAdapter.FooterViewHolder> {
    private List<Event> eventList;
    private ActivityDelegate delegate;
    private List<String> eventSections;
    private Map<String, List<Event>> eventItems;

    public EventListAdapter(List<Event> pEventList, ActivityDelegate pDelegate) {
        eventList = pEventList;
        delegate = pDelegate;
    }

    @Override
    protected HeaderViewHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_header, parent, false);
        return new HeaderViewHolder(v);
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_row, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    protected boolean hasFooterInSection(int section) {
        return false;
    }

    @Override
    protected FooterViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected void onBindSectionHeaderViewHolder(HeaderViewHolder holder, int section) {
        String headerTitle = "";
        if (eventSections != null) {
            headerTitle =  eventSections.get(section);
        }

        holder.titleView.setText(headerTitle);
    }

    @Override
    public void onBindItemViewHolder(ItemViewHolder holder, int section, int position) {
        String sectionKey = eventSections.get(section);
        List<Event> eventData = eventItems.get(sectionKey);
        if (eventData != null) {
            Event event = eventData.get(position);

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
    }

    @Override
    protected void onBindSectionFooterViewHolder(FooterViewHolder holder, int section) {
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView titleView;

        public HeaderViewHolder(View v) {
            super(v);
            titleView = (TextView) v.findViewById(R.id.header_title);
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView titleView;
        public TextView infoView;
        public TextView locationTextView;

        public ItemViewHolder(View v) {
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

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View v) {
            super(v);
        }
    }

    public void updateList(List<Event> pEventList) {
        if (pEventList.size() != eventList.size() || !eventList.containsAll(pEventList)) {
            prepareSectionData(pEventList);
            eventList = pEventList;
            notifyDataSetChanged();
        }
    }

    private void prepareSectionData(List<Event> pEventList) {
        eventSections = new ArrayList<>();
        eventItems = new HashMap<>();

        for (Event event : pEventList) {
            Date startDate = event.getStartDate();
            String monthName = new SimpleDateFormat("MMMM").format(startDate);
            if (!eventSections.contains(monthName)) {
                eventSections.add(monthName);
                eventItems.put(monthName, new ArrayList<Event>());
            }

            List<Event> eventData = eventItems.get(monthName);
            eventData.add(event);
            eventItems.put(monthName, eventData);
        }
    }

    @Override
    protected int getSectionCount() {
        if (eventSections == null) {
            return 0;
        }
        return eventSections.size();
    }

    @Override
    protected int getItemCountForSection(int section) {
        if (eventSections == null) {
            return 0;
        }
        String sectionKey = eventSections.get(section);
        List<Event> eventData = eventItems.get(sectionKey);
        if (eventData == null) {
            return 0;
        }
        return eventData.size();
    }
}
