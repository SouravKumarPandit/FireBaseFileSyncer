package com.centrahub.focus.sourav.firebasefilesyncer.accountauth;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;

public class ParseFirebaseServer implements ServerAuthenticate {
    private FirebaseAuth mAuth;
    public static FirebaseUser user;



/*    public static List<Header> getAppParseComHeaders() {
        List<Header> ret = new ArrayList<Header>();
        ret.add(new BasicHeader("X-Parse-Application-Id", APP_ID));
        ret.add(new BasicHeader("X-Parse-REST-API-Key", REST_API_KEY));
        return ret;
    }*/

    @Override
    public FirebaseUser userSignUp(final Context clContext, String userName, String userEmail, String userPass, String authType) throws Exception {

/*
        String url = "https://api.parse.com/1/users";

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);

        for (Header header : getAppParseComHeaders()) {
            httpPost.addHeader(header);
        }
        httpPost.addHeader("Content-Type", "application/json");

        String user = "{\"username\":\"" + email + "\",\"password\":\"" + pass + "\",\"phone\":\"415-392-0202\"}";
        HttpEntity entity = new StringEntity(user);
        httpPost.setEntity(entity);

        try {
            HttpResponse response = httpClient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity());

            if (response.getStatusLine().getStatusCode() != 201) {
                ParseComError error = new Gson().fromJson(responseString, ParseComError.class);
                throw new Exception("Error creating user[" + error.code + "] - " + error.error);
            }

            User createdUser = new Gson().fromJson(responseString, User.class);

            return createdUser;

        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user != null) {
            mAuth.signOut();
        }
        mAuth.createUserWithEmailAndPassword(userEmail, userPass)
                .addOnCompleteListener((Activity) clContext, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "createUserWithEmail:success");
                            user = mAuth.getCurrentUser();
                            Toast.makeText(clContext, "Adding Success.", Toast.LENGTH_SHORT).show();

                        } else {
                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(clContext, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
        return user;
    }

    @Override
    public FirebaseUser userSignIn(final Context clContext, String userEmail, String userPass, String authType) throws Exception {
/*

        Log.d("udini", "userSignIn");

        DefaultHttpClient httpClient = new DefaultHttpClient();
        String url = "https://api.parse.com/1/login";

        String query = null;
        try {
            query = String.format("%s=%s&%s=%s", "username", URLEncoder.encode(user, "UTF-8"), "password", pass);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        url += "?" + query;

        HttpGet httpGet = new HttpGet(url);

        for (Header header : getAppParseComHeaders()) {
            httpGet.addHeader(header);
        }

        HttpParams params = new BasicHttpParams();
        params.setParameter("username", user);
        params.setParameter("password", pass);
        httpGet.setParams(params);
//        httpGet.getParams().setParameter("username", user).setParameter("password", pass);

        try {
            HttpResponse response = httpClient.execute(httpGet);

            String responseString = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() != 200) {
                ParseComError error = new Gson().fromJson(responseString, ParseComError.class);
                throw new Exception("Error signing-in [" + error.code + "] - " + error.error);
            }

            User loggedUser = new Gson().fromJson(responseString, User.class);
            return loggedUser;

        } catch (IOException e) {
            e.printStackTrace();
        }

*/
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(userEmail, userPass)


                .addOnCompleteListener((Activity) clContext, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithEmail:success");
                            user = mAuth.getCurrentUser();

                            Toast.makeText(clContext, "Authentication Success.", Toast.LENGTH_SHORT).show();

//                            updateUI(user);
//                            Intent fileMakerIntent = new Intent(clContext, FileMakerActivity.class);
//                            clContext.startActivity(fileMakerIntent);


                        } else {
                            // If sign in fails, display a message to the user.
//                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(clContext, "Authentication failed.", Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });


        return user;
    }

    public static class ParseComError implements Serializable {
        public int code;
        public String error;
    }
}
