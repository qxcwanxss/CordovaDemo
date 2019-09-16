package com.test.MyPlugin;

import android.widget.Toast;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

public class MyPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("aaa")) {
            String message = args.getString(0);
            this.aaa(message, callbackContext);
            return true;
        }
        return false;
    }

    private void aaa(String message, CallbackContext callbackContext) {
        //弹框
        Toast.makeText(cordova.getActivity(),"aaa",Toast.LENGTH_LONG).show();
        //h5端传给我什么参数，此处再传回去
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
