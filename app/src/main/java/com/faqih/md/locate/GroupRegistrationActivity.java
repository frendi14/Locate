package com.faqih.md.locate;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class GroupRegistrationActivity extends AppCompatActivity implements View.OnClickListener,
        ValueEventListener, DialogInterface.OnCancelListener{

    private String TAG = "RegisterGroupsActivity";

    // UI references.
    private EditText editTextGroupID;
    private EditText editTextGroupName;
    private Button buttonRegister;

    private ProgressDialog dialog;

    private DatabaseReference databaseReference;

    private String groupId;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_registration);

        init();

        editTextGroupID = (EditText)findViewById(R.id.group_registration_id);
        editTextGroupName = (EditText)findViewById(R.id.group_registration_name);
        buttonRegister = (Button)findViewById(R.id.group_register_button);
        buttonRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        attemptRegister();
    }

    private void init(){
        databaseReference = FirebaseDatabase.getInstance().getReference();
        dialog = new ProgressDialog(this);
    }

    private void attemptRegister() {
        editTextGroupID.setError(null);
        editTextGroupName.setError(null);

        groupId = editTextGroupID.getText().toString();
        groupName = editTextGroupName.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(groupId)) {
            editTextGroupID.setError(getString(R.string.error_field_required));
            focusView = editTextGroupID;
            cancel = true;
        }

        if (TextUtils.isEmpty(groupName)) {
            editTextGroupName.setError(getString(R.string.error_field_required));
            focusView = editTextGroupName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            register();
        }
    }

    private void register(){
        dialog = new ProgressDialog(this);
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_progress);
        dialog.setOnCancelListener(this);
        databaseReference.child(getString(R.string.groups)).child(groupId).addListenerForSingleValueEvent(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        dialog.dismiss();
        if (!dataSnapshot.exists()){
            String[] groups = new String[]{getString(R.string.admin), groupId, groupName};
            startActivity(new Intent(this, UserRegistrationActivity.class).putExtra(getString(R.string.groups), groups));
            overridePendingTransition(R.anim.activity_animation_right_in, R.anim.activity_animation_left_out);
        } else {
            editTextGroupID.setError(getString(R.string.error_groupId_exist));
            editTextGroupID.requestFocus();
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
        dialog.dismiss();
        Toast.makeText(GroupRegistrationActivity.this,databaseError.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        databaseReference.removeEventListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_animation_back_left_in, R.anim.activity_animation_back_rigt_out);
    }
}