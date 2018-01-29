package com.centrahub.focus.sourav.firebasefilesyncer.syncinglogic;

import com.centrahub.focus.sourav.firebasefilesyncer.db.dao.SovFilesShow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * This class is intended to encapsulate all the actions against Parse.com.
 *
 * Created by Udini on 7/6/13.
 */
public class ParseComServerAccessor {
    public List<SovFilesShow> getShows(String auth) throws Exception {
//
//        Log.d("udini", "getShows auth[" + auth + "]");
//
//        DefaultHttpClient httpClient = new DefaultHttpClient();
//        String url = "https://api.parse.com/1/classes/tvshows";
//
//        HttpGet httpGet = new HttpGet(url);
//        for (Header header : getAppParseComHeaders()) {
//            httpGet.addHeader(header);
//        }
//        httpGet.addHeader("X-Parse-Session-Token", auth); // taken from https://parse.com/questions/how-long-before-the-sessiontoken-expires
//
//        try {
//            HttpResponse response = httpClient.execute(httpGet);
//
//            String responseString = EntityUtils.toString(response.getEntity());
//            Log.d("udini", "getShows> Response= " + responseString);
//
//            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
//                ParseComServer.ParseComError error = new Gson().fromJson(responseString, ParseComServer.ParseComError.class);
//                throw new Exception("Error retrieving tv shows ["+error.code+"] - " + error.error);
//            }
//
//            TvShows shows = new Gson().fromJson(responseString, TvShows.class);
//            return shows.results;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
        ArrayList<SovFilesShow> dummyList = new ArrayList<SovFilesShow>();
        for (int i=0;i<5;i++){

            dummyList.add(new SovFilesShow("Dummy Name _"+i+"_", Calendar.getInstance().get(Calendar.YEAR)));
        }
        return dummyList;
    }

    public void putShow(String authtoken, String userId, SovFilesShow showToAdd) throws Exception {

//        Log.d("udinic", "putShow ["+showToAdd.name+"]");
//
//        DefaultHttpClient httpClient = new DefaultHttpClient();
//        String url = "https://api.parse.com/1/classes/tvshows";
//
//        HttpPost httpPost = new HttpPost(url);
//
//        for (Header header : getAppParseComHeaders()) {
//            httpPost.addHeader(header);
//        }
//        httpPost.addHeader("X-Parse-Session-Token", authtoken); // taken from https://parse.com/questions/how-long-before-the-sessiontoken-expires
//        httpPost.addHeader("Content-Type", "application/json");
//
//        JSONObject tvShow = new JSONObject();
//        tvShow.put("name", showToAdd.name);
//        tvShow.put("year", showToAdd.year);
//
//        // Creating ACL JSON object for the current user
//        JSONObject acl = new JSONObject();
//        JSONObject aclEveryone = new JSONObject();
//        JSONObject aclMe = new JSONObject();
//        aclMe.put("read", true);
//        aclMe.put("write", true);
//        acl.put(userId, aclMe);
//        acl.put("*", aclEveryone);
//        tvShow.put("ACL", acl);
//
//        String request = tvShow.toString();
//        Log.d("udinic", "Request = " + request);
//        httpPost.setEntity(new StringEntity(request,"UTF-8"));
//
//        try {
//            HttpResponse response = httpClient.execute(httpPost);
//            String responseString = EntityUtils.toString(response.getEntity());
//            if (response.getStatusLine().getStatusCode() != 201) {
//                ParseComServer.ParseComError error = new Gson().fromJson(responseString, ParseComServer.ParseComError.class);
//                throw new Exception("Error posting tv shows ["+error.code+"] - " + error.error);
//            } else {
////                Log.d("udini", "Response string = " + responseString);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private class TvShows implements Serializable {
        List<SovFilesShow> results;
    }

}
