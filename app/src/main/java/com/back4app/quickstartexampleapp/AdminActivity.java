package com.back4app.quickstartexampleapp;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.app.SearchManager;
import android.widget.ProgressBar;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRole;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ArrayList<String> UsersList;

    RecyclerView MyUsersView;
    UserAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);


        Intent intent = new Intent(getApplicationContext(), MyAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(), 1* 1000, pendingIntent);
        MyUsersView=(RecyclerView)findViewById(R.id.userrecview);
        MyUsersView.setLayoutManager(new LinearLayoutManager(AdminActivity.this));
        UsersList=new ArrayList<>();

        ParseQuery<ParseUser> query=ParseUser.getQuery();
        query.addAscendingOrder("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (objects!=null&&objects.size()>0&&e==null)
                {
                    for (ParseObject object:objects)
                    {
                        UsersList.add(object.getString("username"));

                    }
                    adapter=new UserAdapter(UsersList);
                    MyUsersView.setAdapter(adapter);
                    MyUsersView.addItemDecoration(new SpacesItemDecoration(10));




                }
                else {
                    Toast.makeText(AdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });





    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

//        MenuInflater menuInflater=new MenuInflater(this);
//        menuInflater.inflate(R.menu.searchmenu,menu);
        getMenuInflater().inflate(R.menu.searchmenu,menu);
        MenuItem menuItem=menu.findItem(R.id.searchid);
        SearchView searchView=(SearchView)menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         if (item.getItemId()==R.id.logout) {
             if (haveNetworkConnection()) {
                 final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                 ProgressBar pb = new ProgressBar(this);
                 alertDialog.setView(pb);
                 alertDialog.show();
                 ParseUser.logOutInBackground(new LogOutCallback() {
                     @Override
                     public void done(ParseException e) {
                         if (e == null) {
                             Intent in = new Intent(getApplicationContext(), GetStarted.class);
                             alertDialog.dismiss();
                             startActivity(in);
                             finish();
                         } else {
                             Toast.makeText(AdminActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                         }
                     }
                 });
                 return true;
             }
             else {
                 Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
             }
         }

         return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {

        String userinput=s.toLowerCase();
        List<String> newlist=new ArrayList<>();
        for (String name:UsersList)
        {
            if (name.toLowerCase().contains(userinput))
            {
                newlist.add(name);
            }
        }
        adapter.updateList(newlist);

        return true;
    }
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
