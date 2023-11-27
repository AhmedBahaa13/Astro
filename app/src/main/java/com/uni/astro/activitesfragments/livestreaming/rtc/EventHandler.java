package com.uni.astro.activitesfragments.livestreaming.rtc;

import io.agora.rtc.IRtcEngineEventHandler;

public interface EventHandler {

    int EVENT_TYPE_ON_USER_AUDIO_MUTED = 7;

    int EVENT_TYPE_ON_SPEAKER_STATS = 8;

    int EVENT_TYPE_ON_AGORA_MEDIA_ERROR = 9;

    int EVENT_TYPE_ON_AUDIO_QUALITY = 10;

    int EVENT_TYPE_ON_APP_ERROR = 13;

    int EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED = 18;

    void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed);

    void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats);

    void onJoinChannelSuccess(String channel, int uid, int elapsed);

    void onUserOffline(int uid, int reason);

    void onUserJoined(int uid, int elapsed);

    void onLastmileQuality(int quality);

    void onLastmileProbeResult(IRtcEngineEventHandler.LastmileProbeResult result);

    void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats);

    void onRtcStats(IRtcEngineEventHandler.RtcStats stats);

    void onNetworkQuality(int uid, int txQuality, int rxQuality);

    void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats);

    void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats);


}
