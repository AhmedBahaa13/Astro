package com.uni.astro.activitesfragments.videorecording;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlFilterGroup;
import com.daasuu.gpuv.player.GPUPlayerView;
import com.daasuu.gpuv.player.PlayerScaleType;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.uni.astro.simpleclasses.AppCompatLocaleActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.uni.astro.Constants;
import com.uni.astro.simpleclasses.FilterType;
import com.uni.astro.adapters.FilterAdapter;
import com.uni.astro.R;
import com.uni.astro.simpleclasses.Functions;
import com.uni.astro.simpleclasses.Variables;
import com.google.android.exoplayer2.Player;
import java.io.File;
import java.util.List;

public class PreviewVideoA extends AppCompatLocaleActivity implements Player.Listener {


    String videoUrl, isSoundSelected;
    GPUPlayerView gpuPlayerView;
    public static int selectPostion = 0;
    List<FilterType> filterTypes = FilterType.createFilterList();
    FilterAdapter adapter;
    RecyclerView recylerview;
    String draftFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(PreviewVideoA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(),false);
        setContentView(R.layout.activity_preview_video);


        Intent intent = getIntent();
        if (intent != null) {
            String fromWhere = intent.getStringExtra("fromWhere");
            if (fromWhere != null && fromWhere.equals("video_recording")) {
                isSoundSelected = intent.getStringExtra("isSoundSelected");
                draftFile = intent.getStringExtra("draft_file");
            } else {
                draftFile = intent.getStringExtra("draft_file");
            }
        }


        selectPostion = 0;
        videoUrl = Functions.getAppFolder(this)+Variables.outputfile2;
        findViewById(R.id.goBack).setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        });


        findViewById(R.id.next_btn).setOnClickListener(v -> {

            if (selectPostion == 0) {

                try {
                    Functions.copyFile(new File(Functions.getAppFolder(this)+Variables.outputfile2), new File(Functions.getAppFolder(this)+Variables.output_filter_file));
                    gotopostScreen();
                } catch (Exception e) {
                    e.printStackTrace();
                    Functions.printLog(Constants.TAG_, e.toString());
                    saveVideo(Functions.getAppFolder(this)+Variables.outputfile2, Functions.getAppFolder(this)+Variables.output_filter_file);
                }

            } else
                saveVideo(Functions.getAppFolder(this)+Variables.outputfile2, Functions.getAppFolder(this)+Variables.output_filter_file);

        });


        setPlayer(videoUrl);
        if (isSoundSelected != null && isSoundSelected.equals("yes")) {
            Functions.printLog("resp", "isSoundSelected : " + isSoundSelected);
            preparedAudio();
        }


        recylerview = findViewById(R.id.recylerview);

        adapter = new FilterAdapter(this, filterTypes, (view, postion, item) -> {

            selectPostion = postion;
            gpuPlayerView.setGlFilter(FilterType.createGlFilter(filterTypes.get(postion), getApplicationContext()));
            adapter.notifyDataSetChanged();

        });
        recylerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recylerview.setAdapter(adapter);


    }


    MediaPlayer audio;

    public void preparedAudio() {
        videoPlayer.setVolume(0);

        File file = new File(Functions.getAppFolder(this)+Variables.APP_HIDED_FOLDER + Variables.SelectedAudio_AAC);
        if (file.exists()) {
            audio = new MediaPlayer();
            try {
                audio.setDataSource(Functions.getAppFolder(this)+Variables.APP_HIDED_FOLDER + Variables.SelectedAudio_AAC);
                audio.prepare();
                audio.setLooping(true);


                videoPlayer.seekTo(0);
                videoPlayer.setPlayWhenReady(true);
                audio.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // this function will set the player to the current video
    ExoPlayer videoPlayer;
    public void setPlayer(String path) {

        videoPlayer =new ExoPlayer.Builder(PreviewVideoA.this).
                setTrackSelector(new DefaultTrackSelector(PreviewVideoA.this)).
                build();
        try {
            Uri videoURI = Uri.parse(path);
            MediaItem mediaItem = MediaItem.fromUri(videoURI);
            videoPlayer.setMediaItem(mediaItem);
            videoPlayer.prepare();
            videoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
            videoPlayer.addListener(this);
            videoPlayer.setPlayWhenReady(true);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build();
            videoPlayer.setAudioAttributes(audioAttributes, true);

        }catch (Exception e)
        {
            Log.d(Constants.TAG_,"Exception: getExoPlayerInit "+e);
        }




        gpuPlayerView = new GPUPlayerView(this);
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(path);
        String rotation = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

        if (rotation != null && rotation.equalsIgnoreCase("0")) {
            gpuPlayerView.setPlayerScaleType(PlayerScaleType.RESIZE_FIT_WIDTH);
        } else
            gpuPlayerView.setPlayerScaleType(PlayerScaleType.RESIZE_FIT_HEIGHT);



        PreviewVideoA.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gpuPlayerView.setSimpleExoPlayer(videoPlayer);
                gpuPlayerView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                ((MovieWrapperView) findViewById(R.id.layout_movie_wrapper)).addView(gpuPlayerView);
                gpuPlayerView.onResume();

                videoPlayer.prepare();
                videoPlayer.setPlayWhenReady(true);
            }
        });


    }


    // this is lifecyle of the Activity which is importent for play,pause video or relaese the player

    @Override
    protected void onRestart() {
        super.onRestart();

        if (videoPlayer != null) {
            videoPlayer.setPlayWhenReady(true);
        }
        if (audio != null) {
            audio.start();
        }

    }



    @Override
    protected void onResume() {
        super.onResume();
        if (videoPlayer != null) {
            videoPlayer.setPlayWhenReady(true);
            filterTypes = FilterType.createFilterList();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        try {
            if (videoPlayer != null) {
                videoPlayer.setPlayWhenReady(false);
            }
            if (audio != null) {
                audio.pause();
            }
        } catch (Exception e) {

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (videoPlayer != null) {
            videoPlayer.release();
        }

        if (audio != null) {
            audio.pause();
            audio.release();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (videoPlayer != null) {
            videoPlayer.setPlayWhenReady(true);
        }

    }


    // this function will add the filter to video and save that same video for post the video in post video screen
    public void saveVideo(String srcMp4Path, final String destMp4Path) {

        Functions.showDeterminentLoader(PreviewVideoA.this, false, false,true,PreviewVideoA.this.getString(R.string.rendering_));
        new GPUMp4Composer(srcMp4Path, destMp4Path)
                .filter(new GlFilterGroup(FilterType.createGlFilter(filterTypes.get(selectPostion), getApplicationContext())))
                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {

                        Functions.printLog("resp", "" + (int) (progress * 100));
                        Functions.showLoadingProgress((int) (progress * 100));


                    }

                    @Override
                    public void onCompleted() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Functions.cancelDeterminentLoader();

                                gotopostScreen();


                            }
                        });


                    }

                    @Override
                    public void onCanceled() {
                        Functions.printLog("resp", "onCanceled");
                    }

                    @Override
                    public void onFailed(Exception exception) {

                        Functions.printLog("resp", exception.toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Functions.cancelDeterminentLoader();

                                    Functions.showToast(PreviewVideoA.this, getString(R.string.try_again));
                                } catch (Exception e) {

                                }
                            }
                        });

                    }
                })
                .start();


    }


    // go to the post video screen from perview video screen
    public void gotopostScreen() {

        Intent intent = new Intent(PreviewVideoA.this, PostVideoA.class);
        intent.putExtra("draft_file", draftFile);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

    }


    @Override
    public void onBackPressed() {

        finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

    }


    @Override
    public void onPlaybackStateChanged(int playbackState) {
        if (playbackState == Player.STATE_ENDED) {

            videoPlayer.seekTo(0);
            videoPlayer.setPlayWhenReady(true);

            if (audio != null) {
                audio.seekTo(0);
                audio.start();
            }
        }
    }
}
