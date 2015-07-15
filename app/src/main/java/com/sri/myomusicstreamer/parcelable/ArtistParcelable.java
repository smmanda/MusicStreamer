package com.sri.myomusicstreamer.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by smanda on 5/28/15.
 */

public class ArtistParcelable implements Parcelable {
    public String getSpotifyId() {
        return spotifyId;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public List<String> getImgURLS() {
        return imgURLS;
    }

    public void setImgURLS(List<String> imgURLS) {
        this.imgURLS = imgURLS;
    }

    private String spotifyId;
    private String artistName;
    private List<String> imgURLS;

    public ArtistParcelable(Artist artist) {
        spotifyId = artist.id;
        artistName = artist.name;
        imgURLS = new ArrayList<>();
        for (Image image : artist.images)
            imgURLS.add(image.url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcelOut, int flags) {
        parcelOut.writeString(this.spotifyId);
        parcelOut.writeString(this.artistName);
        parcelOut.writeStringList(this.imgURLS);
    }

    protected ArtistParcelable(Parcel parcelIn) {
        this.spotifyId = parcelIn.readString();
        this.artistName = parcelIn.readString();
        this.imgURLS = parcelIn.createStringArrayList();
    }

    public static final Parcelable.Creator<ArtistParcelable> CREATOR = new Parcelable.Creator<ArtistParcelable>() {
        public ArtistParcelable createFromParcel(Parcel in) {
            return new ArtistParcelable(in);
        }

        public ArtistParcelable[] newArray(int size) {
            return new ArtistParcelable[size];
        }
    };
}
