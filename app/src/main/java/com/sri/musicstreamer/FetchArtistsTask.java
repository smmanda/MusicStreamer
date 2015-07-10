package com.sri.musicstreamer;

import android.os.AsyncTask;

/**
 * Created by smanda on 5/28/15.
 */

import android.content.Context;
import android.util.Log;


import com.sri.musicstreamer.adapters.ArtistAdapter;
import com.sri.musicstreamer.parcelable.ArtistParcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;

public class FetchArtistsTask extends AsyncTask<String, Void, List<ArtistParcelable>> {

    private final String LOG_TAG = FetchArtistsTask.class.getSimpleName();
    private final Context context;
    private ArtistAdapter artistAdapter;
    private boolean DEBUG = true;

    public FetchArtistsTask(Context context,ArtistAdapter arrayAdapter) {
        this.context = context;
        this.artistAdapter = arrayAdapter;
    }

    @Override
    protected List<ArtistParcelable> doInBackground(String... params) {

        // if there is no params return null
        if (params.length == 0) {
            return null;
        }
        List<ArtistParcelable> mArtists = new ArrayList<>();
        String artistsQuery = params[0];
        try {
            // Fetch artists info from spotify api.
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager results = spotify.searchArtists(artistsQuery);
            Log.d(LOG_TAG, "results:"+results);
            Pager<Artist> artists = results.artists;
            for (Artist artist : artists.items) {
                Log.d(LOG_TAG,String.format("artist name: %s",artist.name));
                    mArtists.add(new ArtistParcelable(artist));
            }
        } finally {
                //TODO: Handle errors
        }
        return mArtists;
    }

    @Override
    protected void onPostExecute(List<ArtistParcelable> artistParcelables) {
        if (artistParcelables != null && artistAdapter != null) {
            Log.d(LOG_TAG, "clearing the artist adapter and setting the new values");
            this.artistAdapter.getListArtistsData().clear();
            for(ArtistParcelable artistParcelable: artistParcelables)
                this.artistAdapter.getListArtistsData().add(artistParcelable);

            this.artistAdapter.notifyDataSetChanged();
        }
    }
}