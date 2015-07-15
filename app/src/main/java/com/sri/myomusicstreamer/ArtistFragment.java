package com.sri.myomusicstreamer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.sri.myomusicstreamer.adapters.ArtistAdapter;
import com.sri.myomusicstreamer.parcelable.ArtistParcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * A placeholder fragment containing a simple view.
 */
public class ArtistFragment extends Fragment implements View.OnKeyListener {
    private EditText editArtistsSearch;
    private ArtistAdapter artistAdapter;
    private TextView textViewNoArtist;
    private ListView listView;
    private final String LOG_TAG = ArtistFragment.class.getSimpleName();
    private List<ArtistParcelable> mArtists = Collections.emptyList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.artistfragment_main, container, false);
        listView = (ListView) rootView.findViewById(R.id.listview_artist);
        mArtists = new ArrayList<ArtistParcelable>();
        artistAdapter = new ArtistAdapter(getActivity(), mArtists);
        listView.setAdapter(artistAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, String.format("position: %s",position));
                ArtistAdapter artistAdapter = (ArtistAdapter) parent.getAdapter();
                ArtistParcelable artistParcelable = artistAdapter.getListArtistsData().get(position);
                Intent intent = new Intent(getActivity(), TracksActivity.class)
                        .putExtra("id", artistParcelable.getSpotifyId());
                startActivity(intent);
            }
        });

        editArtistsSearch = (EditText) rootView.findViewById(R.id.artistsSearch);
        textViewNoArtist = (TextView) rootView.findViewById(R.id.textViewNoArtistFound);
        textViewNoArtist.setVisibility(View.GONE);
        editArtistsSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "show the soft keypad");
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(editArtistsSearch, 0);

            }
        });

        editArtistsSearch.setOnKeyListener(this);
        return rootView;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                (keyCode == KeyEvent.KEYCODE_ENTER)) {
            // code to hide the soft keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(editArtistsSearch.getApplicationWindowToken(), 0);

            String editText = editArtistsSearch.getText()
                    .toString();
            if (editText == null || editText.isEmpty())
                Log.d(LOG_TAG, "Artist name is empty");
            else
                searchArtists(editText);
            return true;
        }
        return false;
    }

    private void searchArtists(String artistsName) {
        // code to hide the soft keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editArtistsSearch.getApplicationWindowToken(), 0);
        FetchArtistsTask artistsTask = new FetchArtistsTask(getActivity(), artistAdapter);
        try {
            mArtists = artistsTask.execute(artistsName).get();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG,String.format("InterruptedException: %s",e));
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.e(LOG_TAG,String.format("ExecutionException: %s",e));
            e.printStackTrace();
        }
        if (mArtists.size() == 0) {
            textViewNoArtist.setText(this.getString(R.string.no_artists_found));
            textViewNoArtist.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);

        } else {
            listView.setVisibility(View.VISIBLE);
            textViewNoArtist.setVisibility(View.GONE);
        }
    }
}
