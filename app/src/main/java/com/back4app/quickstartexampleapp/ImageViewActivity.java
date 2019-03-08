package com.back4app.quickstartexampleapp;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class ImageViewActivity extends AppCompatActivity {
    ImageView imageView;
    String url;
    Boolean frmcamera;
    String filename;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        if (getIntent().getStringExtra("useroradmin").equals("admin")) {
            Documents.progressBar.setVisibility(View.INVISIBLE);
        }
        else {
           // Userhistoryactivity.progressBar.setVisibility(View.INVISIBLE);
            Userhistoryactivity.progressBar.setVisibility(View.INVISIBLE);
        }

        imageView = findViewById(R.id.imagefit);
        url = getIntent().getStringExtra("url");
        filename=getIntent().getStringExtra("filename");
        frmcamera=getIntent().getBooleanExtra("fromcamera",false);

        Downlaod downlaod=new Downlaod();
        try {
            Bitmap map=downlaod.execute(url).get();
            //HistoryAdapter.dialog.dismiss();
            if (frmcamera) {
                map = rotateBitmap(map);
            }
            imageView.setImageBitmap(map);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

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

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

        }
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
                        Toast.makeText(ImageViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return true;
        }
        return false;
    }
    public Bitmap rotateBitmap(Bitmap bitmap)
    {
        Matrix matrix=new Matrix();
        matrix.postRotate(-90);
        Bitmap rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return rotated;
    }
    public void sharefile(View view)
    {
        String shareBody=url;
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,filename);
        intent.putExtra(Intent.EXTRA_TEXT,shareBody);
        startActivity(Intent.createChooser(intent,"Sharevia"));
    }
    public void Downtodevice(View view)
    {
//        Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(url));
//        startActivity(intent);

        downloadFile(ImageViewActivity.this,getname(filename)+".",getextension(filename),DIRECTORY_DOWNLOADS,url);

    }
        public void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {

        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        Log.i("mukabla",uri.toString());
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);

        downloadmanager.enqueue(request);
    }
    public String getextension(String filename)
    {
        return filename.substring(filename.lastIndexOf(".")+1);
    }
    public String getname(String filename)
    {
        int m=filename.lastIndexOf(".");
        return filename.substring(0,m);
    }


}
