package com.balaji.projects.myweatherapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.balaji.projects.myweatherapp.R;
/**
 * Created by Balaji on 30-May-18.
 */
public class SplashActivity extends AppCompatActivity {
    class C04471 implements Runnable {
        C04471() {
        }

        public void run() {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        new Handler().postDelayed(new C04471(), 5000);

    }
}