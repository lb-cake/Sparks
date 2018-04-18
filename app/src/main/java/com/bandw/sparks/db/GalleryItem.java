package com.bandw.sparks.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "gallery_item")
public class GalleryItem {

    @PrimaryKey
    @ColumnInfo(name = "_id")
    private int _id;

    @SerializedName("id")
    private String mId;

    @SerializedName("url_s")
    private String mURL;

    @SerializedName("title")
    private String mCaption;

    @SerializedName("owner")
    private String mOwner;

    public int get_id() { return _id;}

    public void set_id(int id) {this._id = id; }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getURL() {
        return mURL;
    }

    public void setURL(String URL) {
        mURL = URL;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }

    public Uri getPhotoPageUri() {
        return Uri.parse("http://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }

    @Override
    public String toString() {
        return "Id: " + mId + ", URL: " + mURL + ", Caption: " + mCaption;
    }
}
