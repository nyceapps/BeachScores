package com.nyceapps.beachscores;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nyceapps.beachscores.entity.Event;
import com.nyceapps.beachscores.entity.Match;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by lugosi on 22.05.17.
 */

class MatchListAdapter extends RecyclerView.Adapter<MatchListAdapter.ViewHolder> {
    private List<Match> matchList;
    private ActivityDelegate delegate;

    public MatchListAdapter(List<Match> pMatchList, ActivityDelegate pDelegate) {
        matchList = pMatchList;
        delegate = pDelegate;
    }

    @Override
    public MatchListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MatchListAdapter.ViewHolder holder, int position) {
        Match match = matchList.get(position);

        String matchName = match.getTeamAName() + " vs. " + match.getTeamBName();
        holder.nameTextView.setText(matchName);

        holder.itemView.setTag(match);
    }

    public void updateList(List<Match> pMatchList) {
        if (pMatchList.size() != matchList.size() || !matchList.containsAll(pMatchList)) {
            matchList = pMatchList;
            notifyDataSetChanged();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView nameTextView;
        public TextView periodTextView;
        public TextView locationTextView;

        public ViewHolder(View v) {
            super(v);
            nameTextView = (TextView) v.findViewById(R.id.match_name);
            periodTextView = (TextView) v.findViewById(R.id.match_period);
            locationTextView = (TextView) v.findViewById(R.id.match_location);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            delegate.onClick(v);
        }
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }
}
