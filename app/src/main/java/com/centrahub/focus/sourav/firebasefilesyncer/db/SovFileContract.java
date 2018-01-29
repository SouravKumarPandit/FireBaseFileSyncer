package com.centrahub.focus.sourav.firebasefilesyncer.db;

import android.net.Uri;

/**
 * Created by sourav on 18-Jan-18.
 */

public class SovFileContract {

    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sourav.firebasefilesyncer";
    public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.sourav.firebasefilesyncer";

    public static final String AUTHORITY ="com.centrahub.focus.sourav.firebasefilesyncer.provider" ;
    public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/fileslist");
}
