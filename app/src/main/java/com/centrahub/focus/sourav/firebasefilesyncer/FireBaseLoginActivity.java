package com.centrahub.focus.sourav.firebasefilesyncer;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.List;

public class FireBaseLoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 1111;
    private static final int RC_SIGN__OTP = 1000;
    private FirebaseAuth mAuth;
    private String TAG = "ACCOUNT_EXAMPLE";
    private GoogleSignInOptions gso;
    private String mVerificationId;
    private EditText fieldVerificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        // Configure Google Sign In
        mAuth = FirebaseAuth.getInstance();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        setContentView(R.layout.activity_main);


        Button addAccount = findViewById(R.id.add_account);
        addAccount.setOnClickListener(this);
        Button btPhoneVarif = findViewById(R.id.phone_number);
        btPhoneVarif.setOnClickListener(this);

        Button btSingIn = findViewById(R.id.sing_in);
        btSingIn.setOnClickListener(this);

        Button btSingInWithGoogle = findViewById(R.id.sing_in_with);
        btSingInWithGoogle.setOnClickListener(this);


        Button btVerifyOtp = findViewById(R.id.bt_verify);
        btVerifyOtp.setOnClickListener(this);

        fieldVerificationCode = findViewById(R.id.et_otpnumber);

        Button btLogout = findViewById(R.id.logout);
        btLogout.setOnClickListener(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent fileMakerIntent = new Intent(FireBaseLoginActivity.this, FileMakerActivity.class);
            startActivity(fileMakerIntent);
            updateUI(currentUser);
        }

    }

    private void updateUI(FirebaseUser currentUser) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            List<? extends UserInfo> token = user.getProviderData();
            Uri photoUrl = user.getPhotoUrl();
            String uid = user.getUid();

            TextView updateView = findViewById(R.id.user_info);
            updateView.setText("name - \n" + name + " \n email -  \n" + email + "   \n User Id  :\n" + uid);
            // Check if user's email is verified
//            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
        } else {
            TextView updateView = findViewById(R.id.user_info);
            updateView.setText("null");
        }

    }


    @Override
    public void onClick(View view) {


        EditText etEmail = findViewById(R.id.et_email);
        EditText etNumber = findViewById(R.id.et_number);
        EditText etPassword = findViewById(R.id.et_password);
        if (etEmail.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            return;
        }

        final String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();


        switch (view.getId()) {
            case R.id.sing_in_with:
//                signIn();
                break;
            case R.id.add_account:

                //mAuth.confirmPasswordReset()

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    Toast.makeText(FireBaseLoginActivity.this, "Adding Success.", Toast.LENGTH_SHORT).show();

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(FireBaseLoginActivity.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }

                                // ...
                            }
                        });
                break;

            case R.id.sing_in:

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(FireBaseLoginActivity.this, "Authentication Success.", Toast.LENGTH_SHORT).show();
                                    updateUI(user);
                                    Intent fileMakerIntent = new Intent(FireBaseLoginActivity.this, FileMakerActivity.class);
                                    startActivity(fileMakerIntent);

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(FireBaseLoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }

                                // ...
                            }
                        });
                break;
            case R.id.logout:

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.signOut();
                updateUI(null);

                break;
            case R.id.phone_number:


              /*  PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        etNumber.getText().toString(),        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        this,               // Activity (for callback binding)
                        mCallbacks);        // OnVerificationStateChangedCallbacks
                */

                break;
            case R.id.bt_verify:
                String code = fieldVerificationCode.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    fieldVerificationCode.setError("Cannot be empty.");
                    return;
                }
                break;

        }

    }
}
