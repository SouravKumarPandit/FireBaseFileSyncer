package com.centrahub.focus.sourav.firebasefilesyncer.accountauth;

import android.content.Context;

import com.google.firebase.auth.FirebaseUser;

public interface ServerAuthenticate {
    public FirebaseUser userSignUp(Context clContext , final String name, final String email, final String pass, String authType) throws Exception;

    public FirebaseUser userSignIn(Context clContext , final String user, final String pass, String authType) throws Exception;
}