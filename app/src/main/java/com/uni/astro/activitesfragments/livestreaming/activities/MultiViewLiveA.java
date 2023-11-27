package com.uni.astro.activitesfragments.livestreaming.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.uni.astro.R;
import com.uni.astro.activitesfragments.livestreaming.adapter.MultiCastStatAdapter;
import com.uni.astro.activitesfragments.livestreaming.fragments.MultipleStreamerListF;
import com.uni.astro.activitesfragments.livestreaming.model.LiveUserModel;
import com.uni.astro.activitesfragments.livestreaming.stats.LocalStatsData;
import com.uni.astro.activitesfragments.livestreaming.stats.RemoteStatsData;
import com.uni.astro.activitesfragments.livestreaming.stats.StatsData;
import com.uni.astro.activitesfragments.livestreaming.stats.StatsManager;
import com.uni.astro.activitesfragments.livestreaming.ui.VideoGridContainer;
import com.uni.astro.simpleclasses.Functions;
import com.uni.astro.simpleclasses.Astro;
import com.uni.astro.simpleclasses.Variables;
import com.uni.astro.simpleclasses.VerticalViewPager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class MultiViewLiveA extends RtcBaseMultiviewA implements View.OnClickListener {

    VerticalViewPager viewpager;
    MultiCastStatAdapter pagerSatetAdapter;
    ArrayList<LiveUserModel> dataList = new ArrayList<>();

    RelativeLayout tabNoUser;
    public VideoGridContainer mVideoGridContainer;
    public VideoEncoderConfiguration.VideoDimensions mVideoDimension;
    DatabaseReference rootref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(), false);
        setContentView(R.layout.activity_multi_view_live);

        InitControl();
    }



    ChildEventListener childEventListener;
    private void callStreamerList() {

        if(childEventListener==null){

            childEventListener=new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    Functions.printLog(com.uni.astro.Constants.tag,"snap:"+snapshot.toString());

                    if(snapshot.exists()) {
                        LiveUserModel model = snapshot.getValue(LiveUserModel.class);
                        if(model.streamingId!=null) {

                            Functions.printLog(com.uni.astro.Constants.tag, "model.streamingId:" + model.streamingId);

                            boolean isAlreadyexist=false;
                            for (int i = 0; i < dataList.size(); i++) {
                                LiveUserModel item = dataList.get(i);
                                if (model.streamingId.equalsIgnoreCase(item.streamingId)) {
                                    isAlreadyexist=true;
                                    break;
                                }
                            }

                            if ((!isAlreadyexist && model.getOnlineType() != null) && model.getOnlineType().equals("multicast")) {
                                Functions.printLog(com.uni.astro.Constants.tag, "added streamingId:" + model.streamingId);

                                dataList.add(model);

                                        MultipleStreamerListF fragment = new MultipleStreamerListF(model, MultiViewLiveA.this);
                                        Bundle bundle = new Bundle();
                                        fragment.setArguments(bundle);
                                        pagerSatetAdapter.addFragment(fragment);

                                        pagerSatetAdapter.refreshStateSet(false);
                                        pagerSatetAdapter.notifyDataSetChanged();
                                    }



                            }

                        }
                    }


                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    LiveUserModel model = snapshot.getValue(LiveUserModel.class);

                    if(model.streamingId!=null) {
                        for (int i = 0; i < dataList.size(); i++) {
                            LiveUserModel item = dataList.get(i);
                            if (model.streamingId.equalsIgnoreCase(item.streamingId)) {
                                dataList.remove(item);
                                pagerSatetAdapter.removeFragment(i);
                                pagerSatetAdapter.refreshStateSet(false);
                                pagerSatetAdapter.notifyDataSetChanged();
                                break;
                            }
                        }

                        if (dataList.isEmpty()) {
                            tabNoUser.setVisibility(View.VISIBLE);
                            viewpager.setVisibility(View.GONE);
                        } else {
                            tabNoUser.setVisibility(View.GONE);
                            viewpager.setVisibility(View.VISIBLE);
                        }
                    }

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            rootref.child("LiveStreamingUsers").addChildEventListener(childEventListener);
        }
    }


    public void removedListerner(){
        if(childEventListener!=null){
            rootref.child("LiveStreamingUsers").removeEventListener(childEventListener);
        }
    }

    public String userId, userName, userPicture;
    public int userRole;
    public String onlineType,description,secureCode,bookingId;


    private void InitControl() {
        Intent bundle = getIntent();
        if (bundle != null) {
            userId = bundle.getStringExtra("user_id");
            userName = bundle.getStringExtra("user_name");
            userPicture = bundle.getStringExtra("user_picture");
            userRole = bundle.getIntExtra("user_role", Constants.CLIENT_ROLE_AUDIENCE);
            onlineType=bundle.getStringExtra("onlineType");
            description=bundle.getStringExtra("description");
            secureCode=bundle.getStringExtra("secureCode");
            if (onlineType.equals("oneTwoOne"))
            {
                bookingId=getIntent().getStringExtra("bookingId");
            }
        }

        rootref= FirebaseDatabase.getInstance().getReference();
        viewpager=findViewById(R.id.viewpager);
        tabNoUser=findViewById(R.id.tabNoUser);
        setTabs();
        getPreviousList();
    }

    private void getPreviousList() {
        ArrayList<LiveUserModel> tempList = new ArrayList<>();
        tempList= (ArrayList<LiveUserModel>) getIntent().getSerializableExtra("dataList");
        dataList.addAll(tempList);

        for (LiveUserModel item:dataList){
            MultipleStreamerListF fragment = new MultipleStreamerListF( item, this);
            Bundle bundle=new Bundle();
            fragment.setArguments(bundle);
            pagerSatetAdapter.addFragment(fragment);
        }

        pagerSatetAdapter.refreshStateSet(false);
        pagerSatetAdapter.notifyDataSetChanged();

        viewpager.setCurrentItem(getIntent().getIntExtra("position",0),true);


        callStreamerList();

    }


    public void setTabs() {
        pagerSatetAdapter = new MultiCastStatAdapter(getSupportFragmentManager(),MultiViewLiveA.this);
        viewpager.setAdapter(pagerSatetAdapter);
        viewpager.setOffscreenPageLimit(1);
        viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


     @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onUserOffline(final int uid, int reason) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeRemoteUser(uid);
            }
        });
    }

    @Override
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Functions.printLog(com.uni.astro.Constants.tag, "onFirstRemoteVideoDecoded");
                renderRemoteUser(uid);
            }
        });
    }

    private void renderRemoteUser(int uid) {
        Functions.printLog(com.uni.astro.Constants.tag, "renderRemoteUser "+uid);
        SurfaceView surface = prepareRtcVideo(uid);
        mVideoGridContainer.addUserVideoSurface(uid, surface);
    }

    private void removeRemoteUser(int uid) {
        removeRtcVideo(uid);
        mVideoGridContainer.removeUserVideo(uid);
    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setWidth(mVideoDimension.width);
        data.setHeight(mVideoDimension.height);
        data.setFramerate(stats.sentFrameRate);
    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {

        if (!(getChannelName().equals("")) && (getChannelName()!=null))
        {
        }

        if (!statsManager().isEnabled()) return;

        LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
        if (data == null) return;

        data.setLastMileDelay(stats.lastmileDelay);
        data.setVideoSendBitrate(stats.txVideoKBitRate);
        data.setVideoRecvBitrate(stats.rxVideoKBitRate);
        data.setAudioSendBitrate(stats.txAudioKBitRate);
        data.setAudioRecvBitrate(stats.rxAudioKBitRate);
        data.setCpuApp(stats.cpuAppUsage);
        data.setCpuTotal(stats.cpuAppUsage);
        data.setSendLoss(stats.txPacketLossRate);
        data.setRecvLoss(stats.rxPacketLossRate);

    }

    // check the network quality
    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        if (!statsManager().isEnabled()) return;

        StatsData data = statsManager().getStatsData(uid);
        if (data == null) return;

        data.setSendQuality(statsManager().qualityToString(txQuality));
        data.setRecvQuality(statsManager().qualityToString(rxQuality));
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setWidth(stats.width);
        data.setHeight(stats.height);
        data.setFramerate(stats.rendererOutputFrameRate);
        data.setVideoDelay(stats.delay);
    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {

        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setAudioNetDelay(stats.networkTransportDelay);
        data.setAudioNetJitter(stats.jitterBufferDelay);
        data.setAudioLoss(stats.audioLossRate);
        data.setAudioQuality(statsManager().qualityToString(stats.quality));
    }


    public void switchCamera()
    {
        rtcEngine().switchCamera();
    }

    public void muteLocalAudioStream(boolean isAudioActivated)
    {
        rtcEngine().muteLocalAudioStream(isAudioActivated);
        if (isAudioActivated)
        {
            rtcEngine().disableAudio();
        }
        else
        {
            rtcEngine().enableAudio();
        }
    }

    public void setBeautyEffectOptions(boolean isbeautyActivated)
    {
        rtcEngine().setBeautyEffectOptions(isbeautyActivated,
                com.uni.astro.activitesfragments.livestreaming.Constants.DEFAULT_BEAUTY_OPTIONS);
    }

    public void stopBroadcast(int role)
    {
        rtcEngine().setClientRole(role);
        removeRtcVideo(Functions.parseInterger(Functions.getSharedPreference(this).getString(Variables.U_ID,"")));
        statsManager().clearAllData();
        rtcEngine().disableVideo();
    }

    public SurfaceView startBroadcast(String userId,int role)
    {
        Astro astro = (Astro)getApplication();
        astro.engineConfig().setChannelName(userId);
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        rtcEngine().enableVideo();
        return  prepareRtcVideo(Functions.parseInterger(Functions.getSharedPreference(this).getString(Variables.U_ID,"")));

    }

    public VideoEncoderConfiguration.VideoDimensions getconfigDimenIndex()
    {
        return com.uni.astro.activitesfragments.livestreaming.Constants.VIDEO_DIMENSIONS[
                config().getVideoDimenIndex()];
    }

    public StatsManager setStatsManager()
    {
        return statsManager();
    }

    public void setClientRole(int userRole)
    {
        rtcEngine().setClientRole(userRole);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        statsManager().clearAllData();
    }

    @Override
    protected void onDestroy() {
        removedListerner();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

    }


    @Override
    public void onBackPressed() {
        finish();
    }
}