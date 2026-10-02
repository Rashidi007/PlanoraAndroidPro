package com.planora.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.OutputStream;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView web;
    private static final int NOTIFICATION_PERMISSION = 2001;
    private static final int CREATE_BACKUP = 3001;
    private static final int PICK_BACKUP = 3002;
    private ValueCallback<Uri[]> filePathCallback;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        web.setBackgroundColor(0xFF0B0910);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setDatabaseEnabled(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){ return false; }
        });
        web.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams params){
                if(filePathCallback!=null) filePathCallback.onReceiveValue(null);
                filePathCallback=cb;
                Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("application/json");
                startActivityForResult(i,PICK_BACKUP);
                return true;
            }
        });
        web.addJavascriptInterface(new NativeBridge(this), "Android");
        if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true);
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
        requestNotifications();
    }

    public void requestNotifications(){
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION);
        }
    }

    public void openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()));
                startActivity(i);
            } catch (Exception ignored) {}
        }
    }

    public void exportBackup(String json){
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/json");
        i.putExtra(Intent.EXTRA_TITLE,"planora-backup.json");
        getIntent().putExtra("pendingBackup", json);
        startActivityForResult(i,CREATE_BACKUP);
    }

    public class NativeBridge {
        private final MainActivity a;
        NativeBridge(MainActivity activity){a=activity;}
        @JavascriptInterface public void scheduleReminder(String json){try{ReminderReceiver.schedule(a,new JSONObject(json));}catch(Exception ignored){}}
        @JavascriptInterface public void cancelReminder(String id){ReminderReceiver.cancel(a,id);}
        @JavascriptInterface public void cancelAllReminders(){ReminderReceiver.cancelAll(a);}
        @JavascriptInterface public void requestNotifications(){a.runOnUiThread(a::requestNotifications);}
        @JavascriptInterface public void openExactAlarmSettings(){a.runOnUiThread(a::openExactAlarmSettings);}
        @JavascriptInterface public void exportBackup(String json){a.runOnUiThread(() -> a.exportBackup(json));}
        @JavascriptInterface public boolean canScheduleExactAlarms(){
            if(Build.VERSION.SDK_INT<31)return true;
            AlarmManager am=(AlarmManager)a.getSystemService(Context.ALARM_SERVICE);
            return am!=null&&am.canScheduleExactAlarms();
        }
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==CREATE_BACKUP && resultCode==RESULT_OK && data!=null){
            String json=getIntent().getStringExtra("pendingBackup");
            if(json!=null){try(OutputStream os=getContentResolver().openOutputStream(data.getData())){if(os!=null)os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));}catch(Exception ignored){}}
            getIntent().removeExtra("pendingBackup");
        }
        if(requestCode==PICK_BACKUP){
            if(filePathCallback!=null){
                Uri[] results=(resultCode==RESULT_OK&&data!=null&&data.getData()!=null)?new Uri[]{data.getData()}:null;
                filePathCallback.onReceiveValue(results);filePathCallback=null;
            }
        }
    }

    @Override public void onBackPressed(){
        if(web!=null && web.canGoBack()) { web.goBack(); return; }
        super.onBackPressed();
    }
}
