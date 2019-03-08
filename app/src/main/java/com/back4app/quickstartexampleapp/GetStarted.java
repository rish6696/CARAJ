package com.back4app.quickstartexampleapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

public class GetStarted extends AppCompatActivity implements View.OnKeyListener {
    Animation btt,bttdua,bttlga,imge,passduaa,forg,si;
    ImageView titile;
    TextView user;
    TextView password,forgot,sign;
    Button Login;
    ImageView man;
    ProgressBar pb;
    Button signup;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);
        ParseObject obj=new ParseObject("Score");
        obj.put("name","rishabh");
        obj.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e==null)
                {
                    Log.i("Information","done");
                }
                else{
                    Log.i("Information",e.getMessage());
                }
            }
        });

        btt=AnimationUtils.loadAnimation(this,R.anim.btt);
        bttdua=AnimationUtils.loadAnimation(this,R.anim.bttdua);
        bttlga=AnimationUtils.loadAnimation(this,R.anim.bttlga);
        imge=AnimationUtils.loadAnimation(this,R.anim.imgealp);
        passduaa=AnimationUtils.loadAnimation(this,R.anim.passdua);
        forg=AnimationUtils.loadAnimation(this,R.anim.fpani);
        si=AnimationUtils.loadAnimation(this,R.anim.signupani);



       // pb=(ProgressBar)findViewById(R.id.progbar);
      //  pb.setVisibility(View.INVISIBLE);
        titile=(ImageView)findViewById(R.id.caimage);
        password=(TextView)findViewById(R.id.password);
        user=(TextView)findViewById(R.id.username);
      //  signup=(Button)findViewById(R.id.SignUp);
        forgot=(TextView)findViewById(R.id.forgotpassword);
        sign=(TextView)findViewById(R.id.signup);
        Login=(Button)findViewById(R.id.login);
        man=(ImageView)findViewById(R.id.manimage);
        titile.setAnimation(btt);
        user.setAnimation(bttdua);
        Login.setAnimation(bttlga);
       // signup.setAnimation(bttlga);
        man.setAnimation(imge);
        password.setAnimation(passduaa);
        forgot.setAnimation(forg);
        sign.setAnimation(si);
        if (ParseUser.getCurrentUser()!=null)
        {
            if (ParseUser.getCurrentUser().getUsername().equals("admin"))
            {
                redirectoadminactivity();

            }
            else {
                redirectouseractivity();
            }

        }
        password.setOnKeyListener(this);

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
    public void LoGin(View view)
    {
        if (haveNetworkConnection()){
        dialog=new AlertDialog.Builder(this).create();
        pb=new ProgressBar(this);
        dialog.setView(pb);


           // pb.setVisibility(View.VISIBLE);

            if (user.getText().toString().length() > 0 && password.getText().toString().length() > 0) {
                dialog.show();
                ParseUser.logInInBackground(user.getText().toString(), password.getText().toString(), new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null && user != null) {
                            if (user.getUsername().equals("admin")) {
                                redirectoadminactivity();

                            } else {
                                redirectouseractivity();
                            }
                            dialog.dismiss();
                            Toast.makeText(GetStarted.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(GetStarted.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }
                });

            } else {
                Toast.makeText(this, "Username or Password cannot be empty", Toast.LENGTH_SHORT).show();
               // pb.setVisibility(View.INVISIBLE);

            }
        }
        else {
            Toast.makeText(this, "NO INTERNET", Toast.LENGTH_SHORT).show();
        }
    }
    private void redirectouseractivity() {
        Intent intent=new Intent(getApplicationContext(),AddFileActivity.class);
       // pb.setVisibility(View.INVISIBLE);
        startActivity(intent);
        finish();

    }

    private void redirectoadminactivity() {

        Intent intent=new Intent(getApplicationContext(),AdminActivity.class);
        startActivity(intent);
        finish();
    }
    public void SignUpProcess(View view)
    {

        startActivity(new Intent(getApplicationContext(),SignUpactivity.class));
        finish();

    }


    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (i==KeyEvent.KEYCODE_ENTER&&keyEvent.getAction()==KeyEvent.ACTION_DOWN)
        {
            LoGin(view);
        }
        return false;
    }
    public void Hidekeyboard(View view)
    {
        InputMethodManager inputMethodManager=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromInputMethod(getCurrentFocus().getWindowToken(),0);
    }

    public void ResetPassword(View view) {

        startActivity(new Intent(getApplicationContext(),ForgotPassword.class));
    }
}
