package com.back4app.quickstartexampleapp;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static com.parse.Parse.getApplicationContext;

public class RecAdapter extends RecyclerView.Adapter<RecAdapter.MyViewHolder> {
    ArrayList<String> SubjectList;
    ArrayList<String> filename;
    ArrayList<String> urlist;
    ArrayList<Boolean> imgefromcamera;
    ArrayList<Date> datelist;
    static Context context;


    public RecAdapter(ArrayList<String> filename,ArrayList<String>subjectList,ArrayList<String> urlist,ArrayList<Boolean>imagfromcamera,ArrayList<Date> datelist) {
        this.SubjectList=subjectList;
        this.filename=filename;
        this.urlist=urlist;
        this.imgefromcamera=imagfromcamera;
        this.datelist=datelist;


        }

    @NonNull
    @Override
    public RecAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.recyview_layout,parent,false);
        MyViewHolder myViewHolder=new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecAdapter.MyViewHolder holder, int position) {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat timeformat=new SimpleDateFormat("HH:mm:ss");
        holder.img.setText(SubjectList.get(position).charAt(0)+"");
        holder.img.setBackgroundColor(Getcolour());
        holder.subject.setText(SubjectList.get(position));
        holder.nameoffile.setText(filename.get(position));
        holder.date.setText(simpleDateFormat.format(datelist.get(position))+","+timeformat.format(datelist.get(position)));
        holder.personalurl=urlist.get(position);
        holder.fromcamera=imgefromcamera.get(position);
        holder.filename=filename.get(position);
        holder.url=urlist.get(position);


    }
    public int Getcolour(){
        Random random=new Random();
        return Color.argb(255,random.nextInt(256),random.nextInt(256),random.nextInt(256));

    }

    @Override
    public int getItemCount() {
        return filename.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView img,subject,nameoffile,date;
        String personalurl;
        Boolean fromcamera;
        String filename;
        String url;

        public MyViewHolder(final View itemView) {
            super(itemView);

            context=itemView.getContext();
           img=(TextView)itemView.findViewById(R.id.imagetext);
            subject=(TextView)itemView.findViewById(R.id.subjecttext);
            nameoffile=(TextView)itemView.findViewById(R.id.filenametext);
            date=(TextView)itemView.findViewById(R.id.datetext);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (haveNetworkConnection()) {

                        if (getextension(filename).equals("jpg") || getextension(filename).equals("jpeg") || getextension(filename).equals("png")) {
                            Documents.progressBar.setVisibility(View.VISIBLE);
                            Intent intent = new Intent(getApplicationContext(), ImageViewActivity.class);
                            intent.putExtra("url", personalurl);
                            intent.putExtra("fromcamera", fromcamera);
                            intent.putExtra("filename", filename);
                            intent.putExtra("useroradmin", "admin");
                            view.getContext().startActivity(intent);
                        } else {
                            downloadFile(itemView.getContext(), getname(filename) + ".", getextension(filename), DIRECTORY_DOWNLOADS, url);
                        }


                    }
                    else {
                        Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }
        private boolean haveNetworkConnection() {
            boolean haveConnectedWifi = false;
            boolean haveConnectedMobile = false;

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
    public String getextension(String filename)
    {
        return filename.substring(filename.lastIndexOf(".")+1);
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
    public String getname(String filename)
    {
        int m=filename.lastIndexOf(".");
        return filename.substring(0,m);
    }
}
