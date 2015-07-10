package com.sri.musicstreamer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sri.musicstreamer.parcelable.TrackParcelable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A Music player fragment containing a  view.
 */
public class MusicPlayerFragment extends Fragment implements ImageButton.OnClickListener {
    private Handler mHandler = new Handler();
    private SeekBar seekBar;
    private ImageButton playPauseImgButton;
    private ImageButton imgButtonLeft;
    private ImageButton imgButtonRight;
    private Chronometer chronometerStart;
    private MediaPlayer mediaPlayer;
    private boolean isPaused = false;
    private long timeWhenStopped = 0;
    private ArrayList<TrackParcelable> tracksList;
    private int trackIndex;
    private TextView artistNameTV;
    private TextView albumNameTV;
    private TextView trackNameTV;
    private ImageView albumImage;
    private final String LOG_TAG = MusicPlayerFragment.class.getSimpleName();


    public MusicPlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_music_player, container, false);
        tracksList = getActivity().getIntent().getParcelableArrayListExtra("trackInfo");
        trackIndex = getActivity().getIntent().getExtras().getInt("position");
        TrackParcelable trackParcelable = tracksList.get(trackIndex);

        artistNameTV = (TextView) rootView.findViewById(R.id.textViewMPArtistName);
        albumNameTV = (TextView) rootView.findViewById(R.id.textViewMPAlbumName);
        trackNameTV = (TextView) rootView.findViewById(R.id.textViewMPTrackName);
        albumImage = (ImageView) rootView.findViewById(R.id.imageViewMPTrack);

        seekBar = (SeekBar) rootView.findViewById(R.id.seekBarMP);
        playPauseImgButton = (ImageButton) rootView.findViewById(R.id.imageButtonPlayPause);
        chronometerStart = (Chronometer) rootView.findViewById(R.id.chronometerStart);

        imgButtonLeft = (ImageButton) rootView.findViewById(R.id.imageButtonLeft);
        imgButtonRight = (ImageButton) rootView.findViewById(R.id.imageButtonRight);
        imgButtonRight.setOnClickListener(this);
        imgButtonLeft.setOnClickListener(this);

        setTrackInfo(trackParcelable);

        playTrack(trackParcelable.getTrackPreviewUrl());
        return rootView;
    }

    /**
     * set the track info
     * @param trackParcelable
     */
    private void setTrackInfo(TrackParcelable trackParcelable) {
        artistNameTV.setText(trackParcelable.getTrackArtistName());
        albumNameTV.setText(trackParcelable.getTrackAlbumName());
        trackNameTV.setText(trackParcelable.getTrackName());
        List<String> imgURLs = trackParcelable.getTrackAlbumImgUrls();
        int size = imgURLs.size();
        if (size > 0) {
            int index = imgURLs.size() - 2;
            Utility.loadImage(getActivity().getApplicationContext(),imgURLs,albumImage,index,R.drawable.ic_not_available);
        } else {
            Log.w(LOG_TAG, "No image for the track item exist");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaPlayer.stop();
        chronometerStart.stop();
        timeWhenStopped = 0;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageButtonLeft) {
            //play the previous track
            playPreviousTrack();

        } else if (v.getId() == R.id.imageButtonRight) {
            // Next Track
            playNextTrack();

        }
    }

    /**
     * play previous track
     */
    private void playPreviousTrack() {
        if (trackIndex - 1 < 0) {
            trackIndex = tracksList.size() - 1;
        } else
            trackIndex--;

        TrackParcelable trackParcelable = tracksList.get(trackIndex);
        setTrackInfo(trackParcelable);

        //stop the current track
        mediaPlayer.stop();
        chronometerStart.stop();
        playPauseImgButton.setImageResource(R.drawable.ic_pause_arrow_black);
        isPaused = false;
        playTrack(trackParcelable.getTrackPreviewUrl());
    }

    /**
     * play the next track
     */
    private void playNextTrack() {
        if (trackIndex + 1 == tracksList.size()) {
            trackIndex = 0;
        } else {
            trackIndex++;
        }
        TrackParcelable trackParcelable = tracksList.get(trackIndex);
        setTrackInfo(trackParcelable);

        //stop the current track
        mediaPlayer.stop();
        chronometerStart.stop();
        playPauseImgButton.setImageResource(R.drawable.ic_pause_arrow_black);
        isPaused = false;
        playTrack(trackParcelable.getTrackPreviewUrl());
    }

    /**
     * play the music track
     * @param playtrackUrl
     */
    private void playTrack(String playtrackUrl) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(playtrackUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    chronometerStart.setBase(SystemClock.elapsedRealtime());
                    chronometerStart.start();
                    int duration = mp.getDuration();

                    seekBar.setMax(duration);
                    seekBar.setProgress(mp.getCurrentPosition());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int startTime = mediaPlayer.getCurrentPosition();
                            seekBar.setProgress(startTime);
                            mHandler.postDelayed(this, 100);
                        }
                    }, 100);
                }
            });

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress);

//                        chronometerStart.setBase(mediaPlayer
//                                .getCurrentPosition());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    timeWhenStopped = chronometerStart.getBase() - SystemClock.elapsedRealtime();
                    chronometerStart.stop();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    chronometerStart.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    chronometerStart.start();
                }
            });

            playPauseImgButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isPaused) {
                        //play the music player
                        play();
                    } else {
                        // pause the music player
                        pause();
                    }

                }

                /**
                 * pause the music player
                 */

                private void pause() {
                    playPauseImgButton.setImageResource(R.drawable.ic_play_arrow_black);
                    isPaused = true;
                    mediaPlayer.pause();
                    timeWhenStopped = chronometerStart.getBase() - SystemClock.elapsedRealtime();
                    chronometerStart.stop();
                }

                /**
                 * play the music
                 */
                private void play() {
                    playPauseImgButton.setImageResource(R.drawable.ic_pause_arrow_black);
                    isPaused = false;
                    mediaPlayer.start();
                    chronometerStart.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    chronometerStart.start();
                }
            });

            /**
             * when the media player is completed, stop the chronometer, reset the values and start the next track
             */
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.stop();
                    chronometerStart.stop();
                    // seekBar.setEnabled(false); doesn't look good
                    timeWhenStopped = 0;
                    playNextTrack();
                }
            });

        } catch (IOException e) {
            Log.e(LOG_TAG,String.format("io: %s",e));
        }
    }
}
