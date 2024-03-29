package com.uni.astro.activitesfragments.spaces.voicecallmodule.openacall;


import com.uni.astro.activitesfragments.livestreaming.rtc.EngineConfig;
import com.uni.astro.activitesfragments.livestreaming.rtc.EventHandler;
import com.uni.astro.activitesfragments.livestreaming.stats.StatsManager;
import com.uni.astro.simpleclasses.Astro;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public class VoiceStreamingNonUiBase implements EventHandler {

    Astro application;

    public VoiceStreamingNonUiBase(Astro application) {
        this.application=application;
    }


    protected RtcEngine rtcEngine() {
        return application.rtcEngine();
    }


    protected EngineConfig config() {
        return application.engineConfig();
    }

    protected StatsManager statsManager() {
        return application.statsManager();
    }

    protected void event(EventHandler handler) {
        application.registerEventHandler(handler);
    }

    protected void removeRtcEventHandler(EventHandler handler) {
        if (handler!=null)
        {
            application.removeEventHandler(handler);
        }

    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {

    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {

    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {

    }

    @Override
    public void onUserOffline(int uid, int reason) {

    }

    @Override
    public void onUserJoined(int uid, int elapsed) {

    }

    @Override
    public void onLastmileQuality(final int quality) {

    }

    @Override
    public void onLastmileProbeResult(final IRtcEngineEventHandler.LastmileProbeResult result) {

    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {

    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {

    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {

    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {

    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {

    }


}
