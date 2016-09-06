package com.faqih.md.locate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Faqih on 9/2/2016.
 */
public class SignUpMenuActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_menu);

        Button buttonCreateGroup = (Button)findViewById(R.id.sign_up_menu_group_reg_button);
        buttonCreateGroup.setOnClickListener(this);

        Button butonInvites = (Button)findViewById(R.id.sign_up_menu_member_reg_button);
        butonInvites.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.sign_up_menu_group_reg_button){
            startActivity(new Intent(this, GroupRegistrationActivity.class));
            overridePendingTransition(R.anim.activity_animation_right_in, R.anim.activity_animation_left_out);
        } else if (v.getId()==R.id.sign_up_menu_member_reg_button){
            startActivity(new Intent(this, InvitationScanActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_animation_back_left_in, R.anim.activity_animation_back_rigt_out);
    }
}
