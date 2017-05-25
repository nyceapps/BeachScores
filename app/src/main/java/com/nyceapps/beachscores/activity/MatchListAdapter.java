package com.nyceapps.beachscores.activity;

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
import java.util.Locale;
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_header, parent, false);
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

            holder.teamAView.setText(match.getTeamAName());
            holder.teamBView.setText(match.getTeamBName());

            int pointsTeamASet1 = match.getPointsTeamASet1();
            int pointsTeamASet2 = match.getPointsTeamASet2();
            int pointsTeamASet3 = match.getPointsTeamASet3();
            int pointsTeamBSet1 = match.getPointsTeamBSet1();
            int pointsTeamBSet2 = match.getPointsTeamBSet2();
            int pointsTeamBSet3 = match.getPointsTeamBSet3();

            int teamASets = 0;
            int teamBSets = 0;
            if (Math.abs(pointsTeamASet1 - pointsTeamBSet1) >= 2) {
                if (pointsTeamASet1 >= 21 && pointsTeamASet1 > pointsTeamBSet1) {
                    teamASets++;
                } else if (pointsTeamBSet1 >= 21 && pointsTeamBSet1 > pointsTeamASet1) {
                    teamBSets++;
                }
            }
            if (Math.abs(pointsTeamASet2 - pointsTeamBSet2) >= 2) {
                if (pointsTeamASet2 >= 21 && pointsTeamASet2 > pointsTeamBSet2) {
                    teamASets++;
                } else if (pointsTeamBSet2 >= 21 && pointsTeamBSet2 > pointsTeamASet2) {
                    teamBSets++;
                }
            }
            if (Math.abs(pointsTeamASet3 - pointsTeamBSet3) >= 2) {
                if (pointsTeamASet3 >= 15 && pointsTeamASet3 > pointsTeamBSet3) {
                    teamASets++;
                } else if (pointsTeamBSet3 >= 15 && pointsTeamBSet3 > pointsTeamASet3) {
                    teamBSets++;
                }
            }

            holder.teamASetsView.setText(String.valueOf(teamASets));
            holder.teamBSetsView.setText(String.valueOf(teamBSets));

            StringBuilder eventInfo = new StringBuilder();
            eventInfo.append("Game #").append(match.getNoInTournament()).append(" | ");
            String localDayStr = getLocalDayString(match);
            eventInfo.append(localDayStr).append(" ");
            String localTimeStr = getLocalTimeString(match);
            eventInfo.append(localTimeStr);
            int court = match.getCourt();
            if (court > -1) {
                eventInfo.append(" | ").append("Court ").append(court);
            }
            holder.infoView.setText(eventInfo.toString());

            holder.itemView.setTag(match);
        }
    }

    private String getLocalDayString(Match match) {
        DateFormat dfDay = new SimpleDateFormat("EEEE");
        return dfDay.format(match.getLocalDate());
    }

    private String getLocalTimeString(Match match) {
        DateFormat dfTime = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        return dfTime.format(match.getLocalDate());
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
        public TextView teamAView;
        public TextView teamBView;
        public TextView teamASetsView;
        public TextView teamBSetsView;
        public TextView infoView;

        public ItemViewHolder(View v) {
            super(v);
            teamAView = (TextView) v.findViewById(R.id.match_team_a);
            teamBView = (TextView) v.findViewById(R.id.match_team_b);
            teamASetsView = (TextView) v.findViewById(R.id.match_team_a_sets);
            teamBSetsView = (TextView) v.findViewById(R.id.match_team_b_sets);
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
