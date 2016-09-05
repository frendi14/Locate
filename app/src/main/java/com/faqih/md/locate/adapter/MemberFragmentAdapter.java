package com.faqih.md.locate.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.faqih.md.locate.R;
import com.faqih.md.locate.init.Constants;

import java.util.List;
import java.util.Map;

/**
 * Created by Faqih on 8/25/2016.
 */
public class MemberFragmentAdapter extends RecyclerView.Adapter<MemberFragmentAdapter.ViewHolder>{
    private List<Map<String,Object>> dataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView memberName;
        protected TextView memberStatus;
        protected ImageButton messageButton;

        public ViewHolder(View v) {
            super(v);
            memberName = (TextView)v.findViewById(R.id.item_member_textView_userName);
            memberStatus = (TextView)v.findViewById(R.id.item_member_textView_status);
            messageButton = (ImageButton) v.findViewById(R.id.item_member_imageButton);
        }
    }
    public MemberFragmentAdapter(List<Map<String,Object>> dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public MemberFragmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_member, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MemberFragmentAdapter.ViewHolder holder, int position) {
        holder.memberName.setText((String)dataSet.get(position).get(Constants.memberName));
        holder.memberStatus.setText((String)dataSet.get(position).get(Constants.status));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
