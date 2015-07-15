package com.sri.myomusicstreamer;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sri.myomusicstreamer.adapters.TrackAdapter;
import com.sri.myomusicstreamer.parcelable.TrackParcelable;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder Track fragment containing a simple view.
 */
public class TracksActivityFragment extends Fragment {
    private List<TrackParcelable> mTracks;
    private TrackAdapter trackAdapter;
    private final String LOG_TAG = TracksActivityFragment.class.getSimpleName();

    public TracksActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracks, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listViewTracks);
        String id = getActivity().getIntent().getStringExtra("id");
        Log.d(LOG_TAG, String.format("spotify id: %s", id));

        mTracks = new ArrayList<TrackParcelable>();
        trackAdapter = new TrackAdapter(getActivity(), mTracks);
        searchTracks(id);

        listView.setAdapter(trackAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TrackAdapter trackAdapter = (TrackAdapter) parent.getAdapter();
                ArrayList<TrackParcelable> list = (ArrayList<TrackParcelable>) trackAdapter.getTracksList();

                Intent intent = new Intent(getActivity(), MusicPlayer.class)
                        .putExtra("trackInfo", list)
                        .putExtra("position", position);
                startActivity(intent);
            }
        });
        return rootView;
    }

    private void searchTracks(String trackName) {
        FetchTracksTask tracksTask = new FetchTracksTask(getActivity(), trackAdapter);
        tracksTask.execute(trackName);
    }
}
