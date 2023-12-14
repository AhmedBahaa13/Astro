package com.uni.astro.activitesfragments.spaces.voicecallmodule.openacall;


import com.uni.astro.activitesfragments.spaces.voicecallmodule.openacall.model.CurrentUserSettings;
import com.uni.astro.activitesfragments.spaces.voicecallmodule.openacall.model.EngineConfig;
import com.uni.astro.activitesfragments.spaces.voicecallmodule.openacall.model.MyEngineEventHandler;
import com.uni.astro.activitesfragments.spaces.voicecallmodule.openacall.model.WorkerThread;
import com.uni.astro.simpleclasses.Astro;

import io.agora.rtc.RtcEngine;

public class VoiceStreamingNonUiBase {

    Astro application;

    public VoiceStreamingNonUiBase(Astro application) {
        this.application=application;
        application.initWorkerThread();
    }


    protected RtcEngine rtcEngine() {
        return application.getWorkerThread().getRtcEngine();
    }

    protected final WorkerThread worker() {
        return application.getWorkerThread();
    }

    protected final EngineConfig config() {
        return application.getWorkerThread().getEngineConfig();
    }

    protected final MyEngineEventHandler event() {
        return application.getWorkerThread().eventHandler();
    }

    protected CurrentUserSettings vSettings() {
        return Astro.mAudioSettings;
    }

}
