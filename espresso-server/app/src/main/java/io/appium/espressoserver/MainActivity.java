package io.appium.espressoserver;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;

import static android.content.Context.WIFI_SERVICE;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);
    }
}
