package com.sri.myomusicstreamer;

import android.graphics.Color;
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
import android.widget.Toast;

import com.sri.myomusicstreamer.parcelable.TrackParcelable;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;

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

    private TextView mLockStateView;
    private TextView mTextView;

    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {

        // onConnect() is called whenever a Myo has been connected.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            // Set the text color of the text view to cyan when a Myo connects.
            mTextView.setTextColor(Color.CYAN);
        }

        // onDisconnect() is called whenever a Myo has been disconnected.
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            // Set the text color of the text view to red when a Myo disconnects.
            mTextView.setTextColor(Color.RED);
        }

        // onArmSync() is called whenever Myo has recognized a Sync Gesture after someone has put it on their
        // arm. This lets Myo know which arm it's on and which way it's facing.
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            mTextView.setText(myo.getArm() == Arm.LEFT ? R.string.arm_left : R.string.arm_right);
        }

        // onArmUnsync() is called whenever Myo has detected that it was moved from a stable position on a person's arm after
        // it recognized the arm. Typically this happens when someone takes Myo off of their arm, but it can also happen
        // when Myo is moved around on the arm.
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            mTextView.setText(R.string.hello_world);
        }

        // onUnlock() is called whenever a synced Myo has been unlocked. Under the standard locking
        // policy, that means poses will now be delivered to the listener.
        @Override
        public void onUnlock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.unlocked);
        }

        // onLock() is called whenever a synced Myo has been locked. Under the standard locking
        // policy, that means poses will no longer be delivered to the listener.
        @Override
        public void onLock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.locked);
        }

        // onOrientationData() is called whenever a Myo provides its current orientation,
        // represented as a quaternion.
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));

            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (myo.getXDirection() == XDirection.TOWARD_ELBOW) {
                roll *= -1;
                pitch *= -1;
            }

            // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
            mTextView.setRotation(roll);
            mTextView.setRotationX(pitch);
            mTextView.setRotationY(yaw);
        }

        // onPose() is called whenever a Myo provides a new pose.
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    mTextView.setText(getString(R.string.hello_world));
                    break;
                case REST:
                case DOUBLE_TAP:
                    int restTextId = R.string.hello_world;
                    switch (myo.getArm()) {
                        case LEFT:
                            restTextId = R.string.arm_left;
                            break;
                        case RIGHT:
                            restTextId = R.string.arm_right;
                            break;
                    }
                    mTextView.setText(getString(restTextId));
                    break;
                case FIST:
                    mTextView.setText(getString(R.string.pose_fist));
                    if (!isPaused) {
                        //pause the music player
                        pause();
                       // isPaused = true;
                    }
                    break;
                case WAVE_IN:
                    mTextView.setText(getString(R.string.pose_wavein));
                    playPreviousTrack();
                    break;
                case WAVE_OUT:
                    mTextView.setText(getString(R.string.pose_waveout));
                    playNextTrack();
                    break;
                case FINGERS_SPREAD:
                    mTextView.setText(getString(R.string.pose_fingersspread));
                    if (isPaused) {
                        //play the music player
                        play();
                       // isPaused = false;
                    }
                    break;
            }

            if (pose != Pose.UNKNOWN && pose != Pose.REST) {
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);

                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
                myo.unlock(Myo.UnlockType.TIMED);
            }
        }
    };




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
        chronometerStart = (Chronometer) rootView.findViewById(R.id.chronometerStart);

        playPauseImgButton = (ImageButton) rootView.findViewById(R.id.imageButtonPlayPause);
        imgButtonLeft = (ImageButton) rootView.findViewById(R.id.imageButtonLeft);
        imgButtonRight = (ImageButton) rootView.findViewById(R.id.imageButtonRight);
        imgButtonRight.setOnClickListener(this);
        imgButtonLeft.setOnClickListener(this);
        playPauseImgButton.setOnClickListener(this);

        mLockStateView = (TextView) rootView.findViewById(R.id.lock_state);
        mTextView = (TextView) rootView.findViewById(R.id.textShowGesture);

        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(getActivity(), getActivity().getPackageName())) {
            // We can't do anything with the Myo device if the Hub can't be initialized, so exit.
            Toast.makeText(getActivity(), "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
            getActivity().finish();

        }

        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);

        setTrackInfo(trackParcelable);

        playTrack(trackParcelable.getTrackPreviewUrl());
        return rootView;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Activity is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);

        if (getActivity().isFinishing()) {
            // The Activity is finishing, so shutdown the Hub. This will disconnect from the Myo.
            Hub.getInstance().shutdown();
        }
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

        } else if (v.getId() == R.id.imageButtonPlayPause) {
           playorPauseMusic();
        }
    }

    private void playorPauseMusic() {
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
        if(mediaPlayer != null)
             mediaPlayer.stop();
        if(chronometerStart != null)
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
        if(mediaPlayer != null)
            mediaPlayer.stop();
        if(chronometerStart != null)
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

//            playPauseImgButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (isPaused) {
//                        //play the music player
//                        play();
//                    } else {
//                        // pause the music player
//                        pause();
//                    }
//
//                }
//
//
//            });

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
