package com.ridev.howtostudykorean;

import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class AudioPlayerActivity extends AppCompatActivity {

    SimpleExoPlayerView exoPlayerView;
    SimpleExoPlayer exoPlayer;
    private String audioPath;
    private String filePath;
    private StorageManager storageManager;
    private String fullAudioPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        audioPath = getIntent().getStringExtra("audioPath");

        exoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exoplayerView);

        TrackSelector trackSelector = new DefaultTrackSelector();
        LoadControl loadControl = new DefaultLoadControl();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this,trackSelector,loadControl);
        exoPlayerView.setPlayer(exoPlayer);

        Uri uri = CustomAPEZProvider.buildUri(audioPath);
        String userAgent = Util.getUserAgent(this,"HTSK");
        MediaSource mediaSource = new ExtractorMediaSource(uri,new DefaultDataSourceFactory(this,userAgent),new DefaultExtractorsFactory(),null,null);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.stop();
        exoPlayer.release();
        exoPlayer = null;
    }
}
