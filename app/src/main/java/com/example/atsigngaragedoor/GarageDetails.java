package com.example.atsigngaragedoor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.atsign.common.AtException;
import org.atsign.common.NoSuchSecondaryException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;


public class GarageDetails extends AppCompatActivity {

    ImageButton openDoor;
    ImageButton closeDoor;
    FragmentManager manager;
    DataHandler dataHandler;
    String atSignLogin;
    AlertDialog.Builder builder;
    TextView status;
    ImageView imageDoor;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_garage_details);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        openDoor = findViewById(R.id.imageButton);
        closeDoor = findViewById(R.id.imageButton2);
        manager = getSupportFragmentManager();
        status = (TextView) findViewById(R.id.textGarageStatusFlip);
        imageDoor = findViewById(R.id.imageStatus);
        atSignLogin = getIntent().getStringExtra("keyName");
        String keyString  = "";
        FileOutputStream fileOutputStream = null;
        //builder = new AlertDialog.Builder(this);

        try {
            InputStream inputStream = getAssets().open("@acidrock20_key.atKeys");
            int size = inputStream.available();
            byte[] bytes = new byte[size];
            inputStream.read(bytes);

            keyString = new String(bytes);
            fileOutputStream = openFileOutput("data.txt",MODE_PRIVATE);

        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("login is : "+atSignLogin);
        try {
            FileInputStream fileInputStream = openFileInput("data.txt");
            dataHandler = new DataHandler(atSignLogin,
                    "@1capricorn",keyString, fileOutputStream, fileInputStream);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (AtException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        openDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder = new AlertDialog.Builder(GarageDetails.this);
                builder.setTitle("Alert!!").setMessage("Garage door is open!")
                        .setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                try {
                    if (dataHandler.getStatus().compareTo("1") ==0){
                        builder = new AlertDialog.Builder(GarageDetails.this);
                        builder.setTitle("Alert!!").setMessage("Garage door is already open!")
                                .setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                    } else {
                        dataHandler.putSharedKey("1");
                        status.setText("opened");
                        imageDoor.setImageResource(R.drawable.open);

                    }
                } catch (IOException | NoSuchSecondaryException | ExecutionException

                        | InterruptedException e) {
                    e.printStackTrace();
                }
                //progressDialog.dismiss();
            }
        });
        closeDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder = new AlertDialog.Builder(GarageDetails.this);
                builder.setTitle("Alert!!").setMessage("Garage door is closed!")
                        .setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                try {
                    if (dataHandler.getStatus().compareTo("2") ==0){
                        builder = new AlertDialog.Builder(GarageDetails.this);
                        builder.setTitle("Alert!!").setMessage("Garage door is already closed!")
                                .setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                    } else {
                        dataHandler.putSharedKey("2");
                        status.setText("closed");
                        imageDoor.setImageResource(R.drawable.close);

                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchSecondaryException e) {
                    e.printStackTrace();
                }
            }
        });
        /*
        GarageDoorStatus status = new GarageDoorStatus();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment,status).commit();*/

    }
}