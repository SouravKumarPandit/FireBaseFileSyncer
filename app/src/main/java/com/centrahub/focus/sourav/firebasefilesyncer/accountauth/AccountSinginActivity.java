package com.centrahub.focus.sourav.firebasefilesyncer.accountauth;


import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import static com.centrahub.focus.sourav.firebasefilesyncer.FileMakerActivity.REQUEST_CODE;
import static com.centrahub.focus.sourav.firebasefilesyncer.accountauth.AccountGeneral.ACCOUNT_TYPE;
import static com.centrahub.focus.sourav.firebasefilesyncer.accountauth.AccountGeneral.USERDATA_USER_OBJ_ID;
import static com.centrahub.focus.sourav.firebasefilesyncer.accountauth.AccountGeneral.sServerAuthenticate;

public class AccountSinginActivity extends AccountAuthenticatorActivity implements View.OnClickListener {
    public final static String ARG_ACCOUNT_TYPE = ACCOUNT_TYPE;
    private static final String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
//    public final static String PARAM_USER_PASS = "USER_PASS";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";


    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private final int REQ_SIGNUP = 1;


    //firebase auth


    private FirebaseAuth mAuth;

    private AccountManager mAccountManager;
    private String mAuthTokenType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_accountlogin);
        //initialize account manager
        mAccountManager = AccountManager.get(getBaseContext());

        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);

        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (mAuthTokenType == null)
            mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;

/*
        if (accountName != null) {
            ((TextView) findViewById(R.id.account_et_email)).setText(accountName);
        }
*/

        //intialize click events
        Button btSingIn = findViewById(R.id.account_sing_in);
        btSingIn.setOnClickListener(this);
        TextView tv_newUser = (TextView) findViewById(R.id.user_is_new);
        SpannableString content = new SpannableString(getResources().getString(R.string.new_user_click_here));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tv_newUser.setText(content);
        tv_newUser.setOnClickListener(this);
        tv_newUser.setText(Html.fromHtml(getString(R.string.new_user_click_here)));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onClick(View view) {


        EditText etEmail = findViewById(R.id.account_et_email);
        EditText etPassword = findViewById(R.id.account_et_password);
        if (etEmail.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()) {
            Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            return;
        }

        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        final String accountType = ARG_ACCOUNT_TYPE;


        switch (view.getId()) {

            case R.id.account_sing_in:
                new AsyncTask<String, Void, Intent>(){

                @Override
                protected Intent doInBackground(String... params) {

                    Bundle data = new Bundle();
                    try {
                        FirebaseUser user = sServerAuthenticate.userSignIn(AccountSinginActivity.this, email, password, mAuthTokenType);

                        data.putString(AccountManager.KEY_ACCOUNT_NAME, email);
                        data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                        data.putString(AccountManager.KEY_AUTHTOKEN, user.getUid());

                        // We keep the user's object id as an extra data on the account.
                        // It's used later for determine ACL for the data we send to the Parse.com service
                        Bundle userData = new Bundle();
                        userData.putString(USERDATA_USER_OBJ_ID, user.getEmail());
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
                        Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                    } else {
                        finishLogin(intent);
                    }
                }
            }.execute();;

                break;
            case R.id.user_is_new:
//                Intent newUser=new Intent(AccountSinginActivity.this, AccountSingupActivity.class);
//                startActivity(newUser);


                Intent signup = new Intent(getBaseContext(), AccountSingupActivity.class);
//                signup.putExtras(getIntent().getExtras());
                startActivityForResult(signup, REQ_SIGNUP);
//                finish();
                break;
        }

    }
    private void finishLogin(Intent intent) {
//        Log.d("udinic", TAG + "> finishLogin");

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
//        String accountType=intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

        if (!accountName.isEmpty()&&!accountPassword.isEmpty()){
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
//            Log.d("udinic", TAG + "> finishLogin > addAccountExplicitly");
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword,intent.getBundleExtra(AccountManager.KEY_USERDATA));
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
            Toast.makeText(this, "New Account Created", Toast.LENGTH_SHORT).show();
        } else {
//            Log.d("udinic", TAG + "> finishLogin > setPassword");
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(REQUEST_CODE, intent);
        finish();
    }else
            Toast.makeText(this, "Registration Fails", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth!=null)
            mAuth.signOut();
    }
}
