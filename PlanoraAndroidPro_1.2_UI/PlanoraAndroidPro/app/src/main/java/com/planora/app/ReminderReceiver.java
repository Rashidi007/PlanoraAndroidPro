package com.planora.app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.Iterator;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String PREF="planora_reminders";
    private static final String DATA="data";
    private static final String CHANNEL="planora_reminders_channel";

    @Override public void onReceive(Context context, Intent intent){
        String id=intent.getStringExtra("id"); if(id==null)return;
        JSONObject all=readAll(context), p=all.optJSONObject(id); if(p==null)return;
        createChannel(context);
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(nm!=null && (Build.VERSION.SDK_INT<33 || context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)==android.content.pm.PackageManager.PERMISSION_GRANTED)){
            android.app.Notification.Builder b=Build.VERSION.SDK_INT>=26?new android.app.Notification.Builder(context,CHANNEL):new android.app.Notification.Builder(context);
            Intent open=new Intent(context,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            b.setSmallIcon(R.drawable.ic_planora).setContentTitle(p.optString("title","Planora")).setContentText(p.optString("body","یادآوری"))
              .setAutoCancel(true).setCategory(android.app.Notification.CATEGORY_REMINDER)
              .setContentIntent(PendingIntent.getActivity(context,1,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
            nm.notify(requestCode(id),b.build());
        }
        String repeat=p.optString("repeat","none");
        long next=nextTime(p.optLong("when",System.currentTimeMillis()),repeat);
        if(next>System.currentTimeMillis() && !"none".equals(repeat)){
            try{p.put("when",next);all.put(id,p);writeAll(context,all);schedule(context,p);}catch(Exception ignored){}
        }else{
            all.remove(id);writeAll(context,all);
        }
    }

    static void schedule(Context c, JSONObject p){
        try{
            long when=p.optLong("when",0); if(when<=System.currentTimeMillis())return;
            AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE); if(am==null)return;
            String id=p.getString("id"); Intent i=new Intent(c,ReminderReceiver.class).setAction("PLANORA_REMINDER").putExtra("id",id);
            PendingIntent pi=PendingIntent.getBroadcast(c,requestCode(id),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            boolean exact=p.optBoolean("exact",true);
            if(Build.VERSION.SDK_INT>=31&&exact&&am.canScheduleExactAlarms())am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
            else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
            JSONObject all=readAll(c);all.put(id,p);writeAll(c,all);
        }catch(Exception ignored){}
    }

    static void cancel(Context c,String id){
        try{
            AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
            Intent i=new Intent(c,ReminderReceiver.class).setAction("PLANORA_REMINDER").putExtra("id",id);
            PendingIntent pi=PendingIntent.getBroadcast(c,requestCode(id),i,PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);
            if(am!=null&&pi!=null)am.cancel(pi);
            JSONObject all=readAll(c);all.remove(id);writeAll(c,all);
        }catch(Exception ignored){}
    }

    static void cancelAll(Context c){
        JSONObject snapshot=readAll(c);Iterator<String> it=snapshot.keys();java.util.ArrayList<String> ids=new java.util.ArrayList<>();while(it.hasNext())ids.add(it.next());for(String id:ids)cancel(c,id);
    }

    static void rescheduleAll(Context c){JSONObject all=readAll(c);Iterator<String> it=all.keys();java.util.ArrayList<JSONObject> list=new java.util.ArrayList<>();while(it.hasNext()){JSONObject p=all.optJSONObject(it.next());if(p!=null)list.add(p);}for(JSONObject p:list)schedule(c,p);}

    static long nextTime(long from,String repeat){Calendar cal=Calendar.getInstance();cal.setTimeInMillis(from);if("daily".equals(repeat))cal.add(Calendar.DAY_OF_YEAR,1);else if("weekly".equals(repeat))cal.add(Calendar.WEEK_OF_YEAR,1);else if("monthly".equals(repeat))cal.add(Calendar.MONTH,1);else return -1;return cal.getTimeInMillis();}
    private static int requestCode(String id){return id.hashCode()&0x7fffffff;}
    private static void createChannel(Context c){if(Build.VERSION.SDK_INT>=26){NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);if(nm!=null&&nm.getNotificationChannel(CHANNEL)==null){NotificationChannel ch=new NotificationChannel(CHANNEL,"یادآوری‌های Planora",NotificationManager.IMPORTANCE_HIGH);ch.setDescription("اعلان زمان‌دار کارها و برنامه‌ها");ch.enableVibration(true);nm.createNotificationChannel(ch);}}}
    private static JSONObject readAll(Context c){try{return new JSONObject(c.getSharedPreferences(PREF,Context.MODE_PRIVATE).getString(DATA,"{}"));}catch(Exception e){return new JSONObject();}}
    private static void writeAll(Context c,JSONObject all){c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putString(DATA,all.toString()).apply();}
}
