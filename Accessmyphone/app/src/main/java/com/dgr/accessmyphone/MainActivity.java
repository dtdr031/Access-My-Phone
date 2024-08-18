package com.dgr.accessmyphone;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText e;
    Button setpin, start, stop;
    Uri uri;
    static int flag=0;
    static String pin;
    static ArrayList<String> names;
    static ArrayList<String> nos;
    TelephonyManager tel;
    static String imei, PhoneNumber;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.our_menus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.item1:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(android.R.drawable.star_big_on);
                builder.setTitle("How to use:");
                builder.setMessage("In app: set pin-> start service\n\nIn other phone:\n<pin> get imei - sends imei no.\n" +
                        "\n<pin> contact <contact name 'x'> - sends contact details whose name starts with 'x'\n" +
                        "\n<pin> pass - sends random new pin to the owner's phone\n\n<pin> pass <contact no.> - sends new pin to contact no.\n"+
                        "\n<pin> silent - changes sound to silent mode\n\n<pin> ring - changes sound to ring mode\n" +
                        "\n<pin> lock - lock screen remotely\n\n<pin> location - sends phone location");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "ok", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        names = new ArrayList<String>();
        nos = new ArrayList<String>();
        e = findViewById(R.id.editText);
        setpin = findViewById(R.id.button);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        setpin.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);

        // Check that the user hasn't revoked permissions by going to Settings.

        //for imei no
        tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_PHONE_STATE},100);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            imei = tel.getImei();
            PhoneNumber = tel.getLine1Number();
        }

        //for contacts and permissions
        uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.FOREGROUND_SERVICE}, 100);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},100);
        }
        //device admin
        if(flag==0) {
            DeviceAdminPermission();
            flag=1;
        }
        //contacts
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(uri, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " DESC");
        int nameColumn = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int phColumn = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        while (cur.moveToNext()) {
            String name = cur.getString(nameColumn);
            String mob = cur.getString(phColumn);
            names.add(name);
            nos.add(mob);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == setpin) {
            if(e.getText().toString().length() < 6){
                Toast.makeText(this, "Pin should be a minimum of 6 characters", Toast.LENGTH_SHORT).show();
            }
            else{
                pin = e.getText().toString();
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "PIN set successfully", Toast.LENGTH_SHORT).show();
                } else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
            }
        }
        if (v == start) {
            Intent i = new Intent(this, ReadMessageService.class);
            i.putExtra("inputExtra", "waiting for command");
            ContextCompat.startForegroundService(this,i);
        }
        if(v == stop){
            Intent i = new Intent(this, ReadMessageService.class);
            stopService(i);
        }
    }

    //Device admin permission
    private void DeviceAdminPermission() {
            ComponentName admin = new ComponentName(this, AdminReceiver.class);
            Intent intent2 = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
            startActivityForResult(intent2,1);
    }
}