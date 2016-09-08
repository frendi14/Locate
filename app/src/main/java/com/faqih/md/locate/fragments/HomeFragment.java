package com.faqih.md.locate.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.faqih.md.locate.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by docotel on 3/4/16.
 */
public class HomeFragment extends Fragment implements ValueEventListener{
    private static String TAG = "HomeFragment";
    ImageView QR;

    public static HomeFragment newInstance(Context context, String userId) {
        HomeFragment fragment = new HomeFragment();

        Bundle args = new Bundle();
        args.putString(context.getString(R.string.userId), userId);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        QR = (ImageView)rootView.findViewById(R.id.fragment_home_QR_imageView);

        String userId = getArguments().getString(getString(R.string.userId));
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.users)).child(userId).addListenerForSingleValueEvent(this);

        return rootView;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        String groupId = dataSnapshot.child(getString(R.string.groupId)).getValue().toString();
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.groups)).child(groupId).child(getString(R.string.qr)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String base64 = dataSnapshot.getValue().toString();
                byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                QR.setImageBitmap(bitmap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        });
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
    }
}
