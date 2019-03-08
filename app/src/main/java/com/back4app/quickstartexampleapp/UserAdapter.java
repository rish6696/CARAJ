package com.back4app.quickstartexampleapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserAdapter extends RecyclerView.Adapter <UserAdapter.UserHolder> {
    ArrayList<String> usernames;

    static AlertDialog dialog;

    public UserAdapter(ArrayList<String> usernames) {
        this.usernames = usernames;

    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.userrecview,parent,false);
        UserHolder userHolder=new UserHolder(view);
        return userHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        String name=usernames.get(position);
        char cc=name.charAt(0);
        if (cc>=97&&cc<=122) {
            int v = cc - 32;
            cc = (char) v;
        }
        holder.position=position;
        holder.nameicon.setText(cc+"");
        holder.nameicon.setBackgroundColor(Getcolour());
        holder.Namefield.setText(cc+name.substring(1));
        holder.name=usernames.get(position);


    }
    public int Getcolour(){
        Random random=new Random();
        return Color.argb(255,random.nextInt(256),random.nextInt(256),random.nextInt(256));

    }





    @Override
    public int getItemCount() {
        return usernames.size();
    }

    public class UserHolder extends RecyclerView.ViewHolder{

        TextView nameicon,Namefield;
        String name;

        ProgressBar pb;
        Context context;
        int position;

        public UserHolder(View itemView) {
            super(itemView);
            nameicon=(TextView)itemView.findViewById(R.id.imageperson);

            Namefield=(TextView)itemView.findViewById(R.id.nameid);
            context=itemView.getContext();
            dialog=new AlertDialog.Builder(itemView.getContext()).create();
            pb=new ProgressBar(itemView.getContext());
            dialog.setView(pb);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (haveNetworkConnection()) {
                        dialog.show();
                        Intent intent = new Intent(view.getContext(), Documents.class);
                        intent.putExtra("user", name);
                        view.getContext().startActivity(intent);


                    }
                    else {
                        Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(context)
                            .setTitle("Remove User")
                            .setMessage("Are you sure you want to remove "+name+" from user's list")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    removefromlist(position);
                                    shortlistuser();
                                }
                            }).setNegativeButton("No",null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return true;

                }
            });



        }
        public void removefromlist(int position)
        {
            usernames.remove(position);
            notifyItemRemoved(position);
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
        public void shortlistuser()
        {
            ParseObject object=new ParseObject("shorlistedusers");
            object.put("username",name);
            object.saveInBackground(new SaveCallback() {

                @Override
                public void done(ParseException e) {
                    if (e==null)
                    {
                        Toast.makeText(context, "User Shorlisted", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }
    public  void updateList(List<String> newList)
    {
        usernames=new ArrayList<>();
        usernames.addAll(newList);
        notifyDataSetChanged();
    }

}
