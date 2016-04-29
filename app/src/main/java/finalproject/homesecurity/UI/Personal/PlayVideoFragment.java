package finalproject.homesecurity.UI.Personal;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.exomedia.EMVideoView;

import finalproject.homesecurity.R;

/**
 * Created by Robbie on 02/04/2016.
 *
 * THIS FRAGMENT TAKES THE URL OF THE VIDEO PASSED IN AND PLAYS IT USING THE OPEN SOURCE EXOMEDIA LIBRARY
 * WHICH ACTS AS A WRAPPER AROUND THE EXOPLAYER LIBRARY
 */

/***************************************************************************************
 *    Title: ExoMedia
 *    Author: Brian Wernick
 *    Date: 2/4/2016
 *    Code version: 1
 *    Availability: https://github.com/brianwernick/ExoMedia
 *
 ***************************************************************************************/
public class PlayVideoFragment extends Fragment implements MediaPlayer.OnPreparedListener{
    protected EMVideoView emVideoView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.play_video_layout,
                container, false);

        emVideoView = (EMVideoView) view.findViewById(R.id.video_play_activity_video_view);
        emVideoView.setOnPreparedListener(this);

        emVideoView.setVideoURI(Uri.parse(getArguments().getString("video"))); //video url was passed as a parameter to this fragment
        return view;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Starts the video playback as soon as it is ready
        emVideoView.start();
    }
}
