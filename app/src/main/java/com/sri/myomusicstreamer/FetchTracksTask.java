package com.sri.myomusicstreamer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sri.myomusicstreamer.parcelable.TrackParcelable;
import com.sri.myomusicstreamer.adapters.TrackAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by smanda on 5/28/15.
 */

public class FetchTracksTask extends AsyncTask<String, Void, List<TrackParcelable>> {

    private final String LOG_TAG = FetchTracksTask.class.getSimpleName();
    private final Context context;
    private TrackAdapter trackAdapter;
    private boolean DEBUG = true;

    public FetchTracksTask(Context context, TrackAdapter trackAdapter) {
        this.context = context;
        this.trackAdapter = trackAdapter;
    }

    @Override
    protected List<TrackParcelable> doInBackground(String... params) {

        // If there's no tracks, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }
        List<TrackParcelable> mTracks = new ArrayList<>();
        String tracksQuery = params[0];
        try {
            // Will contain the raw JSON response as a string.
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            Map<String, Object> options = new HashMap<>();
            String locationCode = Utility.getPreferredLocation(context);
            options.put(SpotifyService.COUNTRY, locationCode);
            Log.d(LOG_TAG, String.format("location code: %s",locationCode));
            Tracks tracks = spotify.getArtistTopTrack(tracksQuery,options);
            for (Track track : tracks.tracks) {
                Log.d(LOG_TAG, String.format("track name: %s",track.name));
                mTracks.add(new TrackParcelable(track));
            }
        } finally {
            // TODO: Handle errors
        }
        return mTracks;
    }

    @Override
    protected void onPostExecute(List<TrackParcelable> trackParcelables) {
        if (trackParcelables != null && trackAdapter != null) {
            Log.d(LOG_TAG, "clearing the track adapter and setting the new values");
            this.trackAdapter.getTracksList().clear();
            for(TrackParcelable trackParcelable: trackParcelables)
                this.trackAdapter.getTracksList().add(trackParcelable);
            this.trackAdapter.notifyDataSetChanged();
        }
    }
}