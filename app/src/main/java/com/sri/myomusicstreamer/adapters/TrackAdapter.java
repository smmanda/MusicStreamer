package com.sri.myomusicstreamer.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sri.myomusicstreamer.R;
import com.sri.myomusicstreamer.Utility;
import com.sri.myomusicstreamer.parcelable.TrackParcelable;

import java.util.List;


/**
 * Created by smanda on 5/28/15.
 */
public class TrackAdapter extends ArrayAdapter<TrackParcelable> {
    private List<TrackParcelable> tracksList;
    private Context context;
    private final String LOG_TAG = TrackAdapter.class.getSimpleName();

    public TrackAdapter(Context context, List<TrackParcelable> tracksList) {
        super(context, 0, tracksList);
        this.context = context;
        this.tracksList = tracksList;
    }

    public void updateList(List<TrackParcelable> trackParcelables) {
        this.tracksList = trackParcelables;
    }

    public List<TrackParcelable> getTracksList() {
        return tracksList;
    }

    public void setTracksList(List<TrackParcelable> tracksList) {
        this.tracksList = tracksList;
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public ImageView trackImageView;
        public TextView trackNameView;
        public TextView trackAlbumNameView;

        public ViewHolder(View view) {
            trackImageView = (ImageView) view.findViewById(R.id.imageViewTrack);
            trackNameView = (TextView) view.findViewById(R.id.textViewTrackName);
            trackAlbumNameView = (TextView) view.findViewById(R.id.textViewAlbumName);
        }
    }

    /**
     * Either create or fetch the existing view holder and create view for Track list.
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        TrackParcelable track = this.tracksList.get(position);
        List<String> imgURLs = track.getTrackAlbumImgUrls();
        ViewHolder viewHolder = null;
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            Log.d(LOG_TAG, "creating new Track view holder");
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);
            viewHolder = new ViewHolder(convertView);
            viewHolder.trackNameView = (TextView) convertView.findViewById(R.id.textViewTrackName);
            viewHolder.trackAlbumNameView = (TextView) convertView.findViewById(R.id.textViewAlbumName);
            viewHolder.trackImageView = (ImageView) convertView.findViewById(R.id.imageViewTrack);
            convertView.setTag(viewHolder);
        } else {
            //reuse the existing view holder
            Log.d(LOG_TAG, "reuse the existing Track view holder");
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        String trackName = track.getTrackName();
        String trackAlbumName = track.getTrackAlbumName();

        Log.d(LOG_TAG, String.format("track name:%s trackAlbumName:%s",trackName,trackAlbumName));
        viewHolder.trackNameView.setText(trackName);
        viewHolder.trackAlbumNameView.setText(trackAlbumName);
        int size = imgURLs.size();
        if (imgURLs.size() > 0) {
            int index = imgURLs.size() -1;
            Utility.loadImage(context, imgURLs, viewHolder.trackImageView, index,R.mipmap.ic_launcher);
        }
        else{
            Log.w(LOG_TAG, "No image for the track item exist");
        }
        return convertView;
    }
}
