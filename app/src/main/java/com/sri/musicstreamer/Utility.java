package com.sri.musicstreamer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by smanda on 5/1/15.
 */
public class Utility {
    /**
     * get fetch the preferred location from activity context
     * @param context
     * @return
     */
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_country_code), context.getString(R.string.pref_loc_label_us));
    }

    /**
     * Load image from picasso
     * @param context
     * @param imgURLs
     * @param albumImage
     * @param index
     * @param placeholder
     */
    public static void loadImage(Context context,List<String> imgURLs,ImageView albumImage,int index,int placeholder) {
        //first cancel the previous request
        Picasso.with(context)
                .cancelRequest(albumImage);
        //request the smallest image
        Picasso.with(context)
                .load(imgURLs.get(index))
                .placeholder(placeholder)
                .into(albumImage);
    }
}
