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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.faqih.md.locate.init.Constants;
import com.faqih.md.locate.util.MD5;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Faqih on 9/2/2016.
 */
public class SignInActivity extends AppCompatActivity implements View.OnClickListener,
        ValueEventListener, DialogInterface.OnCancelListener {
    private String TAG = "LoginActivity";

    private EditText editTextUserId;
    private EditText editTextPassword;
    private TextView textViewMessageRegister;
    private Button buttonSignIn;

    private ProgressDialog dialog;

    private String password;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        init();

        editTextUserId = (EditText)findViewById(R.id.login_user_id);
        editTextPassword = (EditText)findViewById(R.id.login_password);

        textViewMessageRegister = (TextView)findViewById(R.id.login_message_register);
        textViewMessageRegister.setOnClickListener(this);

        buttonSignIn = (Button)findViewById(R.id.login_button);
        buttonSignIn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.login_message_register){
            startActivity(new Intent(this, SignUpMenuActivity.class));
            overridePendingTransition(R.anim.activity_animation_right_in, R.anim.activity_animation_left_out);
        } else if (v.getId()==R.id.login_button){
            attemptLogin();
        }
    }

    private void init(){
        dialog = new ProgressDialog(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void attemptLogin() {
        editTextUserId.setError(null);
        editTextPassword.setError(null);

        String userId = editTextUserId.getText().toString();
        password = editTextPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(userId) && editTextUserId.getVisibility()==View.VISIBLE) {
            editTextUserId.setError(getString(R.string.error_field_required));
            focusView = editTextUserId;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError(getString(R.string.error_field_required));
            focusView = editTextPassword;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            login(userId);
        }
    }

    private void login( String userId){
        dialog = new ProgressDialog(this);
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_progress);
        dialog.setOnCancelListener(this);

        databaseReference.child(getString(R.string.constant_users)).child(userId).addListenerForSingleValueEvent(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if(dataSnapshot.exists()){
            String passwordHash = null;
            try {
                passwordHash = MD5.generateMD5(password);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            if (!dataSnapshot.child(Constants.password).getValue().toString().equals(passwordHash)){
                dialog.dismiss();
                editTextPassword.setError(getString(R.string.error_password_login_failed));
                editTextPassword.requestFocus();
            } else {
                dialog.dismiss();
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.activity_animation_right_in, R.anim.activity_animation_left_out);
                finish();
            }
        } else {
            dialog.dismiss();
            editTextUserId.setError(getString(R.string.error_userId_not_exist));
            editTextUserId.requestFocus();
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
        dialog.dismiss();
        Toast.makeText(this,databaseError.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        databaseReference.removeEventListener(this);
    }

    private void request_update(String groupId){
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("title",Constants.MESSAGE_TYPE_REQUEST_LOCATION);
            messageJson.put("", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "https://fcm.googleapis.com/fcm/send";
        JSONObject object = new JSONObject();
        try {
            object.put("to", "/topics/"+groupId);
            object.put("notification", messageJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(new JsonObjectRequest(
                Request.Method.POST,
                url,
                object,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", getString(R.string.constant_serverKey));
                return headers;
            }
        });
    }
}