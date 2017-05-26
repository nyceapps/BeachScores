package com.nyceapps.beachscores.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nyceapps.beachscores.R;
import com.nyceapps.beachscores.entity.Event;
import com.truizlop.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by lugosi on 22.05.17.
 */

class EventListAdapter extends SectionedRecyclerViewAdapter<EventListAdapter.HeaderViewHolder, EventListAdapter.ItemViewHolder, EventListAdapter.FooterViewHolder> {
    private List<Event> eventList;
    private ActivityDelegate delegate;
    private final Context context;
    private List<String> eventSections;
    private Map<String, List<Event>> eventItems;

    public EventListAdapter(List<Event> pEventList, ActivityDelegate pDelegate, Context pContext) {
        eventList = pEventList;
        delegate = pDelegate;
        context = pContext;
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
            //Log.i("MAIN", event.getTitle() + "[" + event.getName() + "] = " + event.getValue());

            Date now = new Date();

            int textColor = ContextCompat.getColor(context, R.color.colorDark);
            if (event.getEndDate().before(now)) {
                textColor = ContextCompat.getColor(context, R.color.colorLighter);
            } else if (event.getStartDate().after(now)) {
                textColor = ContextCompat.getColor(context, R.color.colorDarker);
            }

            Drawable drawableMaleFemale = ContextCompat.getDrawable(context, R.drawable.gender_male_female_dark);
            Drawable drawableFemale = ContextCompat.getDrawable(context, R.drawable.gender_female_dark);
            Drawable drawableMale = ContextCompat.getDrawable(context, R.drawable.gender_male_dark);
            Drawable drawableValue = ContextCompat.getDrawable(context, R.drawable.star_dark);
            if (event.getEndDate().before(now)) {
                drawableMaleFemale = ContextCompat.getDrawable(context, R.drawable.gender_male_female_lighter);
                drawableFemale = ContextCompat.getDrawable(context, R.drawable.gender_female_lighter);
                drawableMale = ContextCompat.getDrawable(context, R.drawable.gender_male_lighter);
                drawableValue = ContextCompat.getDrawable(context, R.drawable.star_lighter);
            } else if (event.getStartDate().after(now)) {
                drawableMaleFemale = ContextCompat.getDrawable(context, R.drawable.gender_male_female_darker);
                drawableFemale = ContextCompat.getDrawable(context, R.drawable.gender_female_darker);
                drawableMale = ContextCompat.getDrawable(context, R.drawable.gender_male_darker);
                drawableValue = ContextCompat.getDrawable(context, R.drawable.star_darker);
            }

            String eventTitle = event.getTitle();
            holder.titleView.setText(eventTitle);
            holder.titleView.setTextColor(textColor);

            StringBuilder eventInfo = new StringBuilder();
            String fromToDateStr = getFromToDateString(event);
            eventInfo.append(fromToDateStr).append(" | ").append(event.getName());
            holder.infoView.setText(eventInfo.toString());
            holder.infoView.setTextColor(textColor);

            if (event.hasWomenTournament() && event.hasMenTournament()) {
                holder.genderView.setImageDrawable(drawableMaleFemale);
            } else if (event.hasWomenTournament()) {
                holder.genderView.setImageDrawable(drawableFemale);
            } else if (event.hasMenTournament()){
                holder.genderView.setImageDrawable(drawableMale);
            } else {
                holder.genderView.setImageResource(0);
            }

            int value = event.getValue();
            if (value > 0) {
                holder.valueTextView.setText(String.valueOf(value));
                holder.valueImageView.setImageDrawable(drawableValue);
            } else {
                holder.valueTextView.setText("");
                holder.valueImageView.setImageResource(0);
            }
            holder.valueTextView.setTextColor(textColor);

            holder.itemView.setTag(event);
        }
    }

    @NonNull
    private String getFromToDateString(Event event) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTime(event.getStartDate());
        int startMonth = cal.get(Calendar.MONTH);
        cal.setTime(event.getEndDate());
        int endMonth = cal.get(Calendar.MONTH);

        DateFormat dfStart = (startMonth == endMonth ? new SimpleDateFormat("dd") : new SimpleDateFormat("dd MMMM"));
        String startDateStr = dfStart.format(event.getStartDate());
        DateFormat dfEnd = new SimpleDateFormat("dd MMMM");
        String endDateStr = dfEnd.format(event.getEndDate());
        return startDateStr + " - " + endDateStr;
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
        public ImageView genderView;
        public TextView valueTextView;
        public ImageView valueImageView;

        public ItemViewHolder(View v) {
            super(v);
            titleView = (TextView) v.findViewById(R.id.event_title);
            infoView = (TextView) v.findViewById(R.id.event_info);
            genderView = (ImageView) v.findViewById(R.id.event_gender_image);
            valueTextView = (TextView) v.findViewById(R.id.event_value_text);
            valueImageView = (ImageView) v.findViewById(R.id.event_value_image);
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
