package com.back4app.quickstartexampleapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Documents extends AppCompatActivity {
    RecyclerView documentslist;
    ArrayList<String> filenames;
    ArrayList<String> urllist;
    ArrayList<String> subjects;
    ArrayList<Boolean> imgfromcamera;
    ArrayList<Date> datelist;
    static ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);
        UserAdapter.dialog.dismiss();
        progressBar=(ProgressBar)findViewById(R.id.progress_circular);
        progressBar.setVisibility(View.INVISIBLE);
        filenames=new ArrayList<>();
        datelist=new ArrayList<>();
        urllist=new ArrayList<>();
        subjects=new ArrayList<>();
        imgfromcamera=new ArrayList<>();
        documentslist=(RecyclerView) findViewById(R.id.recview);
        documentslist.setLayoutManager(new LinearLayoutManager(this));

        String user=getIntent().getStringExtra("user");
        ParseQuery<ParseObject> query=ParseQuery.getQuery(user);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects!=null&&e==null) {

                    if (objects.size() > 0) {

                        for (ParseObject object:objects)
                        {

                            filenames.add(object.getString("Filename"));
                            urllist.add(object.getString("url"));
                            subjects.add(object.getString("subject"));
                            imgfromcamera.add(object.getBoolean("imagefromcamera"));
                            datelist.add(object.getUpdatedAt());

                        }
                       // RecyclerView.LayoutManager layoutManager=new GridLayoutManager(Documents.this,2);
                       // documentslist.setLayoutManager(layoutManager);
                        Collections.reverse(filenames);
                        Collections.reverse(urllist);
                        Collections.reverse(subjects);
                        Collections.reverse(imgfromcamera);
                        Collections.reverse(datelist);
                        documentslist.setAdapter(new RecAdapter(filenames,subjects,urllist,imgfromcamera,datelist));
                        documentslist.addItemDecoration(new SpacesItemDecoration(10));






                    } else {
                        Toast.makeText(Documents.this, "No file found", Toast.LENGTH_SHORT).show();
                    }
                }


                else {
                    Toast.makeText(Documents.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }


            }
        });
//        documentslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent=new Intent(getApplicationContext(),Documentshower.class);
//                intent.putExtra("url",urllist.get(i));
//                intent.putExtra("filename",filenames.get(i));
//                startActivity(intent);
//            }
//        });


    }
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater=new MenuInflater(this);
        menuInflater.inflate(R.menu.mymenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.logout)
        {
            final AlertDialog alertDialog=new AlertDialog.Builder(this).create();
            ProgressBar pb=new ProgressBar(this);
            alertDialog.setView(pb);
            alertDialog.show();
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e==null)
                    {

                        Intent in=new Intent(getApplicationContext(),GetStarted.class);
                        alertDialog.dismiss();
                        startActivity(in);
                        finish();
                    }
                    else {
                        Toast.makeText(Documents.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return true;
        }
        return false;
    }





    public class Downlaod extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url;
            try {
                url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return bitmap;


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;


        }
    }
}
