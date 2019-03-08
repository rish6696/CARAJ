package com.back4app.quickstartexampleapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MyAlarm extends BroadcastReceiver {
    private final String Channel_ID="personal notification";
    private final int NOTIFICATION_ID=001;
    @Override
    public void onReceive(final Context context, Intent intent) {



        ParseQuery<ParseObject> query=ParseQuery.getQuery("notificationList");
        query.whereEqualTo("notificationshown",false);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects!=null&&objects.size()>0) {
                    for (ParseObject obj : objects) {
                        String username = obj.getString("UserName");
                        String subject = obj.getString("subject");
                        int id=obj.getObjectId().hashCode();
                        Shownotification1(context,username,subject,obj.getObjectId(),id);
                        obj.deleteInBackground();
                        obj.saveInBackground();

                    }
                }
            }
        });



    }


    public void Shownotification1(Context context,String username,String filename,String objectId,int notid)
    {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            CharSequence name="Personal Notification";
            String description="Include Personal Notifications";
            int importance=NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel=new NotificationChannel(objectId,name,importance);
            notificationChannel.setDescription("this is des");
            NotificationManager notificationManager=(NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder builder=new NotificationCompat.Builder(context,objectId);
        builder.setSmallIcon(R.drawable.caicon);
        builder.setContentTitle(username);
        builder.setContentText("sent file "+filename);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(notid,builder.build());

    }



}
