package com.ridev.howtostudykorean.adapters;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;

import java.io.IOException;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;

/**
 * Created by Rica on 6/7/2017.
 */

 abstract class CustomLinkListener implements BetterLinkMovementMethod.OnLinkClickListener, MediaPlayer.OnPreparedListener {

    private MediaPlayer player;
    private AssetFileDescriptor fd;

    public void playAudio(String mURL, Context context) {
        try {
            ZipResourceFile expanFile = APKExpansionSupport.getAPKExpansionZipFile(context,1,0);
            fd = expanFile.getAssetFileDescriptor(mURL);
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());
            player.setOnPreparedListener(this);
            player.prepareAsync();
            Log.d("CustomUrlSpan"," found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
    }

}
