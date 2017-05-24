package com.nyceapps.beachscores.activity;

import android.content.ClipData;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nyceapps.beachscores.R;
import com.nyceapps.beachscores.entity.Match;
import com.truizlop.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lugosi on 22.05.17.
 */

class MatchListAdapter extends SectionedRecyclerViewAdapter<MatchListAdapter.HeaderViewHolder, MatchListAdapter.ItemViewHolder, MatchListAdapter.FooterViewHolder> {
    private List<Match> matchList;
    private ActivityDelegate delegate;
    private List<String> matchSections;
    private Map<String, List<Match>> matchItems;

    public MatchListAdapter(List<Match> pMatchList, ActivityDelegate pDelegate) {
        matchList = pMatchList;
        delegate = pDelegate;
    }

    @Override
    protected HeaderViewHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_list_header, parent, false);
        return new HeaderViewHolder(v);
    }

    @Override
    protected ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_list_row, parent, false);
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
        if (matchSections != null) {
            headerTitle =  matchSections.get(section);
        }

        holder.titleView.setText(headerTitle);
    }

    @Override
    protected void onBindItemViewHolder(ItemViewHolder holder, int section, int position) {
        String sectionKey = matchSections.get(section);
        List<Match> matchData = matchItems.get(sectionKey);
        if (matchData != null) {
            Match match = matchData.get(position);

            String matchName = match.getTeamAName() + " vs. " + match.getTeamBName();
            holder.titleView.setText(matchName);

            StringBuilder eventInfo = new StringBuilder();
            eventInfo.append("#").append(match.getNoInTournament()).append(", ");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String localDateStr = df.format(match.getLocalDate());
            eventInfo.append(localDateStr);
            int court = match.getCourt();
            if (court > -1) {
                eventInfo.append(", Court ").append(court);
            }
            eventInfo.append(", ").append(match.getRoundName());
            holder.infoView.setText(eventInfo.toString());

            holder.itemView.setTag(match);
        }
    }

    @Override
    protected void onBindSectionFooterViewHolder(FooterViewHolder holder, int section) {
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView titleView;

        public HeaderViewHolder(View v) {
            super(v);
            titleView = (TextView) v.findViewById(R.id.match_header_title);
        }

    }
    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView titleView;
        public TextView infoView;

        public ItemViewHolder(View v) {
            super(v);
            titleView = (TextView) v.findViewById(R.id.match_title);
            infoView = (TextView) v.findViewById(R.id.match_info);
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

    public void updateList(List<Match> pMatchList) {
        if (pMatchList.size() != matchList.size() || !matchList.containsAll(pMatchList)) {
            prepareSectionData(pMatchList);
            matchList = pMatchList;
            notifyDataSetChanged();
        }
    }

    private void prepareSectionData(List<Match> pMatchList) {
        matchSections = new ArrayList<>();
        matchItems = new HashMap<>();

        for (Match match : pMatchList) {
            String roundName = match.getRoundName();
            if (!matchSections.contains(roundName)) {
                matchSections.add(roundName);
                matchItems.put(roundName, new ArrayList<Match>());
            }

            List<Match> matchData = matchItems.get(roundName);
            matchData.add(match);
            matchItems.put(roundName, matchData);
        }
    }

    @Override
    protected int getSectionCount() {
        if (matchSections == null) {
            return 0;
        }
        return matchSections.size();
    }

    @Override
    protected int getItemCountForSection(int section) {
        if (matchSections == null) {
            return 0;
        }
        String sectionKey = matchSections.get(section);
        List<Match> matchData = matchItems.get(sectionKey);
        if (matchData == null) {
            return 0;
        }
        return matchData.size();
    }
}
