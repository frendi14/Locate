package com.faqih.md.locate;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.faqih.md.locate.init.Constants;
import com.faqih.md.locate.init.Validations;
import com.faqih.md.locate.util.MD5;
import com.faqih.md.locate.util.QRCode;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.zxing.WriterException;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Faqih on 9/4/2016.
 */
public class UserRegistrationActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ValueEventListener,
        DialogInterface.OnCancelListener {

    private String TAG = "MemberRegistrationActivity";

    private EditText editTextUserId;
    private EditText editTextUserName;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;

    private ProgressDialog dialog;

    private DatabaseReference databaseReference;

    private String groupId;
    private String groupName;
    private String userId;
    private String userName;
    private String password;

    private GoogleApiClient mGoogleApiClient;

    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        if(getIntent().getExtras()!=null){
            String[] groups = getIntent().getExtras().getStringArray(getString(R.string.groups));
            groupId = groups[0];
            groupName = groups[1];
        } else {
            finish();
        }

        editTextUserId = (EditText) findViewById(R.id.member_registration_user_id);
        editTextUserName = (EditText) findViewById(R.id.member_registration_name);
        editTextPassword = (EditText) findViewById(R.id.member_registration_password);
        editTextConfirmPassword = (EditText) findViewById(R.id.member_registration_confirm_password);

        buttonRegister = (Button) findViewById(R.id.member_registration_button);
        buttonRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        attemptRegister();
    }

    @Override
    protected void onStart() {
        super.onStart();
        dialog = new ProgressDialog(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void attemptRegister() {
        editTextUserId.setError(null);
        editTextPassword.setError(null);
        editTextConfirmPassword.setError(null);

        userId = editTextUserId.getText().toString();
        userName = editTextUserName.getText().toString();
        password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(userId)) {
            editTextUserId.setError(getString(R.string.error_field_required));
            focusView = editTextUserId;
            cancel = true;
        }

        if (TextUtils.isEmpty(userName)) {
            editTextUserName.setError(getString(R.string.error_field_required));
            focusView = editTextUserName;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError(getString(R.string.error_field_required));
            focusView = editTextPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError(getString(R.string.error_field_required));
            focusView = editTextConfirmPassword;
            cancel = true;
        }

        if (!Validations.isPasswordValid(password)&&!TextUtils.isEmpty(password)) {
            editTextPassword.setError(getString(R.string.error_password_less));
            focusView = editTextPassword;
            cancel = true;
        }

        if (!Validations.isPasswordMatch(password, confirmPassword)&&!TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError(getString(R.string.error_password_not_match));
            focusView = editTextConfirmPassword;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            register();
        }
    }

    private void register() {
        dialog = new ProgressDialog(this);
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_progress);
        dialog.setOnCancelListener(this);

        databaseReference.child(getString(R.string.users)).child(userId).addListenerForSingleValueEvent(this);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        databaseReference.removeEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (!dataSnapshot.exists()){
            DatabaseReference referenceInvitations = databaseReference.child(getString(R.string.invitations)).push();

            Map<String, Object> map = new HashMap<>();
            map.put(getString(R.string.groupId), groupId);

            referenceInvitations.setValue(map);
            String invitationCode = referenceInvitations.getKey();

            String code = null;
            try {
                Bitmap bmp = QRCode.getQRCode(invitationCode);
                code = com.faqih.md.locate.util.Base64.base64fromBitmap(bmp);
            } catch (WriterException e) {
                e.printStackTrace();
            }

            String uid = FirebaseInstanceId.getInstance().getToken();

            String encryptedPassword = null;
            try {
                encryptedPassword = MD5.generateMD5(password);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            Map<String, Object> userMap = new HashMap<>();
            userMap.put(getString(R.string.name), userName);
            userMap.put(getString(R.string.groupId), groupId);
            userMap.put(getString(R.string.password), encryptedPassword);
            userMap.put(getString(R.string.uid), uid);

            if (!TextUtils.isEmpty(groupName)){
                userMap.put(getString(R.string.roleId), getString(R.string.admin));
                userMap.put(getString(R.string.status), getString(R.string.defaultStatusAdmin));

                Map<String, Object> adminMap = new HashMap<>();
                adminMap.put(getString(R.string.uid),uid);
                adminMap.put(getString(R.string.userId), userId);
                adminMap.put(getString(R.string.name), userName);

                Map<String, Object> groupMap = new HashMap<>();
                groupMap.put(getString(R.string.admin), adminMap);
                groupMap.put(getString(R.string.name), groupName);

                Calendar expired = Calendar.getInstance();
                expired.add(Calendar.MONTH, Constants.defaultMonthExpired);

                String expiredString = new SimpleDateFormat(getString(R.string.timestamp), Locale.getDefault()).format(expired.getTime());

                groupMap.put(getString(R.string.expired), expiredString);
                groupMap.put(getString(R.string.limit), Constants.defaultLimitGroup);
                groupMap.put(getString(R.string.qr), code);
                groupMap.put(getString(R.string.status), getString(R.string.defaultStatus));

                databaseReference.child(getString(R.string.groups)).child(groupId).setValue(groupMap);
            } else {
                userMap.put(getString(R.string.status), getString(R.string.defaultStatus));
                userMap.put(getString(R.string.roleId), getString(R.string.member));

                FirebaseMessaging.getInstance().subscribeToTopic(groupId);
            }

            databaseReference.child(getString(R.string.users)).child(userId).setValue(userMap);

            String time = new SimpleDateFormat(getString(R.string.timestamp), Locale.getDefault()).format(new Date());

            if(location!=null){
                Map<String, Object> locationMap = new HashMap<>();
                locationMap.put(getString(R.string.latitude),location.getLatitude());
                locationMap.put(getString(R.string.longitude), location.getLongitude());
                locationMap.put(getString(R.string.time), time);

                DatabaseReference referenceLocation = databaseReference.child(getString(R.string.locations)).child(groupId).child(userId).push();
                referenceLocation.setValue(locationMap);
            }

            Map<String, Object> updatesMap =new HashMap<>();
            updatesMap.put(getString(R.string.time), time);
            updatesMap.put(getString(R.string.name), userName);
            updatesMap.put(getString(R.string.updates), getString(R.string.defaultUpdates));

            DatabaseReference referenceUpdates = databaseReference.child(getString(R.string.updates)).child(groupId).child(userId).push();
            referenceUpdates.setValue(updatesMap);

            dialog.dismiss();

            startActivity(new Intent(this, SignInActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            overridePendingTransition(R.anim.activity_animation_back_left_in, R.anim.activity_animation_back_rigt_out);
        } else {
            dialog.dismiss();

            editTextUserId.setError(getString(R.string.error_user_id_exist));
            editTextUserId.requestFocus();
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
        dialog.dismiss();
        Toast.makeText(this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        request_location();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    private void request_location(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int fineLocationPermission = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
            int coarseLocationPermission = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);

            if (fineLocationPermission != PackageManager.PERMISSION_GRANTED
                    && coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

                if (!shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    showMessageOKCancel("You need to allow access to location",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(UserRegistrationActivity.this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                            Constants.REQUEST_CODE_LOCATION);
                                }
                            });
                    return;
                }

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.REQUEST_CODE_LOCATION);
                return;
            }
        }

        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CODE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    request_location();
                } else {
                    Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (this.location==null || location.getAccuracy() < this.location.getAccuracy()){
            this.location = location;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_animation_back_left_in, R.anim.activity_animation_back_rigt_out);
    }
}