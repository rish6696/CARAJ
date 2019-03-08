package com.back4app.quickstartexampleapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.ExifInterface;
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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.Calendar;
import java.util.List;

public class AddFileActivity extends AppCompatActivity {
    TextView filename;
    Button upload;
    Button select;
    String nameoffileselected;
    Uri pdfuri;
    FirebaseStorage firebaseStorage;
    ProgressDialog progressDialog;
    String currentImagepath=null;
    ImageView status;

    boolean imagefromcamera=false;
    SharedPreferences sharedPreferences;
    //static ProgressBar progressBar;


    RecyclerView UserHistoryview;
    ArrayList<String> filenames;
    ArrayList<String> urlists;
    ArrayList<String> subjects;
    ArrayList<Boolean> fromcamera;
    String subject="";
    ArrayList<String> defaulters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_file);
        setAlarmReminder();
        sharedPreferences=this.getSharedPreferences(getPackageName(),Context.MODE_PRIVATE);
        getlanguage();
        Log.i("virat",sharedPreferences.getString("language","novalue"));
        firebaseStorage = FirebaseStorage.getInstance();
        filename=(TextView)findViewById(R.id.filestatus);
        status=(ImageView)findViewById(R.id.statusicon);
        defaulters=new ArrayList<>();
        ParseQuery<ParseObject> query=ParseQuery.getQuery("shorlistedusers");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects!=null&&e==null)
                {
                    for (ParseObject object:objects)
                    {
                        defaulters.add(object.getString("username"));
                    }
                }
            }
        });
    }

    private void getlanguage() {
        if (sharedPreferences.getString("language","").length()==0)
        {
            new AlertDialog.Builder(this)
                    .setTitle("Reminder Language")
                    .setMessage("Select language for reminder")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("English", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sharedPreferences.edit().putString("language","English").apply();
                        }
                    })
                    .setNegativeButton("Hindi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sharedPreferences.edit().putString("language","Hindi").apply();
                        }
                    }).show();
        }
    }

    public void SelecFile(View view) {
        new AlertDialog.Builder(AddFileActivity.this)
                .setTitle("Select Mode")
                .setMessage("Add File From?")
                .setIcon(R.drawable.ic_alert)
                .setPositiveButton("Storage", new DialogInterface.OnClickListener() {
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

    public void UploadFile(View view) {

        if (pdfuri!=null) {
            final EditText text = new EditText(AddFileActivity.this);

            AlertDialog dialog = new AlertDialog.Builder(AddFileActivity.this)
                    .setTitle("File Upload")
                    .setMessage("Please enter the subject")
                    .setView(text)
                    .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            subject = text.getText().toString();
                            new AlertDialog.Builder(AddFileActivity.this)
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
            Toast.makeText(AddFileActivity.this, "Please Select File", Toast.LENGTH_SHORT).show();
        }

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 86 && resultCode == RESULT_OK && data != null) {
            pdfuri = data.getData();
            nameoffileselected=getFileName(pdfuri);
            filename.setText("File Selected successfully,\n Upload now");
            status.setImageResource(R.drawable.check);


            Log.i("PDFURI", pdfuri.toString());

        } else if (requestCode==856){

            Toast.makeText(this, "Image Captured,upload now", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Please Select File", Toast.LENGTH_SHORT).show();
        }
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
                filename.setText("File Selected successfully,\n Upload now");
                status.setImageResource(R.drawable.check);
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
            if (!defaulters.contains(ParseUser.getCurrentUser().getUsername())) {
                if (pdfuri != null) {
                    progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("Uploading File");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setProgress(0);
                    progressDialog.show();
                    final StorageReference storageReference = firebaseStorage.getReference();
                    UploadTask uploadTask;
                    uploadTask = storageReference.child("uploads").child(ParseUser.getCurrentUser().getUsername().toString()).child(nameoffileselected).putFile(pdfuri);
                    Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return storageReference.child("uploads").child(ParseUser.getCurrentUser().getUsername()).child(nameoffileselected).getDownloadUrl();
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
                                            filename.setText("NO FILE SELECTED");
                                            status.setImageResource(R.drawable.cancelicon);
                                            Toast.makeText(AddFileActivity.this, "Document Saved Successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(AddFileActivity.this, "Unable to upload,try again later or check the internet", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AddFileActivity.this, "Unable to upload try again Later", Toast.LENGTH_SHORT).show();

                        }
                    });


                } else {
                    Toast.makeText(this, "Select a file", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this, "You cannot Upload", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "Sorry,No internet", Toast.LENGTH_SHORT).show();
        }

    }
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.usermenu,menu);
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
                            Toast.makeText(AddFileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return true;
            }
            else {
                Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
            }
        }
        else if (item.getItemId()==R.id.history)
        {
            if (haveNetworkConnection()) {
                startActivity(new Intent(getApplicationContext(), Userhistoryactivity.class));
            }
            else {
                Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
            }
        }
        else if(item.getItemId()==R.id.reminder)
        {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("Select language for reminder")
                    .setTitle("Reminder Language")
                    .setPositiveButton("English", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sharedPreferences.edit().putString("language","English").apply();
                            Toast.makeText(AddFileActivity.this, "Language set to English", Toast.LENGTH_SHORT).show();


                        }
                    })
                    .setNegativeButton("Hindi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sharedPreferences.edit().putString("language","Hindi").apply();
                            Toast.makeText(AddFileActivity.this, "Language set to Hindi", Toast.LENGTH_SHORT).show();


                        }
                    }).show();
        }

        return false;
    }
    private void setAlarmReminder() {

//        Calendar calendar=Calendar.getInstance();
//        calendar.set(Calendar.DATE,12);
//        calendar.set(Calendar.MONTH,Calendar.FEBRUARY);
//        calendar.set(Calendar.YEAR,2019);
//        calendar.set(Calendar.HOUR_OF_DAY,2);
//        calendar.set(Calendar.MINUTE,52);
//
//
//        Intent intent = new Intent(getApplicationContext(), ReminderAlarm.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), 3* 1000, pendingIntent);

    }


    public int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath){
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            Log.i("RotateImage", "Exif orientation: " + orientation);
            Log.i("RotateImage", "Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }


}
