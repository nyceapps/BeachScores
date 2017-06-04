package com.nyceapps.beachscores.activity;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nyceapps.beachscores.R;
import com.nyceapps.beachscores.entity.Match;
import com.truizlop.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lugosi on 22.05.17.
 */

class MatchListAdapter extends SectionedRecyclerViewAdapter<MatchListAdapter.HeaderViewHolder, MatchListAdapter.ItemViewHolder, MatchListAdapter.FooterViewHolder> {
    private final static String TAG = MatchListAdapter.class.getSimpleName();

    private List<Match> matchList;
    private ActivityDelegate delegate;
    private final Context context;
    private List<String> matchSections;
    private Map<String, List<Match>> matchItems;
    private String timeDisplayType;

    public MatchListAdapter(List<Match> pMatchList, ActivityDelegate pDelegate, Context pContext) {
        matchList = pMatchList;
        delegate = pDelegate;
        context = pContext;
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

            int textColor = ContextCompat.getColor(context, R.color.colorDark);
            if (match.isFinished()) {
                textColor = ContextCompat.getColor(context, R.color.colorLighter);
            } else if (match.isScheduled()) {
                textColor = ContextCompat.getColor(context, R.color.colorDarker);
            }

            long noTeamA = match.getNoTeamA();
            long noTeamB = match.getNoTeamB();

            holder.teamAView.setText(match.getTeamAName());
            holder.teamAView.setCompoundDrawablesWithIntrinsicBounds(match.getTeamAFederationFlag(), null, null, null);
            holder.teamAView.setCompoundDrawablePadding(8);
            holder.teamAView.setTextColor(textColor);
            holder.teamBView.setText(match.getTeamBName());
            holder.teamBView.setCompoundDrawablesWithIntrinsicBounds(match.getTeamBFederationFlag(), null, null, null);
            holder.teamBView.setCompoundDrawablePadding(8);
            holder.teamBView.setTextColor(textColor);

            String teamASetsStr = "";
            String teamBSetsStr = "";
            String setPointsStr = "";

            if (!match.isBye()) {
                int pointsTeamASet1 = match.getPointsTeamASet1();
                int pointsTeamASet2 = match.getPointsTeamASet2();
                int pointsTeamASet3 = match.getPointsTeamASet3();
                int pointsTeamBSet1 = match.getPointsTeamBSet1();
                int pointsTeamBSet2 = match.getPointsTeamBSet2();
                int pointsTeamBSet3 = match.getPointsTeamBSet3();

                int teamASets = 0;
                int teamBSets = 0;

                StringBuilder setPointsSB = new StringBuilder();

                if (match.isSet1Finished() && pointsTeamASet1 > -1 && pointsTeamBSet1 > -1) {
                    if (pointsTeamASet1 > pointsTeamBSet1) {
                        teamASets++;
                    } else {
                        teamBSets++;
                    }
                    if (match.isSet2Finished() && pointsTeamASet2 > -1 && pointsTeamBSet2 > -1) {
                        if (pointsTeamASet2 > pointsTeamBSet2) {
                            teamASets++;
                        } else {
                            teamBSets++;
                        }
                        if (match.isSet3Finished() && pointsTeamASet3 > -1 && pointsTeamBSet3 > -1) {
                            if (pointsTeamASet3 > pointsTeamBSet3) {
                                teamASets++;
                            } else {
                                teamBSets++;
                            }
                        }
                    }
                }
                teamASetsStr = String.valueOf(teamASets);
                teamBSetsStr = String.valueOf(teamBSets);
                if ((match.isSet1Running() || match.isSet1Finished()) && pointsTeamASet1 > -1 && pointsTeamBSet1 > -1) {
                    setPointsSB.append(pointsTeamASet1).append("-").append(pointsTeamBSet1);
                    if ((match.isSet2Running() || match.isSet2Finished()) && pointsTeamASet2 > -1 && pointsTeamBSet2 > -1) {
                        setPointsSB.append("\n").append(pointsTeamASet2).append("-").append(pointsTeamBSet2);
                        if ((match.isSet3running() || match.isSet3Finished()) && pointsTeamASet3 > -1 && pointsTeamBSet3 > -1) {
                            setPointsSB.append("\n").append(pointsTeamASet3).append("-").append(pointsTeamBSet3);
                        }
                    }
                }
                setPointsStr = setPointsSB.toString();
            }

            holder.teamASetsView.setText(teamASetsStr);
            holder.teamASetsView.setTextColor(textColor);
            holder.teamBSetsView.setText(teamBSetsStr);
            holder.teamBSetsView.setTextColor(textColor);
            holder.setPointsView.setText(setPointsStr);
            holder.setPointsView.setTextColor(textColor);

            StringBuilder eventInfo = new StringBuilder();
            eventInfo.append("Game #").append(match.getNoInTournament()).append(" | ");
            DateTime gameDateTime = match.getLocalDateTime();
            if (MatchListActivity.TIME_DISPLAY_TYPE_MY.equals(timeDisplayType)) {
                gameDateTime = match.getMyDateTime();
            }
            String localDayStr = getGameDayString(gameDateTime);
            eventInfo.append(localDayStr).append(" ");
            String localTimeStr = getGameTimeString(gameDateTime);
            eventInfo.append(localTimeStr);
            String court = match.getCourt();
            if (!TextUtils.isEmpty(court)) {
                eventInfo.append(" | ");
                if (TextUtils.isDigitsOnly(court)) {
                    eventInfo.append("Court ");
                }
                eventInfo.append(court);
            }
            holder.infoView.setText(eventInfo.toString());
            holder.infoView.setTextColor(textColor);

            holder.itemView.setTag(match);
        }
    }

    private String getGameDayString(DateTime pGameDateTime) {
        DateTimeFormatter dtfDay = DateTimeFormat.forPattern("EEEE");
        return dtfDay.print(pGameDateTime);
    }

    private String getGameTimeString(DateTime pGameDateTime) {
        DateTimeFormatter dtfTime = DateTimeFormat.shortTime();
        return dtfTime.print(pGameDateTime);
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
        public TextView setPointsView;
        public TextView infoView;

        public ItemViewHolder(View v) {
            super(v);
            teamAView = (TextView) v.findViewById(R.id.match_team_a);
            teamBView = (TextView) v.findViewById(R.id.match_team_b);
            teamASetsView = (TextView) v.findViewById(R.id.match_team_a_sets);
            teamBSetsView = (TextView) v.findViewById(R.id.match_team_b_sets);
            setPointsView = (TextView) v.findViewById(R.id.match_set_points);
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

    public void updateList(List<Match> pMatchList, String pTimeDisplayType) {
        timeDisplayType = pTimeDisplayType;
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
