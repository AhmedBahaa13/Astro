package com.uni.astro.activitesfragments.spaces.voicecallmodule.openacall.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;


import com.uni.astro.Constants;
import com.uni.astro.R;
import com.uni.astro.simpleclasses.Functions;

import java.io.File;

import io.agora.rtc.RtcEngine;

public class WorkerThread extends Thread {

    private final Context mContext;

    private static final int ACTION_WORKER_THREAD_QUIT = 0X1010; // quit this thread

    private static final int ACTION_WORKER_JOIN_CHANNEL = 0X2010;

    private static final int ACTION_WORKER_LEAVE_CHANNEL = 0X2011;

    private static final class WorkerThreadHandler extends Handler {

        private WorkerThread mWorkerThread;

        WorkerThreadHandler(WorkerThread thread) {
            this.mWorkerThread = thread;
        }

        public void release() {
            mWorkerThread = null;
        }

        @Override
        public void handleMessage(Message msg) {
            if (this.mWorkerThread == null) {
                Functions.printLog(Constants.tag,"handler is already released! " + msg.what);
                return;
            }

            switch (msg.what) {
                case ACTION_WORKER_THREAD_QUIT:
                    mWorkerThread.exit();
                    break;
                case ACTION_WORKER_JOIN_CHANNEL:
                    String[] data = (String[]) msg.obj;
                    mWorkerThread.joinChannel(data[0], msg.arg1);
                    break;
                case ACTION_WORKER_LEAVE_CHANNEL:
                    String channel = (String) msg.obj;
                    mWorkerThread.leaveChannel(channel);
                    break;
            }
        }
    }

    private WorkerThreadHandler mWorkerHandler;

    private boolean mReady;

    public final void waitForReady() {
        while (!mReady) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Functions.printLog(Constants.tag,"wait for " + WorkerThread.class.getSimpleName());
        }
    }

    @Override
    public void run() {
        Functions.printLog(Constants.tag,"start to run");
        Looper.prepare();

        mWorkerHandler = new WorkerThreadHandler(this);

        ensureRtcEngineReadyLock();

        mReady = true;

        // enter thread looper
        Looper.loop();
    }

    private RtcEngine mRtcEngine;

    public final void joinChannel(final String channel, int uid) {
        if (Thread.currentThread() != this) {
            Functions.printLog(Constants.tag,"joinChannel() - worker thread asynchronously " + channel + " " + uid);
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_JOIN_CHANNEL;
            envelop.obj = new String[]{channel};
            envelop.arg1 = uid;
            mWorkerHandler.sendMessage(envelop);
            return;
        }

        ensureRtcEngineReadyLock();
        mRtcEngine.joinChannel(null, channel, "OpenVCall", uid);

        mEngineConfig.mChannel = channel;

        Functions.printLog(Constants.tag,"joinChannel " + channel + " " + uid);
    }

    public final void leaveChannel(String channel) {
        if (Thread.currentThread() != this) {
            Functions.printLog(Constants.tag,"leaveChannel() - worker thread asynchronously " + channel);
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_LEAVE_CHANNEL;
            envelop.obj = channel;
            mWorkerHandler.sendMessage(envelop);
            return;
        }

        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }

        mEngineConfig.reset();
        Functions.printLog(Constants.tag,"leaveChannel " + channel);
    }

    private EngineConfig mEngineConfig;

    public final EngineConfig getEngineConfig() {
        return mEngineConfig;
    }

    private final MyEngineEventHandler mEngineEventHandler;

    public static String getDeviceID(Context context) {
        // XXX according to the API docs, this value may change after factory reset
        // use Android id as device id
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private RtcEngine ensureRtcEngineReadyLock() {
        if (mRtcEngine == null) {
            String appId = mContext.getString(R.string.agora_app_id);
            if (TextUtils.isEmpty(appId)) {
                throw new RuntimeException("NEED TO use your App ID, get your own ID at https://dashboard.agora.io/");
            }
            try {
                // Creates an RtcEngine instance
                mRtcEngine = RtcEngine.create(mContext, appId, mEngineEventHandler.mRtcEventHandler);
            } catch (Exception e) {
                Functions.printLog(Constants.tag,Log.getStackTraceString(e));
                throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
            }

            /*
              Sets the channel profile of the Agora RtcEngine.
              The Agora RtcEngine differentiates channel profiles and applies different optimization
              algorithms accordingly. For example, it prioritizes smoothness and low latency for a
              video call, and prioritizes video quality for a video broadcast.
             */
            mRtcEngine.setChannelProfile(io.agora.rtc.Constants.CHANNEL_PROFILE_COMMUNICATION);

            /*
              Enables the onAudioVolumeIndication callback at a set time interval to report on which
              users are speaking and the speakers' volume.
              Once this method is enabled, the SDK returns the volume indication in the
              onAudioVolumeIndication callback at the set time interval, regardless of whether any user
              is speaking in the channel.
            */
            mRtcEngine.enableAudioVolumeIndication(200, 3, false); // 200 ms
            mRtcEngine.setLogFile(Environment.getExternalStorageDirectory()
                    + File.separator + mContext.getPackageName() + "/log/agora-rtc.log");
        }
        return mRtcEngine;
    }

    public MyEngineEventHandler eventHandler() {
        return mEngineEventHandler;
    }

    public RtcEngine getRtcEngine() {
        return mRtcEngine;
    }

    /**
     * call this method to exit
     * should ONLY call this method when this thread is running
     */
    public final void exit() {
        if (Thread.currentThread() != this) {
            Functions.printLog(Constants.tag,"exit() - exit app thread asynchronously");
            mWorkerHandler.sendEmptyMessage(ACTION_WORKER_THREAD_QUIT);
            return;
        }

        mReady = false;

        // TODO should remove all pending(read) messages

        Functions.printLog(Constants.tag,"exit() > start");

        // exit thread looper
        Looper.myLooper().quit();

        mWorkerHandler.release();

        Functions.printLog(Constants.tag,"exit() > end");
    }

    public WorkerThread(Context context) {
        this.mContext = context;

        this.mEngineConfig = new EngineConfig();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        this.mEngineConfig.mUid = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_UID, 0);

        this.mEngineEventHandler = new MyEngineEventHandler(mContext, this.mEngineConfig);
    }
}
