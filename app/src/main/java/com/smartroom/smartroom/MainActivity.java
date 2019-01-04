package com.smartroom.smartroom;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import me.aflak.bluetooth.Bluetooth;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private SignInButton signin;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ProgressDialog progressdialog;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signin = findViewById(R.id.signin);
        configure();
        signin.setOnClickListener(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    startActivity(new Intent(MainActivity.this, Connect.class));
                    finish();
                }
            }
        };
        progressdialog = new ProgressDialog(this);
        progressdialog.setTitle("Logging In");
        progressdialog.setCancelable(false);
        progressdialog.setCanceledOnTouchOutside(false);
        progressdialog.setMessage("Please wait...");
    }

    private void configure() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Something is not right ...", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        progressdialog.show();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // Google Sign In was successful, save Token and a state then authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();


                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    firebaseAuthWithGoogle(credential);

            } else {
                // Google Sign In failed, update UI appropriately
                //Log.e("ABHI", "Login Unsuccessful. ");
                Toast.makeText(this, "Login Unsuccessful", Toast.LENGTH_SHORT).show();
                progressdialog.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(final AuthCredential credential) {

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {

                    Toast.makeText(MainActivity.this, "Authentication failed..", Toast.LENGTH_SHORT).show();

                } else {
                    //createUserInFirebase();

                        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        databaseReference.child("devices").child("light").setValue(false);
                        databaseReference.child("devices").child("fan").setValue(false);

                    progressdialog.dismiss();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Authentication failed..", Toast.LENGTH_SHORT).show();
                progressdialog.dismiss();
            }
        });
    }

}

