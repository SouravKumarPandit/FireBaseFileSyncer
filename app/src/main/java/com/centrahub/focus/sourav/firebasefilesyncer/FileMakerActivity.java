package com.centrahub.focus.sourav.firebasefilesyncer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.centrahub.focus.sourav.firebasefilesyncer.accountauth.AccountGeneral;
import com.centrahub.focus.sourav.firebasefilesyncer.accountauth.AccountSinginActivity;
import com.centrahub.focus.sourav.firebasefilesyncer.db.SovFileContract;
import com.centrahub.focus.sourav.firebasefilesyncer.db.dao.SovFilesShow;
import com.centrahub.focus.sourav.firebasefilesyncer.syncinglogic.ParseComServerAccessor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sourav on 12-Jan-18.
 */

public class FileMakerActivity extends AppCompatActivity implements View.OnClickListener {


    public static final int REQUEST_CODE = 153;
    private EditText etContent;
    private EditText etFileName;
    FirebaseAuth mAuth;
    public static boolean isLogedIn = false;
    private Button btConnect;

    // account objects
    private AccountManager mAccountManager;
    private String authToken = null;
    private Account mConnectedAccount;

    SyncStatusObserver syncObserver = new SyncStatusObserver() {
        @Override
        public void onStatusChanged(final int which) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshSyncStatus();
                }
            });
        }
    };

    Object handleSyncObserver;
    private FirebaseUser user;

    @Override
    protected void onResume() {
        super.onResume();
        handleSyncObserver = ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE |
                ContentResolver.SYNC_OBSERVER_TYPE_PENDING, syncObserver);
    }

    @Override
    protected void onPause() {
        if (handleSyncObserver != null)
            ContentResolver.removeStatusChangeListener(handleSyncObserver);
        super.onPause();
        super.onStop();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filemaker);
        mAuth = FirebaseAuth.getInstance();
        mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);

         user = mAuth.getCurrentUser();


        Button btAddFile = findViewById(R.id.add_file);
        btAddFile.setOnClickListener(this);

        Button btSyncNow = findViewById(R.id.sync_now);
        btSyncNow.setOnClickListener(this);

//        String authority = SovFileContract.AUTHORITY;
//        int isSyncable = ContentResolver.getIsSyncable(mConnectedAccount, authority);
//        boolean autSync = ContentResolver.getSyncAutomatically(mConnectedAccount, authority);
//        ((CheckBox) findViewById(R.id.cbIsSyncable)).setChecked(isSyncable > 0);
//        ((CheckBox) findViewById(R.id.cbAutoSync)).setChecked(autSync);

        btConnect = findViewById(R.id.connect);
        btConnect.setOnClickListener(this);


        loginStatus(mAuth.getCurrentUser());

        etContent = findViewById(R.id.file_content);
        etFileName = findViewById(R.id.file_name);

        setFilelist();
        findViewById(R.id.show_localfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SovFilesShow> list = readFromContentProvider();
                AlertDialog.Builder builder = new AlertDialog.Builder(FileMakerActivity.this);
                builder.setAdapter(new ArrayAdapter<SovFilesShow>(FileMakerActivity.this, android.R.layout.simple_list_item_1, list),null);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });
        findViewById(R.id.show_accountlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccountPicker(AccountGeneral.ACCOUNT_TYPE,false);

            }
        });
        ((CheckBox)findViewById(R.id.cbIsSyncable)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mConnectedAccount == null) {
                    Toast.makeText(FileMakerActivity.this, "Please connect first", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Setting the syncable state of the sync adapter
                String authority = SovFileContract.AUTHORITY;
                ContentResolver.setIsSyncable(mConnectedAccount, authority, isChecked ? 1 : 0);
            }
        });

        ((CheckBox)findViewById(R.id.cbAutoSync)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mConnectedAccount == null) {
                    Toast.makeText(FileMakerActivity.this, "Please connect first", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Setting the autosync state of the sync adapter
                String authority = SovFileContract.AUTHORITY;
                ContentResolver.setSyncAutomatically(mConnectedAccount,authority, isChecked);
            }
        });
        findViewById(R.id.show_file).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, List<SovFilesShow>>() {

                    ProgressDialog progressDialog = new ProgressDialog(FileMakerActivity.this);
                    @Override
                    protected void onPreExecute() {
                        if (authToken == null) {
                            Toast.makeText(FileMakerActivity.this, "Please connect first", Toast.LENGTH_SHORT).show();
                            cancel(true);
                        } else {
                            progressDialog.show();
                        }
                    }

                    @Override
                    protected List<SovFilesShow> doInBackground(Void... nothing) {
                        ParseComServerAccessor serverAccessor = new ParseComServerAccessor();

                        try {
                            return serverAccessor.getShows(authToken);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(List<SovFilesShow> sovFilesShows) {
                        progressDialog.dismiss();
                        if (sovFilesShows != null) {
                            showOnDialog("Remote TV Shows", sovFilesShows);
                        }
                    }
                }.execute();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully created an account
        if (resultCode == REQUEST_CODE || resultCode == RESULT_OK) {
            ;
            btConnect.setText("Sing Out");
            isLogedIn = true;
        } else {
            ;
            btConnect.setText("Sing In");
            isLogedIn = false;
            super.onActivityResult(requestCode, resultCode, data);
        }
        loginStatus(mAuth.getCurrentUser());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.add_file:
                try {
                    if (etFileName.getText().toString().isEmpty() || etContent.getText().toString().isEmpty()) {
                        Toast.makeText(getBaseContext(),
                                "File \n name or content \ncant be empty ",
                                Toast.LENGTH_SHORT).show();
                        break;
                    }
                    File folder = new File(Environment.getExternalStorageDirectory().toString() + "/firebase");
                    boolean success = true;
                    if (!folder.exists()) {
                        success = folder.mkdir();
                    }
                    File myFile = new File("/sdcard/firebase/" + etFileName.getText().toString() + ".txt");
                    myFile.createNewFile();
                    FileOutputStream fOut = new FileOutputStream(myFile);
                    OutputStreamWriter myOutWriter =
                            new OutputStreamWriter(fOut);
                    myOutWriter.append(etContent.getText());
                    myOutWriter.close();
                    fOut.close();
//                    String tvshowsNames[] = getResources().getStringArray(R.array.tvshows_names);
//                    int tvshowsYears[] = getResources().getIntArray(R.array.tvshows_year);
//                    int randIdx = new Random(currentTimeMillis()).nextInt(tvshowsYears.length);

                    // Creating a Tv Show data object, in order to use some of its convenient methods
//                    SovFilesShow tvShow = new SovFilesShow(tvshowsNames[randIdx], tvshowsYears[randIdx]);
                    SovFilesShow filesShow = new SovFilesShow(etFileName.getText().toString() + ".txt", new Date().getMinutes());
//                    Log.d("udinic", "Tv Show to add [id="+randIdx+"]: " + tvShow.toString());

                    // Add our Tv show to the local data base. This normally should be done on a background thread
                    getContentResolver().insert(SovFileContract.CONTENT_URI, filesShow.getContentValues());


                    etContent.setText("");
                    etFileName.setText("");

                    Toast.makeText(FileMakerActivity.this, "Added \"" + filesShow.toString() + "\"", Toast.LENGTH_SHORT).show();

                    Toast.makeText(getBaseContext(),"Done writing SD " + etFileName.getText().toString(),
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage() + "Error 3",
                            Toast.LENGTH_SHORT).show();
                }
                LinearLayout listLinear = findViewById(R.id.list_item_ll);
                listLinear.removeAllViews();
                setFilelist();

                break;
            case R.id.sync_now:
                if (mConnectedAccount == null) {
                    Toast.makeText(FileMakerActivity.this, "Please connect first", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true); // Performing a sync no matter if it's off
                bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true); // Performing a sync no matter if it's off
                ContentResolver.requestSync(mConnectedAccount, SovFileContract.AUTHORITY, bundle);


                break;

            case R.id.connect:
                startActivityForResult(new Intent(FileMakerActivity.this, AccountSinginActivity.class), REQUEST_CODE);
                loginStatus(mAuth.getCurrentUser());
                getTokenForAccountCreateIfNeeded(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
                loginStatus(mAuth.getCurrentUser());

                break;
        }
        loginStatus(mAuth.getCurrentUser());

    }

    public void setFilelist() {

        LinearLayout listLinear = findViewById(R.id.list_item_ll);
        String path = Environment.getExternalStorageDirectory().toString() + "/firebase";
        String listFilepath = "/sdcard/firebase/";

        File folder = new File(Environment.getExternalStorageDirectory().toString() + "/firebase");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            // Do something on success

        } else {
            // Do something else on failure
        }

        Log.d("Files", "Path: " + path);

        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files != null) {
            Log.d("Files", "Size: " + files.length);
            for (int i = 0; i < files.length; i++) {
                Log.d("Files", "FileName:" + files[i].getName());
                listLinear.addView(getFileName(files[i].getName()));
            }
        }

    }

    public TextView getFileName(String name) {
//        TextView fileName=new TextView(this);
        TextView fileName = new TextView(this);
        fileName.setPadding(5, 10, 5, 10);
        LinearLayout.LayoutParams fileNameParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fileNameParam.setMargins(5, 10, 5, 0);
        fileName.setLayoutParams(fileNameParam);
        fileName.setMaxLines(1);
        fileName.setBackground(getResources().getDrawable(R.drawable.list_background));
        fileName.setGravity(Gravity.CENTER_HORIZONTAL);
        fileName.setTextColor(Color.WHITE);
        fileName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        fileName.setText(name);


        return fileName;
    }

    private List<SovFilesShow> readFromContentProvider() {
        Cursor curTvShows = getContentResolver().query(SovFileContract.CONTENT_URI, null, null, null, null);

        ArrayList<SovFilesShow> shows = new ArrayList<SovFilesShow>();

        if (curTvShows != null) {
            while (curTvShows.moveToNext())
                shows.add(SovFilesShow.fromCursor(curTvShows));
            curTvShows.close();
        }
        return shows;
    }
    private void showOnDialog(String title, List<SovFilesShow> sovFilesShows) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FileMakerActivity.this);
        builder.setTitle(title);
        builder.setAdapter(new ArrayAdapter<SovFilesShow>(FileMakerActivity.this, android.R.layout.simple_list_item_1, sovFilesShows),null);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType) {
//        showAccountPicker(authTokenType,false);
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(accountType, authTokenType, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle bnd = null;
                        try {
                            bnd = future.getResult();
                            authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                            if (authToken != null) {
                                String accountName = bnd.getString(AccountManager.KEY_ACCOUNT_NAME);
                                mConnectedAccount = new Account(accountName, AccountGeneral.ACCOUNT_TYPE);
                                initButtonsAfterConnect();
                            }
                            showMessage(((authToken != null) ? "SUCCESS!\ntoken: " + authToken : "FAIL"));
                            Log.d("udinic", "GetTokenForAccount Bundle is " + bnd);
                            loginStatus(mAuth.getCurrentUser());
                        } catch (Exception e) {
                            e.printStackTrace();
                            showMessage(e.getMessage());
                        }
                    }
                }
                , null);
    }
// in case of multiple account use this methord
    private void showAccountPicker(final String authTokenType, final boolean invalidate) {

        final Account availableAccounts[] = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);

        if (availableAccounts.length == 0) {
            Toast.makeText(this, "No accounts", Toast.LENGTH_SHORT).show();
        } else {
            String name[] = new String[availableAccounts.length];
            for (int i = 0; i < availableAccounts.length; i++) {
                name[i] = availableAccounts[i].name;
            }

            // Account picker
            new AlertDialog.Builder(this).setTitle("Pick Account").setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, name), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(invalidate)
                        invalidateAuthToken(availableAccounts[which], authTokenType);
                    else
                        getExistingAccountAuthToken(availableAccounts[which], authTokenType);
                }
            }).show();
        }
    }

    private void invalidateAuthToken(final Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null,null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();

                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    mAccountManager.invalidateAuthToken(account.type, authtoken);
                    showMessage(account.name + " invalidated");
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }).start();
    }
    private void getExistingAccountAuthToken(final Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();

                    for (String key : bnd.keySet()) {
                        Log.d("udinic", "Bundle[" + key + "] = " + bnd.get(key));
                    }
                    mConnectedAccount=account;
                    final String authtoken = bnd.getString(AccountManager.KEY_ACCOUNT_NAME);
//                    authtoken="uewqyr";
                    showMessage((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL");
                    Log.d("udinic", "GetToken Bundle is " + bnd);
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }).start();
    }
    private void initButtonsAfterConnect() {
        String authority = SovFileContract.AUTHORITY;

        // Get the syncadapter settings and init the checkboxes accordingly
        int isSyncable = ContentResolver.getIsSyncable(mConnectedAccount, authority);
        boolean autSync = ContentResolver.getSyncAutomatically(mConnectedAccount, authority);

//        ((CheckBox) findViewById(R.id.cbIsSyncable)).setChecked(isSyncable > 0);
//        ((CheckBox) findViewById(R.id.cbAutoSync)).setChecked(autSync);

//        findViewById(R.id.cbIsSyncable).setEnabled(true);
//        findViewById(R.id.cbAutoSync).setEnabled(true);
//        findViewById(R.id.status).setEnabled(true);
//        findViewById(R.id.btnShowRemoteList).setEnabled(true);
//        findViewById(R.id.btnSync).setEnabled(true);
        user=mAuth.getCurrentUser();
        if (user == null) {
//            btConnect.setText("Sing In");
//            isLogedIn = false;
            btConnect.setEnabled(true);
        } else {
//            btConnect.setText("Sing Out");
//            isLogedIn = true;
            btConnect.setEnabled(false);
            btConnect.setBackground(getResources().getDrawable(R.drawable.disabled_background));
        }

//
        refreshSyncStatus();
    }

    private void showMessage(final String msg) {
//        loginStatus(mAuth.getCurrentUser());
        if (msg == null || msg.trim().equals(""))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loginStatus(FirebaseUser user) {
        String status;

        if (user == null)
            status = "Status: not Login....";
        else {
            status = "Status: User is loged in....\n  " + "name /email :  \n " + user.getEmail() + "\n " + user.getUid();
        }
        ((TextView) findViewById(R.id.login_status)).setText(status);
//        Log.d("udinic", "refreshSyncStatus> " + status);
    }

    private void refreshSyncStatus() {
        String status;
        if (ContentResolver.isSyncActive(mConnectedAccount, SovFileContract.AUTHORITY))
            status = "Status: Syncing....";
        else if (ContentResolver.isSyncPending(mConnectedAccount, SovFileContract.AUTHORITY))
            status = "Status: Pending....";
        else
            status = "Status: Idle";
        ((TextView) findViewById(R.id.sync_status)).setText(status);
//        Log.d("udinic", "refreshSyncStatus> " + status);
    }
}
