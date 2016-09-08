package com.faqih.md.locate;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.faqih.md.locate.init.Constants;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class InvitationScanActivity extends AppCompatActivity implements SurfaceHolder.Callback, Detector.Processor<Barcode>, ValueEventListener{
    String TAG = "InvitationScanActivity";

    SurfaceView cameraView;

    CameraSource cameraSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_scan);

        cameraView = (SurfaceView) findViewById(R.id.invitation_scan_surface_view);

        BarcodeDetector barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(480, 480)
                .build();

        cameraView.getHolder().addCallback(this);

        barcodeDetector.setProcessor(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int cameraPermission = checkSelfPermission(android.Manifest.permission.CAMERA);
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    showMessageOKCancel(getString(R.string.prompt_message_dialog_camera_permission),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(InvitationScanActivity.this,
                                            new String[] {android.Manifest.permission.CAMERA},
                                            Constants.REQUEST_CODE_CAMERA);
                                }
                            });
                    return;
                }

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA},
                        Constants.REQUEST_CODE_CAMERA);
                return;
            }
        }
        try {
            cameraSource.start(cameraView.getHolder());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        cameraSource.stop();
        cameraSource.release();
        cameraView.getHolder().removeCallback(this);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.action_ok), okListener)
                .setNegativeButton(getString(R.string.action_cancel), null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CODE_CAMERA: {
                if (grantResults.length <= 0){
                    Toast.makeText(this, getString(R.string.prompt_message_camera_permission_denied), Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void release() {

    }
    boolean scanned = false;
    @Override
    public void receiveDetections(Detector.Detections<Barcode> detections) {
        if (detections.getDetectedItems().size()>0){
            if (!scanned){
                String invitationCode = detections.getDetectedItems().valueAt(0).displayValue;

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.invitations))
                        .child(invitationCode)
                        .child(getString(R.string.groupId)).addListenerForSingleValueEvent(this);

//                cameraSource.stop();
                cameraSource.release();
                cameraView.getHolder().removeCallback(this);
                scanned = true;
            }
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        final String groupId = (String) dataSnapshot.getValue();
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.groups)).child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String count = dataSnapshot.child(getString(R.string.count)).getValue().toString();
                String limit = dataSnapshot.child(getString(R.string.limit)).getValue().toString();

                if (Integer.parseInt(count) > Integer.parseInt(limit)){
                    Toast.makeText(InvitationScanActivity.this, "Limit member", Toast.LENGTH_SHORT).show();
                } else {
                    String[] groups = new String[]{getString(R.string.member), groupId, count};
                    startActivity(new Intent(InvitationScanActivity.this, UserRegistrationActivity.class).putExtra(getString(R.string.groups), groups));
                    overridePendingTransition(R.anim.activity_animation_right_in, R.anim.activity_animation_left_out);
                    finish();
                }
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
