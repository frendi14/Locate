package com.faqih.md.locate.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.faqih.md.locate.R;
import com.faqih.md.locate.adapter.MemberFragmentAdapter;
import com.faqih.md.locate.init.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Faqih on 8/22/2016.
 */
public class MemberFragment extends Fragment implements ValueEventListener,
        AdapterView.OnItemClickListener{
    private static String TAG = "MemberFragment";

    private RecyclerView recyclerViewMember;

    private ProgressDialog dialog;

    private List<Map<String,Object>> members;

    public static MemberFragment newInstance(String groupId) {
        MemberFragment fragment = new MemberFragment();

        Bundle args = new Bundle();
        args.putString(Constants.groupId, groupId);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getArguments() != null) {
            members = new ArrayList<>();
            String groupId = getArguments().getString(Constants.groupId);
            FirebaseDatabase.getInstance().getReference().child(groupId).child(Constants.members).addListenerForSingleValueEvent(this);
        }

        View rootView = inflater.inflate(R.layout.fragment_member, container, false);
        recyclerViewMember = (RecyclerView) rootView.findViewById(R.id.fragment_member_recycler_view);
        recyclerViewMember.setHasFixedSize(true);

        dialog = new ProgressDialog(getActivity());
        dialog = new ProgressDialog(getActivity());
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_progress);

        return rootView;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        dialog.dismiss();

        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
            members.add((Map<String,Object>)snapshot.getValue());
        }
        MemberFragmentAdapter adapter = new MemberFragmentAdapter(members);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewMember.setLayoutManager(llm);
        recyclerViewMember.setAdapter(adapter);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), parent.getItemAtPosition(position).toString(),Toast.LENGTH_SHORT).show();
    }
}