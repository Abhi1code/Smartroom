package com.smartroom.smartroom;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.DeviceCallback;

import static com.smartroom.smartroom.App.CHANNEL_ID;

public class Back_service extends Service {

    public NotificationCompat.Builder builder;
    public Notification notification;
    DatabaseReference databaseReference;
    Bluetooth bluetooth;
    BroadcastReceiver light, fan;
    private String prev_data = "Smart Room is listening to bluetooth events";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        bluetooth = new Bluetooth(getApplicationContext());
        bluetooth.onStart();
        bluetooth.enable();
        if (bluetooth.isConnected()){
            bluetooth.disconnect();
        }
        bluetooth.startScanning();
        bluetooth.connectToName("XBOTS");
        //Log.d("ABHI", "service called");
        bluetooth.setDeviceCallback(new DeviceCallback() {
            @Override
            public void onDeviceConnected(BluetoothDevice device) {
                //Log.d("ABHI", device.getAddress() + "  " + device.getName());
                //bluetooth.send("aaa");
            }

            @Override
            public void onDeviceDisconnected(BluetoothDevice device, String message) {
            }

            @Override
            public void onMessage(String message) {
            }

            @Override
            public void onError(String message) {
            }

            @Override
            public void onConnectError(BluetoothDevice device, String message) {
            }
        });

        light = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getBooleanExtra("data", false)) {
                    if (bluetooth.isConnected()) {
                        bluetooth.send("lighttrue,sfvsv");
                        databaseReference.child("devices").child("light").setValue("true");
                    } else {
                        Toast.makeText(context, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (bluetooth.isConnected()) {
                        bluetooth.send("lightfalse,dwfcadw");
                        databaseReference.child("devices").child("light").setValue("false");
                    } else {
                        //stopSelf();
                        Toast.makeText(context, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        registerReceiver(light, new IntentFilter("light"));

        fan = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getBooleanExtra("data", false)) {
                    if (bluetooth.isConnected()) {
                        bluetooth.send("fantrue,sdfvdw");
                        databaseReference.child("devices").child("fan").setValue("true");
                    } else {
                        Toast.makeText(context, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (bluetooth.isConnected()) {
                        bluetooth.send("fanfalse,qefqef");
                        databaseReference.child("devices").child("fan").setValue("false");
                    } else {
                        //stopSelf();
                        Toast.makeText(context, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        registerReceiver(fan, new IntentFilter("fan"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notify_intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notify_intent, 0);

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SmartRoom is running")
                .setContentText(prev_data)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setColor(Color.parseColor("#104E8B"))
                .setPriority(Notification.PRIORITY_LOW);

        notification = builder.build();

        startForeground(1, notification);

        databaseReference.child("devices").child("light").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (bluetooth.isConnected()) {
                    if (dataSnapshot.getValue().equals("true")) {
                        bluetooth.send("lighttrue,vss");
                    } else {
                        bluetooth.send("lightfalse,weaf");
                    }
                } else {
                    //stopSelf();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Back_service.this, "connection problem", Toast.LENGTH_SHORT).show();
            }
        });

        databaseReference.child("devices").child("fan").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (bluetooth.isConnected()) {
                    if (dataSnapshot.getValue().equals("true")) {
                        bluetooth.send("fantrue,rsgv");
                    } else {
                        bluetooth.send("fanfalse,waeg");
                    }
                } else {
                    //stopSelf();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Back_service.this, "connection problem", Toast.LENGTH_SHORT).show();
            }
        });

        enable_offline();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (bluetooth.isConnected()) {
            bluetooth.disconnect();
        }
        stopSelf();
        ContextCompat.startForegroundService(this, new Intent(this, Back_service.class));
        super.onDestroy();
    }

    public void enable_offline() {
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    //Log.d(TAG, "connected");
                    databaseReference.child("devices").child("status").setValue("true");
                    databaseReference.child("devices").child("status").onDisconnect().setValue("false");
                } else {
                    Log.d("ABHI", "Not connected");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Log.w(TAG, "Listener was cancelled");
            }
        });

        //Write a string when this client loses connection
        databaseReference.child("devices").child("status").onDisconnect().setValue("false");

    }
}
