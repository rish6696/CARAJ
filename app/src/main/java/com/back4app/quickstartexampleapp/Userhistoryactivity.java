package com.back4app.quickstartexampleapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Userhistoryactivity extends AppCompatActivity {

    String nameoffileselected;
    Uri pdfuri;
    FirebaseStorage firebaseStorage;
    ProgressDialog progressDialog;
    String currentImagepath=null;
    boolean imagefromcamera=false;
    static ProgressBar progressBar;


    RecyclerView UserHistoryview;
    ArrayList<String> filenames;
    ArrayList<String> urlists;
    ArrayList<String> subjects;
    ArrayList<Boolean> fromcamera;
    String subject="";
    ArrayList<Date> datelist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userhistoryactivity);



        progressBar=(ProgressBar)findViewById(R.id.progress_circular);
        progressBar.setVisibility(View.INVISIBLE);
        filenames=new ArrayList<>();
        urlists=new ArrayList<>();
        subjects=new ArrayList<>();
        fromcamera=new ArrayList<>();
        datelist=new ArrayList<>();
        firebaseStorage = FirebaseStorage.getInstance();
        UserHistoryview=(RecyclerView)findViewById(R.id.userhistory);
        UserHistoryview.setLayoutManager(new LinearLayoutManager(this));

        ParseQuery<ParseObject> query=ParseQuery.getQuery(ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects!=null&&objects.size()>0)
                {
                    for (ParseObject object:objects)
                    {
                        filenames.add(object.getString("Filename"));
                        subjects.add(object.getString("subject"));
                        urlists.add(object.getString("url"));
                        fromcamera.add(object.getBoolean("imagefromcamera"));
                        datelist.add(object.getUpdatedAt());
                    }
                    Collections.reverse(filenames);
                    Collections.reverse(subjects);
                    Collections.reverse(urlists);
                    Collections.reverse(fromcamera);
                    UserHistoryview.addItemDecoration(new SpacesItemDecoration(10));

                    UserHistoryview.setAdapter(new HistoryAdapter(subjects,filenames,urlists,fromcamera,datelist));
                }
                if(objects.size()==0)
                {
                    Toast.makeText(Userhistoryactivity.this, "No files Uploaded", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setAlarmReminder() {

        Intent intent = new Intent(getApplicationContext(), ReminderAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(), 1* 1000, pendingIntent);

    }

    public void selectFile() {
        askforPermition();

    }
    private void askforPermition() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            proceedFile();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) {
            pdfuri = data.getData();
            nameoffileselected=getFileName(pdfuri);


            Log.i("PDFURI", pdfuri.toString());

        } else if (requestCode==856){

            Toast.makeText(this, "Image Captured,upload now", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Please Select File", Toast.LENGTH_SHORT).show();
        }
    }
    private void proceedFile() {


        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                proceedFile();

            }

        } else {
            Toast.makeText(this, "Permission is required to Upload Files", Toast.LENGTH_SHORT).show();
        }
    }
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    public void uploadFile() {
        if (haveNetworkConnection()) {
            if (pdfuri != null) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading File");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setProgress(0);
                progressDialog.show();
                final StorageReference storageReference = firebaseStorage.getReference();
                UploadTask uploadTask;
                uploadTask = storageReference.child("uploads").child(nameoffileselected).putFile(pdfuri);
                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return storageReference.child("uploads").child(nameoffileselected).getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {

                            Uri uri = task.getResult();
                            String m = uri.toString();
                            Log.i("INFORMATION", m);
                            ParseObject object = new ParseObject(ParseUser.getCurrentUser().getUsername());
                            ParseObject noti = new ParseObject("notificationList");
                            object.put("url", uri.toString());
                            object.put("Filename", nameoffileselected);
                            object.put("imagefromcamera", imagefromcamera);
                            object.put("subject", subject);
                            imagefromcamera = false;
                            // object.put("sunject","");
                            noti.put("notificationshown", false);
                            noti.put("UserName", ParseUser.getCurrentUser().getUsername());
                            noti.put("fileName", nameoffileselected);
                            noti.put("subject", subject);
                            noti.saveInBackground();
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        progressDialog.dismiss();
                                        pdfuri = null;
                                        Toast.makeText(Userhistoryactivity.this, "Document Saved Successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Userhistoryactivity.this, "Unable to upload,try again later or check the internet", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    }
                });
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int m = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setProgress(m);

                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Toast.makeText(Userhistoryactivity.this, "Unable to upload try again Later", Toast.LENGTH_SHORT).show();

                    }
                });


            } else {
                Toast.makeText(this, "Select a file", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "Sorry,No internet", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(Userhistoryactivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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


    public void OpenCamera()
    {


        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager())!=null)
        {
            File imagefile=null;
            try {
                imagefile=getfile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (imagefile!=null){
                Uri uri=FileProvider.getUriForFile(this,"com.example.android.fileprovider",imagefile);
                pdfuri=uri;
                imagefromcamera=true;
                nameoffileselected=Long.toString(System.currentTimeMillis())+".jpg";
               // filename.setText(nameoffileselected);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                startActivityForResult(intent,856);
            }
        }


    }
    private File getfile () throws IOException {
        String filename="jpg_mypic";
        File storageDir=getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagefile=File.createTempFile(filename,".jpg",storageDir);
        currentImagepath=imagefile.getAbsolutePath();


        Log.i("myfilename",currentImagepath);
        return imagefile;
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


    public void sel(View view) {
        new AlertDialog.Builder(Userhistoryactivity.this)
                            .setTitle("Select Mode")
                            .setMessage("Add File From?")
                            .setIcon(R.drawable.ic_alert)
                            .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    selectFile();

                                }
                            })
                            .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    OpenCamera();
                                }
                            })
                            .show();

    }

    public void up(View view) {
        if (pdfuri!=null) {
                        final EditText text = new EditText(Userhistoryactivity.this);

                        AlertDialog dialog = new AlertDialog.Builder(Userhistoryactivity.this)
                                .setTitle("File Upload")
                                .setMessage("Please enter the subject")
                                .setView(text)
                                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        subject = text.getText().toString();
                                        new AlertDialog.Builder(Userhistoryactivity.this)
                                                .setIcon(R.drawable.ic_alert)
                                                .setMessage("Yes I want to Upload")
                                                .setTitle("Are You Sure")
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        uploadFile();
                                                    }
                                                })
                                                .setNegativeButton("No",null)
                                                .show();

                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .setIcon(R.drawable.ic_alert)
                                .show();
                    }
                    else {
                        Toast.makeText(Userhistoryactivity.this, "Please Select File", Toast.LENGTH_SHORT).show();
                    }

    }
}





