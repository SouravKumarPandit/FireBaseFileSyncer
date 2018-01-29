package com.centrahub.focus.sourav.firebasefilesyncer.accountauth;


import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.centrahub.focus.sourav.firebasefilesyncer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

import static com.centrahub.focus.sourav.firebasefilesyncer.accountauth.AccountGeneral.USERDATA_USER_OBJ_ID;
import static com.centrahub.focus.sourav.firebasefilesyncer.accountauth.AccountGeneral.sServerAuthenticate;
import static com.centrahub.focus.sourav.firebasefilesyncer.accountauth.AccountSinginActivity.ARG_ACCOUNT_TYPE;
import static com.centrahub.focus.sourav.firebasefilesyncer.accountauth.AccountSinginActivity.KEY_ERROR_MESSAGE;
import static com.centrahub.focus.sourav.firebasefilesyncer.accountauth.AccountSinginActivity.PARAM_USER_PASS;

public class AccountSingupActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private String mAccountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_accountsingup);

        mAccountType = ARG_ACCOUNT_TYPE;

        getSupportActionBar().setTitle("SING UP");
        Button btSingIn = findViewById(R.id.account_sing_up);
        btSingIn.setOnClickListener(this);


        findViewById(R.id.user_is_old).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        TextView tv_olduser = (TextView) findViewById(R.id.user_is_old);
        SpannableString content = new SpannableString(getResources().getString(R.string.already_user_click_here));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tv_olduser.setText(content);
        tv_olduser.setOnClickListener(this);
        tv_olduser.setText(Html.fromHtml(getString(R.string.already_user_click_here)));
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onClick(View view) {


        EditText etEmail = findViewById(R.id.account_et_email);
        EditText etPassword = findViewById(R.id.account_et_password);
        EditText etName=findViewById(R.id.account_et_name);
        if (etEmail.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()||etName.getText().toString().isEmpty()) {
            Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            return;
        }

        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        final String name = etName.getText().toString().trim();


        switch (view.getId()) {
            case R.id.account_sing_up:


                // Validation!

                new AsyncTask<String, Void, Intent>() {

//                    String name = ((TextView) findViewById(R.id.name)).getText().toString().trim();
//                    String accountName = ((TextView) findViewById(R.id.et)).getText().toString().trim();
//                    String accountPassword = ((TextView) findViewById(R.id.accountPassword)).getText().toString().trim();

                    @Override
                    protected Intent doInBackground(String... params) {

//                        Log.d("udinic", TAG + "> Started authenticating");

                        String authtoken = null;
                        Bundle data = new Bundle();
                        try {
                            FirebaseUser user = sServerAuthenticate.userSignUp(AccountSingupActivity.this,name, email, password, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
                            if (user != null){
                                authtoken = user.getUid();
                                data.putString(AccountManager.KEY_ACCOUNT_NAME, name);
                            }
                            data.putString(AccountManager.KEY_AUTHTOKEN, name+ Calendar.getInstance().getTime());
                            data.putString(AccountManager.KEY_ACCOUNT_TYPE, mAccountType);


                            // We keep the user's object id as an extra data on the account.
                            // It's used later for determine ACL for the data we send to the Parse.com service
                            Bundle userData = new Bundle();
                            userData.putString(USERDATA_USER_OBJ_ID, user != null ? user.getEmail() : null);
                            data.putBundle(AccountManager.KEY_USERDATA, userData);

                            data.putString(PARAM_USER_PASS, password);
                        } catch (Exception e) {
                            data.putString(KEY_ERROR_MESSAGE, e.getMessage());
                        }

                        final Intent res = new Intent();
                        res.putExtras(data);
                        return res;
                    }

                    @Override
                    protected void onPostExecute(Intent intent) {
                        if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
                            Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE)+"SIGN UP ! ERROR", Toast.LENGTH_SHORT).show();
                        } else {
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                }.execute();

                break;
            case R.id.user_is_old:
//                Intent oldUser=new Intent(AccountSingupActivity.this,AccountSinginActivity.class);
                setResult(RESULT_CANCELED);
                finish();
//                startActivity(oldUser);
//                finish();
                break;
        }

    }
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
