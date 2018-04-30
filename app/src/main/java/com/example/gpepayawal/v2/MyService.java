package com.example.gpepayawal.v2;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorEventListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.VolumeProviderCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * Created by gpepayawal on 2/11/18.
 */

public class MyService extends Service {
    private MediaSessionCompat mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(this, "MyService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(PlaybackStateCompat.STATE_PLAYING, 0, 0).build()); //you simulate a player which plays something.

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int volume_level= audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.i("stream", "service " + String.valueOf(volume_level));

        //this will only work on Lollipop and up, see https://code.google.com/p/android/issues/detail?id=224134
        final VolumeProviderCompat myVolumeProvider = new VolumeProviderCompat(VolumeProviderCompat.VOLUME_CONTROL_RELATIVE, 100,50) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

            @Override
            public void onAdjustVolume(int direction) {
                MainActivity activity = MainActivity.instance;
                if(direction == -1){
                    Log.i("VOLUME", String.valueOf(this.getCurrentVolume()));
                    int currVolume = this.getCurrentVolume();
                    this.onSetVolumeTo(currVolume--);
                    this.setCurrentVolume(currVolume--);

                    if(this.getCurrentVolume() == 20) {
                        /*if (AppStatus.getInstance(activity).isOnline()) activity.getLocation();
                        else activity.locationHelper.getLocation(activity, activity.locationResult);*/
                        activity.locationHelper.getLocation(activity, activity.locationResult);
                        if(activity.latitude == 0.0d && activity.longitude == 0.0d) activity.locationHelper.getLocation(activity, activity.locationResult);
                    }

                    if(this.getCurrentVolume() == -10) {
                        vibrator.vibrate(1000);
                        activity.sendSilentSOSMessage();
                        try {
                            Thread.sleep(12000);
                        } catch (Exception e) {}
                        if (activity != null) {
                            activity.setupDropCall();
                        } else Log.i("checker", "activity is null");
                        this.onSetVolumeTo(50);
                        this.setCurrentVolume(50);
                        activity.isSOSTriggered= true;
                    }
                }
                else if(direction == 1){
                    Log.i("VOLUME", String.valueOf(this.getCurrentVolume()));
                    int currVolume = this.getCurrentVolume();
                    this.onSetVolumeTo(currVolume++);
                    this.setCurrentVolume(currVolume++);

                    if(this.getCurrentVolume() == 80) {
                        activity.locationHelper.getLocation(activity, activity.locationResult);
                        if(activity.latitude == 0.0d && activity.longitude == 0.0d) activity.locationHelper.getLocation(activity, activity.locationResult);

                        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                        if(audioManager.isWiredHeadsetOn()){
                            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                            alertDialog.setTitle("Alert");
                            alertDialog.setMessage("Your headphones are still on!");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }
                    }

                    if(this.getCurrentVolume() == 110) {
                        vibrator.vibrate(1000);
                        activity.sendLoudSOSMessage();
                        activity.playPanicAlert();
                        try {
                            Thread.sleep(12000);
                        } catch (Exception e) {}
                        if (activity != null) {
                            activity.setupDropCall();
                        }
                        this.onSetVolumeTo(50);
                        this.setCurrentVolume(50);
                        activity.isSOSTriggered = true;
                    }
                }
                else if(direction == 0) { int dirctn = 0; }
            }

        };
        mediaSession.setPlaybackToRemote(myVolumeProvider);
        mediaSession.setActive(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
    }
}
