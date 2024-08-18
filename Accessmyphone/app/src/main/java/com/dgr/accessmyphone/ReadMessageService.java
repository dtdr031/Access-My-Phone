package com.dgr.accessmyphone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class ReadMessageService extends Service{
    private static final String TAG = ReadMessageService.class.getSimpleName();
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private MyMessageReceiver smsreceiver = null;
    private LocationRequest loc_req;
    private Location loc;

    private FusedLocationProviderClient FusedLocationClient;

    private LocationCallback LocationCallback;

    private Handler ServiceHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Access enabled")
                .setContentText(input)
                .setSmallIcon(R.drawable.applogo)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();
        smsreceiver = new MyMessageReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsreceiver, intentFilter);
        intentFilter.setPriority(100);
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
        Log.d("SmsReceiver", "MyMessageReceiver is registered.");
        FusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        ServiceHandler = new Handler(handlerThread.getLooper());
        requestLocationUpdates();
    }

    private void createLocationRequest() {
        loc_req = new LocationRequest();
        loc_req.setInterval(10000);
        loc_req.setFastestInterval(5000);
        loc_req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location);

        loc = location;
    }

    private void getLastLocation() {
        try {
            FusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                loc = task.getResult();
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");
        LocationInfo.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), ReadMessageService.class));
        try {
            FusedLocationClient.requestLocationUpdates(loc_req,
                    LocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            LocationInfo.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (smsreceiver != null) {
            unregisterReceiver(smsreceiver);
            Log.d("SmsReceiver", "MyMessageReceiver is unregistered.");
            Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
        }
        removeLocationUpdates();
        ServiceHandler.removeCallbacksAndMessages(null);
    }

    private void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            FusedLocationClient.removeLocationUpdates(LocationCallback);
            LocationInfo.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            LocationInfo.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    //Broadcast receiver to receive and exceute commands from sms
    public class MyMessageReceiver extends BroadcastReceiver {
        private final String TAG = this.getClass().getSimpleName();
        int flag;
        AudioManager am;
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            String strMessage = "";
            am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            flag=-1;
            if ( extras != null ) {
                Object[] smsextras = (Object[]) extras.get("pdus");
                for (int i = 0; i < smsextras.length; i++) {
                    SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) smsextras[i]);
                    String strMsgBody = smsmsg.getMessageBody();
                    String strMsgSrc = smsmsg.getOriginatingAddress();
                    String[] MsgSplit = strMsgBody.split(" ", 3);
                    String mpin = MsgSplit[0];
                    String command="";
                    String Name="";
                    String reply="";
                    String temp="";
                    if (MainActivity.pin.equals(mpin) && mpin!=null) {
                        flag=0;
                        if(MsgSplit.length>1)
                            command = MsgSplit[1];
                        if(MsgSplit.length>2)
                            Name = MsgSplit[2].toLowerCase();

                        //retrieve contacts
                        if (command.equals("contact") && MsgSplit.length>2) {
                            flag = 2;
                            for (int j = 0; j < MainActivity.names.size(); j++) {
                                if (MainActivity.names.get(j).toLowerCase().startsWith(Name) && !reply.contains(MainActivity.nos.get(j))) {
                                    flag = 1;
                                    reply += MainActivity.names.get(j) + ": " + MainActivity.nos.get(j) + "\n";
                                    if(reply.length() < 160){
                                        temp = reply;
                                    }
                                    else{
                                        SmsManager.getDefault().sendTextMessage(strMsgSrc, null, temp, null, null);
                                        reply= MainActivity.names.get(j) + ": " + MainActivity.nos.get(j) + "\n";
                                    }
                                }
                            }
                        }

                        //send contacts
                        if (flag == 1) {
                            Toast.makeText(context, reply, Toast.LENGTH_SHORT).show();
                            SmsManager.getDefault().sendTextMessage(strMsgSrc, null, reply, null, null);
                        }
                        else if (flag == 2) {
                            SmsManager.getDefault().sendTextMessage(strMsgSrc, null, "Contact not found", null, null);
                        }
                        //retreive imei
                        if(command.equals("get") && Name.equals("imei")){
                            flag=-1;
                            SmsManager.getDefault().sendTextMessage(strMsgSrc, null, "IMEI: "+ MainActivity.imei, null, null);
                        }

                        //pin change
                        if(command.equals("pass")) {
                            flag = -1;
                            MainActivity.pin = String.valueOf(Math.random()).substring(2, 8);
                            if (MsgSplit.length == 2) {
                                SmsManager.getDefault().sendTextMessage(MainActivity.PhoneNumber, null, "Your new pin: " + MainActivity.pin, null, null);
                                SmsManager.getDefault().sendTextMessage(strMsgSrc, null, "Pin changed sucessfully", null, null);
                            }
                            if (MsgSplit.length == 3){
                                SmsManager.getDefault().sendTextMessage(Name, null, "Your new pin: "+MainActivity.pin, null, null);
                                SmsManager.getDefault().sendTextMessage(strMsgSrc, null, "New pin info sent to "+Name, null, null);
                            }
                        }

                        //sound profile change
                        if(command.equals("silent")) {
                            flag = -1;
                            am.setRingerMode(1);
                            SmsManager.getDefault().sendTextMessage(strMsgSrc, null, "In silent mode", null, null);
                        }
                        if(command.equals("ring")) {
                            flag = -1;
                            am.setRingerMode(2);
                            SmsManager.getDefault().sendTextMessage(strMsgSrc, null, "In ring mode", null, null);
                        }

                        //lockscreen
                        if(command.equals("lock")){
                            flag = -1;
                            lock();
                            SmsManager.getDefault().sendTextMessage(strMsgSrc, null, "Screen locked", null, null);
                        }

                        //location
                        if(command.equals("location")){
                            flag = -1;
                            SmsManager.getDefault().sendTextMessage(strMsgSrc, null, LocationInfo.getLocationText(loc), null, null);
                        }



                        if (flag == 0) {
                            SmsManager.getDefault().sendTextMessage(strMsgSrc, null, "invalid command", null, null);
                        }
                    }

                }

                Log.i(TAG, strMessage);
            }
        }

        //To lock screen
    private void lock() {
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        if (pm.isInteractive()) {
            DevicePolicyManager policy = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            try {
                policy.lockNow();
            } catch (SecurityException ex) {
                Toast.makeText(ReadMessageService.this, "must enable device administrator", Toast.LENGTH_LONG).show();
                ComponentName admin = new ComponentName(ReadMessageService.this, AdminReceiver.class);
                Intent intent2 = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
                startActivity(intent2);
            }
        }
    }
}
}