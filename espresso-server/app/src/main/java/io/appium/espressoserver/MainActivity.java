package io.appium.espressoserver;

import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);
    }
}
