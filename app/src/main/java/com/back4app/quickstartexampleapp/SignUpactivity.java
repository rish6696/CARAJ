package com.back4app.quickstartexampleapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpactivity extends AppCompatActivity implements View.OnKeyListener {

    TextView username,password,emailid,reenterpassword;
    Button signupbutton;
    static AlertDialog dialog;
    ProgressBar bar;
    Animation iconani,userani,emailani,passani,renani,imageani,signupani;
    ImageView woman,ca;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_upactivity);

        username=(TextView)findViewById(R.id.signupusername);
        password=(TextView)findViewById(R.id.signuppassword);
        emailid=(TextView)findViewById(R.id.signupemailid);
        signupbutton=(Button)findViewById(R.id.signupsucess);
        reenterpassword=(TextView)findViewById(R.id.signupreenterpass);
        reenterpassword.setOnKeyListener(this);
        woman=(ImageView)findViewById(R.id.wo);
        ca=(ImageView)findViewById(R.id.caicon);


        iconani=AnimationUtils.loadAnimation(this,R.anim.btt);
        imageani=AnimationUtils.loadAnimation(this,R.anim.imgealp);
        userani=AnimationUtils.loadAnimation(this,R.anim.bttdua);
        emailani=AnimationUtils.loadAnimation(this,R.anim.ano);
        passani=AnimationUtils.loadAnimation(this,R.anim.passdua);
        renani=AnimationUtils.loadAnimation(this,R.anim.fpani);
        signupani=AnimationUtils.loadAnimation(this,R.anim.bttlga);





        woman.setAnimation(imageani);
        ca.setAnimation(iconani);
        username.setAnimation(userani);
        password.setAnimation(passani);
        reenterpassword.setAnimation(renani);
        emailid.setAnimation(emailani);
        signupbutton.setAnimation(signupani);


    }
    public void SignUp(View view)
    {
        if (haveNetworkConnection()) {
            bar = new ProgressBar(this, null, android.R.attr.progressBarStyle);
            dialog = new AlertDialog.Builder(this).create();
            dialog.setView(bar);
            dialog.show();


            if (reenterpassword.getText().toString().equals(password.getText().toString())) {
                ParseUser parseUser = new ParseUser();
                parseUser.setUsername(username.getText().toString());
                parseUser.setEmail(emailid.getText().toString());
                parseUser.setPassword(password.getText().toString());
                parseUser.put("isavaliduser",true);
                parseUser.saveInBackground();
                parseUser.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(SignUpactivity.this, "SignUp successfull", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            startActivity(new Intent(getApplicationContext(), GetStarted.class));
                            finish();

                        } else {
                            dialog.dismiss();
                            Toast.makeText(SignUpactivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            } else {
                Toast.makeText(this, "Password Does Not Match", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "Soory,No internet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (i==KeyEvent.KEYCODE_ENTER&&keyEvent.getAction()==KeyEvent.ACTION_DOWN)
        {
            SignUp(view);
        }
        return false;
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
