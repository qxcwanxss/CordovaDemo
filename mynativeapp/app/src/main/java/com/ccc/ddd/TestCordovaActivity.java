package com.ccc.ddd;

import android.os.Bundle;

import org.apache.cordova.CordovaActivity;

/**
 * @author xc
 * @date 2018/9/25
 * @desc
 */
public class TestCordovaActivity extends CordovaActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);
        //String url = "http://soft.imtt.qq.com/browser/tes/feedback.html";
        //String url = "https://www.baidu.com";
        //loadUrl(url);
    }
}
