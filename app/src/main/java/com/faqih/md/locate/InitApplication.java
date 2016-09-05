package com.faqih.md.locate;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Faqih on 8/23/2016.
 */
public class InitApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    /* Enable disk persistence  */
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
