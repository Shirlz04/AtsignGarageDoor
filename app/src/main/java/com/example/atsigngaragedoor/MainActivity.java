package com.example.atsigngaragedoor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.atsign.common.AtException;
import org.atsign.common.NoSuchSecondaryException;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;

public class MainActivity extends AppCompatActivity {

    Button button;
    EditText editText;
    Thread thread;
    String atsignName;
    AlertDialog.Builder builder;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        button = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editTextMain);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                    atsignName = editText.getText().toString();
                    if(atsignName.compareTo("@acidrock20") == 0) {
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.show();
                        progressDialog.setContentView(R.layout.progessbar);
                        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        ConnectivityManager connMgr = (ConnectivityManager)
                                getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                        if (networkInfo != null && networkInfo.isConnected()) {
                            thread = new Thread(runnable);
                            thread.start();
                        } else {
                            Toast.makeText(getApplicationContext(), "No network connection available", Toast.LENGTH_SHORT);
                        }
                    } else{
                        builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Alert!!").setMessage("AtSign not found please try again!")
                                .setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                    }
                    return true;
                }
                return false;
            }
        });
        //System.out.println("string entered is: "+atsignName);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atsignName = editText.getText().toString();
                if(atsignName.compareTo("@acidrock20") == 0) {
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.show();
                    progressDialog.setContentView(R.layout.progessbar);
                    progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    ConnectivityManager connMgr = (ConnectivityManager)
                            getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {
                        thread = new Thread(runnable);
                        thread.start();
                    } else {
                        Toast.makeText(getApplicationContext(), "No network connection available", Toast.LENGTH_SHORT);

                    }
                } else{
                    builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Alert!!").setMessage("AtSign not found please try again!")
                            .setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        progressDialog.dismiss();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message message) {
            Intent intent = new Intent(getBaseContext(),GarageDetails.class);
            intent.putExtra("keyName",atsignName);
            startActivity(intent);
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                handler.sendEmptyMessage(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}