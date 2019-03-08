package com.back4app.quickstartexampleapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.widget.Toast;

import static com.parse.Parse.getApplicationContext;

public class ReminderAlarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
       // Toast.makeText(context, "Hello world", Toast.LENGTH_SHORT).show();

       // Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        SharedPreferences sharedPreferences=context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
        String lan=sharedPreferences.getString("language","Hindi");
        MediaPlayer mp;
        if (lan.equals("Hindi"))
        {
            mp=MediaPlayer.create(context,R.raw.hindi);
            mp.start();
        }
        else if(lan.equals("English"))
        {
            mp=MediaPlayer.create(context,R.raw.transcript);
            mp.start();
        }


    }
}
