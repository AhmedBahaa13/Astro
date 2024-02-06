package com.uni.astro.activitesfragments.spaces.voicecallmodule.openacall;

import android.util.Log;

import com.uni.astro.Constants;
import com.uni.astro.activitesfragments.livestreaming.rtc.EventHandler;
import com.uni.astro.activitesfragments.spaces.voicecallmodule.openacall.model.ConstantApp;
import com.uni.astro.interfaces.FragmentCallBack;
import com.uni.astro.simpleclasses.Functions;
import com.uni.astro.simpleclasses.Astro;

import io.agora.rtc.IRtcEngineEventHandler;

public class VoiceStreamingNonUiChat extends VoiceStreamingNonUiBase implements EventHandler {

    private volatile boolean mAudioMuted = true;
    private volatile int mAudioRouting = -1;

    Astro application;
    String channelName,userId;
    boolean isCallStart=false;

    public VoiceStreamingNonUiChat(Astro application) {
        super(application);
        this.application=application;
    }


    public String getChannelName() {
        return channelName;
    }

    public String getUid() {
        return userId;
    }

    public void setChannelNameAndUid(String channelName, String userId) {
        this.channelName = channelName;
        this.userId=userId;
        Functions.printLog(Constants.tag,"channelName:"+this.channelName+" UserID:"+this.userId);
        config().setUid(userId);
    }

    public void startStream(FragmentCallBack voiceControler)
    {
        initConfiguration();
    }

    protected void initConfiguration() {
        isCallStart=true;
        event(this);


        rtcEngine().disableVideo();

        rtcEngine().setDefaultAudioRoutetoSpeakerphone(true);
        rtcEngine().adjustRecordingSignalVolume(100);
        rtcEngine().adjustPlaybackSignalVolume(100);
        rtcEngine().adjustAudioMixingVolume(100);

        rtcEngine().joinChannel(null,channelName,"OpenVCall",Integer.parseInt(config().getUid()));
        Log.d(Constants.tag,"Connected Channel ID: "+channelName);

        onEnableSpeakerSwitch();

    }

    protected void removeConfiguration() {
        isCallStart=false;
        doLeaveChannel();
        removeRtcEventHandler(this);
    }



    private void doLeaveChannel() {
        rtcEngine().leaveChannel();
    }

    public void quitCall() {
        Functions.printLog(Constants.tag,"quitCall ");
        removeConfiguration();
    }

    public void muteVoiceCall() {
        Functions.printLog(Constants.tag,"muteVoiceCall");
        mAudioMuted=true;
        rtcEngine().setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_AUDIENCE);
        rtcEngine().muteLocalAudioStream(mAudioMuted);

        if(mAudioRouting==0){
            onDisableSpeakerSwitch();
        }
        else{
            onEnableSpeakerSwitch();
        }

    }

    public void enableVoiceCall(){
        Functions.printLog(Constants.tag,"enableVoiceCall");
        mAudioMuted=false;
        rtcEngine().setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER);
        rtcEngine().muteLocalAudioStream(mAudioMuted);

        if(mAudioRouting==0){
            onDisableSpeakerSwitch();
        }
        else{
            onEnableSpeakerSwitch();
        }
    }


    public boolean ismAudioMuted() {
        return mAudioMuted;
    }

    @Override
    public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
        String msg = "onJoinChannelSuccess " + channel + "=>  UserId:" + (uid) + " => " + elapsed;
        Functions.printLog(Constants.tag,msg);
        rtcEngine().muteLocalAudioStream(mAudioMuted);
    }



    @Override
    public void onUserOffline(int uid, int reason) {
        String msg = "onUserOffline " + (uid) + " " + reason;
        Functions.printLog(Constants.tag,msg);

    }

//    @Override
//    public void onExtraCallback(final int type, final Object... data) {
//
//        if (isCallStart)
//        {
//            doHandleExtraCallback(type, data);
//        }
//
//    }

    private void doHandleExtraCallback(int type, Object... data) {
        int peerUid;
        boolean muted;

        switch (type) {
            case EventHandler.EVENT_TYPE_ON_USER_AUDIO_MUTED: {
                peerUid = (Integer) data[0];
                muted = (boolean) data[1];

                Functions.printLog(Constants.tag,"mute: " + (peerUid & 0xFFFFFFFFL) + " " + muted);
                break;
            }

            case EventHandler.EVENT_TYPE_ON_AUDIO_QUALITY: {
                break;
            }

            case EventHandler.EVENT_TYPE_ON_SPEAKER_STATS: {
                IRtcEngineEventHandler.AudioVolumeInfo[] infos = (IRtcEngineEventHandler.AudioVolumeInfo[]) data[0];


                if (infos.length == 1 && infos[0].uid == 0) { // local guy, ignore it
                    break;
                }

                break;
            }

            case EventHandler.EVENT_TYPE_ON_APP_ERROR: {
                int subType = (int) data[0];
                if (subType == ConstantApp.AppError.NO_NETWORK_CONNECTION) {
                    Functions.printLog(Constants.tag,"msgNoNetworkConnection " + subType);
                }
                break;
            }

            case EventHandler.EVENT_TYPE_ON_AGORA_MEDIA_ERROR: {
                int error = (int) data[0];
                String description = (String) data[1];
                Functions.printLog(Constants.tag,error + " " + description);
                break;
            }

            case EventHandler.EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED: {
                notifyHeadsetPlugged((int) data[0]);
                break;
            }
        }
    }


    public void notifyHeadsetPlugged(final int routing) {
        Functions.printLog(Constants.tag,"notifyHeadsetPlugged " + routing);
        mAudioRouting = routing;
        if(mAudioRouting==0){
            onDisableSpeakerSwitch();
        }
        else{
            onEnableSpeakerSwitch();
        }

    }




    public void onEnableSpeakerSwitch() {
        rtcEngine().setEnableSpeakerphone(true);

    }
    public void onDisableSpeakerSwitch() {
        rtcEngine().setEnableSpeakerphone(false);
    }



}
