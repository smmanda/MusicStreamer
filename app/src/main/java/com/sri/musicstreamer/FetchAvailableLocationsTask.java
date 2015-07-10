package com.sri.musicstreamer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.TracksPager;

/**
 * Created by smanda on 5/28/15.
 */

public class FetchAvailableLocationsTask extends AsyncTask<String, Void, List<String>> {

    private final String LOG_TAG = FetchAvailableLocationsTask.class.getSimpleName();
    private final Context context;
    private List<String> availableMarkets;
    private boolean DEBUG = true;

    public FetchAvailableLocationsTask(Context context, List<String> availableMarkets) {
        this.context = context;
        this.availableMarkets = availableMarkets;
    }

    @Override
    protected List<String> doInBackground(String... params) {
        try {
            // Fetch available locations from spotify api
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            TracksPager results = spotify.searchTracks(context.getString(R.string.default_artist));
            int size = results.tracks.items.size();
            Log.d(LOG_TAG, String.format("size: %s", size));
            if (size > 0) {
                availableMarkets = results.tracks.items.get(0).album.available_markets;
            }
        } finally {
            // TODO: Handle errors
        }
        return availableMarkets;
    }

}