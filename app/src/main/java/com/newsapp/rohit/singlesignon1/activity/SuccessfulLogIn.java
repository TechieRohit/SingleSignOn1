package com.newsapp.rohit.singlesignon1.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.newsapp.rohit.singlesignon1.R;

public class SuccessfulLogIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
        setContentView(R.layout.activity_successful_log_in);

    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
        super.onBackPressed();
    }
}
