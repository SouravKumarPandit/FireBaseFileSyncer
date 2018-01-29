package com.centrahub.focus.sourav.firebasefilesyncer.syncinglogic;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.centrahub.focus.sourav.firebasefilesyncer.accountauth.AccountGeneral;
import com.centrahub.focus.sourav.firebasefilesyncer.db.SovFileContract;
import com.centrahub.focus.sourav.firebasefilesyncer.db.dao.SovFilesShow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SovFileSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SovFileSyncAdapter";

    private final AccountManager mAccountManager;

    public SovFileSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

//         Building a print of the extras we got
        StringBuilder sb = new StringBuilder();
        if (extras != null) {
            for (String key : extras.keySet()) {
                sb.append(key + "[" + extras.get(key) + "] ");
            }
        }

        Log.d("udinic", TAG + "> onPerformSync for account[" + account.name + "]. Extras: "+sb.toString());

        try {
            // Get the auth token for the current account and
            // the userObjectId, needed for creating items on Parse.com account
            String authToken = mAccountManager.blockingGetAuthToken(account,
                    AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, true);
            String userObjectId = mAccountManager.getUserData(account,
                    AccountGeneral.USERDATA_USER_OBJ_ID);

            ParseComServerAccessor parseComService = new ParseComServerAccessor();

            Log.d("udinic", TAG + "> Get remote TV Shows");
            // Get shows from remote
            List<SovFilesShow> remoteSovFilesShows = parseComService.getShows(authToken);

            Log.d("udinic", TAG + "> Get local TV Shows");
            // Get shows from local
            ArrayList<SovFilesShow> localSovFilesShows = new ArrayList<SovFilesShow>();
            Cursor curTvShows = provider.query(SovFileContract.CONTENT_URI, null, null, null, null);
            if (curTvShows != null) {
                while (curTvShows.moveToNext()) {
                    localSovFilesShows.add(SovFilesShow.fromCursor(curTvShows));
                }
                curTvShows.close();
            }

            // See what Local shows are missing on Remote
            ArrayList<SovFilesShow> showsToRemote = new ArrayList<SovFilesShow>();
            for (SovFilesShow localSovFilesShow : localSovFilesShows) {
                if (!remoteSovFilesShows.contains(localSovFilesShow))
                    showsToRemote.add(localSovFilesShow);
            }

            // See what Remote shows are missing on Local
            ArrayList<SovFilesShow> showsToLocal = new ArrayList<SovFilesShow>();
            for (SovFilesShow remoteSovFilesShow : remoteSovFilesShows) {
                if (!localSovFilesShows.contains(remoteSovFilesShow) && remoteSovFilesShow.year != 1) // TODO REMOVE THIS
                    showsToLocal.add(remoteSovFilesShow);
            }

            if (showsToRemote.size() == 0) {
                Log.d("udinic", TAG + "> No local changes to update server");
            } else {
                Log.d("udinic", TAG + "> Updating remote server with local changes");

                // Updating remote tv shows
                for (SovFilesShow remoteSovFilesShow : showsToRemote) {
                    Log.d("udinic", TAG + "> Local -> Remote [" + remoteSovFilesShow.name + "]");
                    parseComService.putShow(authToken, userObjectId, remoteSovFilesShow);
                }
            }

            if (showsToLocal.size() == 0) {
                Log.d("udinic", TAG + "> No server changes to update local database");
            } else {
                Log.d("udinic", TAG + "> Updating local database with remote changes");

                // Updating local tv shows
                int i = 0;
                ContentValues showsToLocalValues[] = new ContentValues[showsToLocal.size()];
                for (SovFilesShow localSovFilesShow : showsToLocal) {
                    Log.d("udinic", TAG + "> Remote -> Local [" + localSovFilesShow.name + "]");
                    showsToLocalValues[i++] = localSovFilesShow.getContentValues();
                }
                provider.bulkInsert(SovFileContract.CONTENT_URI, showsToLocalValues);
            }

            Log.d("udinic", TAG + "> Finished.");

        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (IOException e) {
            syncResult.stats.numIoExceptions++;
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            syncResult.stats.numAuthExceptions++;
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

