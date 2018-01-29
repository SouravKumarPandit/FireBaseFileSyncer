package com.centrahub.focus.sourav.firebasefilesyncer.db.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.centrahub.focus.sourav.firebasefilesyncer.db.SovFileDbHelper;

import java.io.Serializable;

/**
 * This class represent a TV Show.
 *
 * Created by Udini on 6/22/13.
 */
public class SovFilesShow implements Serializable {

    // Fields
    public String name;
    public int year;

    public SovFilesShow(String name, int year) {
        this.name = name;
        this.year = year;
    }

    /**
     * Convenient method to get the objects data members in ContentValues object.
     * This will be useful for Content Provider operations,
     * which use ContentValues object to represent the data.
     *
     * @return
     */
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(SovFileDbHelper.TVSHOWS_COL_NAME, name);
        values.put(SovFileDbHelper.TVSHOWS_COL_YEAR, year);
        return values;
    }

    // Create a SovFilesShow object from a cursor
    public static SovFilesShow fromCursor(Cursor curTvShows) {
        String name = curTvShows.getString(curTvShows.getColumnIndex(SovFileDbHelper.TVSHOWS_COL_NAME));
        int year = curTvShows.getInt(curTvShows.getColumnIndex(SovFileDbHelper.TVSHOWS_COL_YEAR));

        return new SovFilesShow(name, year);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SovFilesShow sovFilesShow = (SovFilesShow) o;

        if (year != sovFilesShow.year) return false;
        if (!name.equals(sovFilesShow.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + year;
        return result;
    }

    @Override
    public String toString() {
        return name + " (" + year + ")";
    }
}
