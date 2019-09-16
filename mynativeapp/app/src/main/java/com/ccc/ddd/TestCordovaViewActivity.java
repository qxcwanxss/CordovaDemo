package com.ccc.ddd;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TestCordovaViewActivity extends AppCompatActivity {

    private CordovaView cordovaView;
    private String launchUrl = "file:///android_asset/www/index.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_cordova_view);

        cordovaView = findViewById(R.id.cv);
        cordovaView.initCordova(this);
        cordovaView.loadUrl(launchUrl);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cordovaView.onDestroy();
    }
}
