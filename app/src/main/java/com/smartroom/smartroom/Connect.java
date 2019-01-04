package com.smartroom.smartroom;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.DeviceCallback;
import me.aflak.bluetooth.DiscoveryCallback;

public class Connect extends AppCompatActivity {

    Switch light, fan;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect);
        light = findViewById(R.id.light);
        fan = findViewById(R.id.fan);
        startService(new Intent(this, Back_service.class));
        databaseReference = FirebaseDatabase.getInstance().getReference();

        light.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Intent intent = new Intent("light");
                intent.putExtra("data", b);
                sendBroadcast(intent);
            }
        });

        fan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Intent intent = new Intent("fan");
                intent.putExtra("data", b);
                sendBroadcast(intent);
            }
        });

        databaseReference.child("devices").child("light").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().equals("true")) {
                    light.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Connect.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
            }
        });

        databaseReference.child("devices").child("fan").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().equals("true")) {
                    fan.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Connect.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
