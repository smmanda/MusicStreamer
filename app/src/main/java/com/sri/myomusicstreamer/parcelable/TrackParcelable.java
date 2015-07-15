package com.sri.myomusicstreamer.parcelable;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by smanda on 5/28/15.
 */
public class TrackParcelable implements Parcelable {

    private String trackName;
    private String trackArtistName;
    private String trackAlbumName;
    private long trackDuration;
    private String trackPreviewUrl;
    private List<String> trackAlbumImgUrls;

    public TrackParcelable(Track track) {
        this.trackName = track.name;
        this.trackAlbumName = track.album.name;
        this.trackArtistName = track.artists.get(0).name;
        this.trackDuration = track.duration_ms;
        this.trackPreviewUrl = track.preview_url;
        this.trackAlbumImgUrls = new ArrayList<>();
        for (Image image : track.album.images)
            this.trackAlbumImgUrls.add(image.url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcelOut, int flags) {
        parcelOut.writeString(this.trackName);
        parcelOut.writeString(this.trackArtistName);
        parcelOut.writeString(this.trackAlbumName);
        parcelOut.writeString(this.trackPreviewUrl);
        parcelOut.writeLong(this.trackDuration);
        parcelOut.writeStringList(this.trackAlbumImgUrls);
    }

    protected TrackParcelable(Parcel parcelIn) {
        this.trackName = parcelIn.readString();
        this.trackArtistName = parcelIn.readString();
        this.trackAlbumName = parcelIn.readString();
        this.trackPreviewUrl = parcelIn.readString();
        this.trackDuration = parcelIn.readLong();
        if (this.trackAlbumImgUrls == null)
            this.trackAlbumImgUrls = new ArrayList<String>();
        parcelIn.readStringList(this.trackAlbumImgUrls);
    }

    public static final Creator<TrackParcelable> CREATOR = new Creator<TrackParcelable>() {
        public TrackParcelable createFromParcel(Parcel in) {
            return new TrackParcelable(in);
        }

        public TrackParcelable[] newArray(int size) {
            return new TrackParcelable[size];
        }
    };

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getTrackArtistName() {
        return trackArtistName;
    }

    public void setTrackArtistName(String trackArtistName) {
        this.trackArtistName = trackArtistName;
    }

    public String getTrackAlbumName() {
        return trackAlbumName;
    }

    public void setTrackAlbumName(String trackAlbumName) {
        this.trackAlbumName = trackAlbumName;
    }

    public long getTrackDuration() {
        return trackDuration;
    }

    public void setTrackDuration(long trackDuration) {
        this.trackDuration = trackDuration;
    }

    public String getTrackPreviewUrl() {
        return trackPreviewUrl;
    }

    public void setTrackPreviewUrl(String trackPreviewUrl) {
        this.trackPreviewUrl = trackPreviewUrl;
    }

    public List<String> getTrackAlbumImgUrls() {
        return trackAlbumImgUrls;
    }

    public void setTrackAlbumImgUrls(List<String> trackAlbumImgUrls) {
        this.trackAlbumImgUrls = trackAlbumImgUrls;
    }
}
