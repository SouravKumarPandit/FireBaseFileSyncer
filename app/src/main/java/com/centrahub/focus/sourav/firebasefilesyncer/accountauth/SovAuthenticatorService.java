package com.centrahub.focus.sourav.firebasefilesyncer.accountauth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SovAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {

        SovAuthenticator authenticator = new SovAuthenticator(this);
        return authenticator.getIBinder();
    }
}