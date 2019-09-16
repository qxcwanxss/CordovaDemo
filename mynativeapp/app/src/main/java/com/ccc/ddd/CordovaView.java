package com.ccc.ddd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.apache.cordova.Config;
import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.PluginManager;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * 自定义Cordova控件
 * 1、可用于Activity、Fragment集成
 * 2、可在布局xml文件中引入
 * 3、可在代码中使用new关键字创建实例
 *
 * <p>
 * 使用示例：
 * String launchUrl = "file:///android_asset/www/index.html";
 * CordovaView cordovaView = view.findViewById(R.id.cv);
 * cordovaView.initCordova(getActivity());
 * cordovaView.loadUrl(launchUrl);
 * <p>
 */
public class CordovaView extends RelativeLayout {
    //页面对象
    private Activity activity;
    //Cordova浏览器对象: 初始化、UI布局控制、url加载、生命周期（开始、暂停、销毁...）
    protected CordovaWebView appView;
    //Cordova配置对象: 各类配置信息读取、设置、使用
    protected CordovaPreferences preferences;
    //Cordova接口实现对象: 消息处理（页面跳转、页面数据存取、权限申请...）
    protected CordovaInterfaceImpl cordovaInterface;
    //是否保持运行
    protected boolean keepRunning = true;
    //是否沉浸式
    protected boolean immersiveMode;
    //默认启动url
    protected String launchUrl;
    //插件实体类集合
    protected ArrayList<PluginEntry> pluginEntries;
    //接收错误的监听器（用于回调页面加载错误，如：页面未找到等等。使用方需先调用方法：setOnReceivedErrorListener()）
    private OnReceivedErrorListener errorListener;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public CordovaView(Context context) {
        super(context);
    }

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param attrs   属性
     */
    public CordovaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 初始化Cordova
     *
     * @param activity 页面
     */
    public void initCordova(Activity activity) {
        this.activity = activity;

        //加载配置信息
        loadConfig();

        //设置页面是否全屏
        if (preferences.getBoolean("SetFullscreen", false)) {
            preferences.set("Fullscreen", true);
        }
        if (preferences.getBoolean("Fullscreen", false)) {
            if (!preferences.getBoolean("FullscreenNotImmersive", false)) {
                immersiveMode = true;
            } else {
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        } else {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        //实例化接口实现
        cordovaInterface = makeCordovaInterface();

        //设置背景为白色
        activity.getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        //初始化
        initCordova();
    }

    /**
     * 加载配置信息
     * 1.读取默认启动的url  file:///android_asset/www/index.html
     * 2.读取res/xml/config.xml文件，获得插件集合 pluginEntries
     */
    private void loadConfig() {
        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(activity);
        preferences = parser.getPreferences();
        preferences.setPreferencesBundle(activity.getIntent().getExtras());
        launchUrl = parser.getLaunchUrl();
        pluginEntries = parser.getPluginEntries();
        try {
            //通过反射处理
            Field field = Config.class.getDeclaredField("parser");
            field.setAccessible(true);
            field.set(null, parser);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 初始化
     */
    private void initCordova() {
        //实例化webview对象
        appView = makeWebView();
        //将webview加载到页面中，并根据参数配置其属性
        createViews();
        //如果"实例化接口"为空
        if (!appView.isInitialized()) {
            //appview初始化
            //初始化插件管理
            //初始化消息队列
            //初始化桥模块
            //......
            appView.init(cordovaInterface, pluginEntries, preferences);
        }
        //设置插件管理器
        //设置onActivityResult消息回调
        //设置Activity销毁处理
        cordovaInterface.onCordovaInit(appView.getPluginManager());
    }


    /**
     * 创建views
     */
    @SuppressWarnings({"deprecation", "ResourceType"})
    private void createViews() {
        //appView.getView()指SystemWebViewEngine的SystemWebView，继承自android.webkit.WebView
        appView.getView().setId(100);
        appView.getView().setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        //设置当前视图为SystemWebViewEngine的SystemWebView
        //setContentView(appView.getView());
        this.removeAllViews();
        this.addView(appView.getView(), new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //如果preferences有配置背景色，则设置webview背景色
        if (preferences.contains("BackgroundColor")) {
            try {
                int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
                // Background of activity:
                appView.getView().setBackgroundColor(backgroundColor);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        //webview获得焦点（不受touch限制）
        appView.getView().requestFocusFromTouch();
    }

    /**
     * 构建CordovaWebView
     *
     * @return CordovaWebView
     */
    private CordovaWebView makeWebView() {
        //1.通过preferences配置信息构建CordovaWebViewEngine
        //2.通过CordovaWebViewEngine构建CordovaWebView
        return new CordovaWebViewImpl(makeWebViewEngine());
    }

    /**
     * 通过preferences配置信息构建CordovaWebViewEngine
     *
     * @return CordovaWebViewEngine
     */
    private CordovaWebViewEngine makeWebViewEngine() {
        return CordovaWebViewImpl.createEngine(activity, preferences);
    }

    /**
     * 构建接口实现类，接收消息
     *
     * @return CordovaInterfaceImpl
     */
    private CordovaInterfaceImpl makeCordovaInterface() {
        return new CordovaInterfaceImpl(activity) {
            @Override
            public Object onMessage(String id, Object data) {
                return CordovaView.this.onMessage(id, data);
            }
        };
    }

    /**
     * 处理消息
     *
     * @param id   消息id
     * @param data 消息数据
     * @return 处理结果
     */
    public Object onMessage(String id, Object data) {
        try {
            if ("onReceivedError".equals(id)) {
                JSONObject d = (JSONObject) data;
                try {
                    //将消息透传给客户端
                    if (errorListener != null) {
                        int errorCode = d.getInt("errorCode");
                        String description = d.getString("description");
                        String failingUrl = d.getString("url");
                        errorListener.onReceivedError(errorCode, description, failingUrl);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    /**
     * 获得Webview组件，供客户端使用
     *
     * @return Webview组件
     */
    public SystemWebView getWebview() {
        if (appView != null && appView.getView() instanceof WebView) {
            SystemWebView webView = (SystemWebView) appView.getView();
            return webView;
        }
        return null;
    }

    /**
     * 获得系统webview引擎
     * @return 系统webview引擎
     */
    public SystemWebViewEngine getSystemWebViewEngine(){
        return (SystemWebViewEngine) appView.getEngine();
    }

    /**
     * 加载url
     *
     * @param url 地址，默认应是：file:///android_asset/www/index.html
     */
    public void loadUrl(String url) {
        if (appView == null) {
            initCordova();
        }

        // If keepRunning
        // 如果preferences配置了KeepRunning，则页面置于后台时，仍可见
        this.keepRunning = preferences.getBoolean("KeepRunning", true);

        //加载url
        //第2个参数表示重新初始化插件管理器、插件集合等
        appView.loadUrlIntoView(url, true);
    }

    /**
     * 当页面执行onPause方法时，可调用
     */
    public void onPause() {
        if (this.appView != null) {
            CordovaPlugin activityResultCallback = null;
            try {
                Field field = CordovaInterfaceImpl.class.getDeclaredField("activityResultCallback");
                field.setAccessible(true);
                activityResultCallback = (CordovaPlugin) field.get(this.cordovaInterface);
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean keepRunning = this.keepRunning || activityResultCallback != null;
            this.appView.handlePause(keepRunning);
        }
    }

    /**
     * 当页面执行onNewIntent方法时，可调用
     */
    public void onNewIntent(Intent intent) {
        if (this.appView != null)
            this.appView.onNewIntent(intent);
    }

    /**
     * 当页面执行onResume方法时，可调用
     */
    public void onResume() {
        if (this.appView == null) {
            return;
        }
        activity.getWindow().getDecorView().requestFocus();
        this.appView.handleResume(this.keepRunning);
    }

    /**
     * 当页面执行onStop方法时，可调用
     */
    public void onStop() {
        if (this.appView == null) {
            return;
        }
        this.appView.handleStop();
    }

    /**
     * 当页面执行onStart方法时，可调用
     */
    public void onStart() {
        if (this.appView == null) {
            return;
        }
        this.appView.handleStart();
    }

    /**
     * 当页面执行onDestroy方法时，可调用
     */
    public void onDestroy() {
        if (this.appView != null) {
            appView.handleDestroy();
        }
    }

    /**
     * 当页面执行onWindowFocusChanged方法时，可调用
     */
    @SuppressLint("InlinedApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //设置沉浸式与全屏
        if (hasFocus && immersiveMode) {
            final int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            activity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * 当页面执行startActivityForResult方法时，可调用
     */
    @SuppressLint({"NewApi", "RestrictedApi"})
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        // Capture requestCode here so that it is captured in the setActivityResultCallback() case.
        cordovaInterface.setActivityResultRequestCode(requestCode);
        //super.startActivityForResult(intent, requestCode, options);
    }

    /**
     * 当页面执行onActivityResult方法时，可调用
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //super.onActivityResult(requestCode, resultCode, intent);
        cordovaInterface.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * 当页面执行onSaveInstanceState方法时，可调用
     */
    public void onSaveInstanceState(Bundle outState) {
        cordovaInterface.onSaveInstanceState(outState);
    }

    /**
     * 当页面执行onConfigurationChanged方法时，可调用
     */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.appView == null) {
            return;
        }
        PluginManager pm = this.appView.getPluginManager();
        if (pm != null) {
            pm.onConfigurationChanged(newConfig);
        }
    }

    /**
     * 接口：接收到错误时的回调
     */
    public interface OnReceivedErrorListener {
        /**
         * 接收到错误
         *
         * @param errorCode   错误码（请参考Cordova官方定义）
         * @param description 错误描述
         * @param failingUrl  发生异常的url
         */
        void onReceivedError(int errorCode, String description, String failingUrl);
    }

    /**
     * 设置监听
     *
     * @param listener 监听器
     */
    public void setOnReceivedErrorListener(OnReceivedErrorListener listener) {
        this.errorListener = listener;
    }
}
