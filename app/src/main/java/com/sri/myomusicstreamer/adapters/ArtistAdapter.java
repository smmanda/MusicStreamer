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
import com.sri.myomusicstreamer.parcelable.ArtistParcelable;

import java.util.List;

/**
 * Created by smanda on 5/28/15.
 */
public class ArtistAdapter extends ArrayAdapter<ArtistParcelable> {
    private List<ArtistParcelable> listArtistsData;
    private Context context;
    private final String LOG_TAG = ArtistAdapter.class.getSimpleName();

    public ArtistAdapter(Context context, List<ArtistParcelable> artists) {
        super(context, 0, artists);
        this.context = context;
        listArtistsData = artists;
    }

    public void updateList(List<ArtistParcelable> artistParcelables) {
        this.listArtistsData = artistParcelables;
    }

    public List<ArtistParcelable> getListArtistsData() {
        return listArtistsData;
    }

    public void setListArtistsData(List<ArtistParcelable> listArtistsData) {
        this.listArtistsData = listArtistsData;
    }

    /**
     * Cache of the children views for a Artist list item.
     */
    public static class ViewHolder {
        public ImageView artistImageView;
        public TextView artistNameView;

        public ViewHolder(View view) {
            artistImageView = (ImageView) view.findViewById(R.id.imageViewArtist);
            artistNameView = (TextView) view.findViewById(R.id.textViewTitle);
        }
    }

    /**
     * Either create or fetch the existing view holder and create view for Artist list.
     * @param position
     * @param convertView
     * @param parent
     * @return
     */

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ArtistParcelable artist = this.listArtistsData.get(position);
        List<String> imgURLs = artist.getImgURLS();
        ViewHolder viewHolder = null;
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            Log.d(LOG_TAG, "creating new artist view holder");
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
            viewHolder = new ViewHolder(convertView);
            viewHolder.artistNameView = (TextView) convertView.findViewById(R.id.textViewTitle);
            viewHolder.artistImageView = (ImageView) convertView.findViewById(R.id.imageViewArtist);
            convertView.setTag(viewHolder);
        } else {
            //reuse the existing view holder
            Log.d(LOG_TAG, "reuse the existing artist view holder");
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String artistName = artist.getArtistName();
        Log.d(LOG_TAG, String.format("track name:%s ",artistName));
        // setting the artist Name
        viewHolder.artistNameView.setText(artistName);
        int size = imgURLs.size();
        if (imgURLs.size() > 0) {
            int index = imgURLs.size() -1;
            Utility.loadImage(context,imgURLs,viewHolder.artistImageView,index,R.mipmap.ic_launcher);
        }
        else{
            Log.w(LOG_TAG, "No image for the artist item exist");
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
