package com.ccc.ddd;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TestFragment extends Fragment {
    public TestFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        String launchUrl = "file:///android_asset/www/index.html";
        final CordovaView cordovaView = view.findViewById(R.id.cv);
        cordovaView.initCordova(getActivity());
        cordovaView.loadUrl(launchUrl);
        cordovaView.setOnReceivedErrorListener(new CordovaView.OnReceivedErrorListener() {
            @Override
            public void onReceivedError(int errorCode, String description, String failingUrl) {
                Log.i("onReceivedError", "errorCode:" + errorCode + "     description:" + description + "     failingUrl:" + failingUrl);
            }
        });
        cordovaView.getWebview().reload();
        cordovaView.getWebview().goBack();
        cordovaView.getWebview().setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                //进度条
                super.onProgressChanged(view, newProgress);
            }
        });
    }
}
