package com.uni.astro.simpleclasses

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import cat.ereza.customactivityoncrash.config.CaocConfig
import com.danikula.videocache.HttpProxyCacheServer
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.smartnsoft.backgrounddetector.BackgroundDetectorCallback
import com.smartnsoft.backgrounddetector.BackgroundDetectorHandler
import com.uni.astro.Constants
import com.uni.astro.R
import com.uni.astro.activitesfragments.CustomErrorActivity
import com.uni.astro.activitesfragments.livestreaming.model.LiveUserModel
import com.uni.astro.activitesfragments.livestreaming.rtc.AgoraEventHandler
import com.uni.astro.activitesfragments.livestreaming.rtc.EngineConfig
import com.uni.astro.activitesfragments.livestreaming.rtc.EventHandler
import com.uni.astro.activitesfragments.livestreaming.stats.StatsManager
import com.uni.astro.activitesfragments.livestreaming.utils.FileUtil
import com.uni.astro.activitesfragments.livestreaming.utils.PrefManager
import com.uni.astro.activitesfragments.spaces.voicecallmodule.openacall.model.CurrentUserSettings
import com.uni.astro.activitesfragments.spaces.voicecallmodule.openacall.model.WorkerThread
import com.uni.astro.models.UserOnlineModel
import com.volley.plus.VPackages.VolleyRequest
import io.agora.rtc.RtcEngine
import io.paperdb.Paper
import java.io.File


class Astro : Application(), ActivityLifecycleCallbacks, BackgroundDetectorHandler.OnVisibilityChangedListener {
    private var proxy: HttpProxyCacheServer? = null
    private var backgroundDetectorHandler: BackgroundDetectorHandler? = null


    override fun onCreate() {
        super.onCreate()

        initAdsView()
        VolleyRequest.initalizeSdk(this)
        appLevelContext = applicationContext
        registerActivityLifecycleCallbacks(this)

        backgroundDetectorHandler = BackgroundDetectorHandler(
            BackgroundDetectorCallback(
                BackgroundDetectorHandler.ON_ACTIVITY_RESUMED,
                this
            )
        )

        Fresco.initialize(
            applicationContext, ImagePipelineConfigUtils.getDefaultImagePipelineConfig(
                applicationContext
            )
        )

        Paper.init(applicationContext)
        FirebaseApp.initializeApp(applicationContext)
        addFirebaseToken()
        setUserOnline()

        if (leastRecentlyUsedCacheEvictor == null) {
            leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)
        }

        if (exoDatabaseProvider == null) {
            exoDatabaseProvider = StandaloneDatabaseProvider(applicationContext)
        }

        if (simpleCache == null) {
            simpleCache =
                SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor!!, exoDatabaseProvider!!)
            if (simpleCache!!.cacheSpace >= 400207768) {
                freeMemory()
            }
        }

        initCrashActivity()
        initConfig()
        Functions.createNoMediaFile(applicationContext)
    }

    private fun initAdsView() {
        MobileAds.initialize(this) { }
    }

    private fun initCrashActivity() {
        CaocConfig.Builder.create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
            .enabled(true)
            .showErrorDetails(true)
            .showRestartButton(true)
            .logErrorOnRestart(true)
            .trackActivities(true)
            .minTimeBetweenCrashesMs(2000)
            .restartActivity(CustomErrorActivity::class.java)
            .errorActivity(CustomErrorActivity::class.java)
            .apply()
    }

    private var onlineEventListener: ChildEventListener? = null
    private var streamingEventListener: ChildEventListener? = null
    var rootref: DatabaseReference? = null

    private fun setUserOnline() {
        rootref = FirebaseDatabase.getInstance().reference
        addOnlineListener()
        addStreamingListener()
    }

    private fun addStreamingListener() {
        if (streamingEventListener == null) {
            streamingEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (!TextUtils.isEmpty(snapshot.value.toString())) {
                        val model = snapshot.getValue(
                            LiveUserModel::class.java
                        )
                        allLiveStreaming[model!!.getStreamingId()] = model
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    if (!TextUtils.isEmpty(snapshot.value.toString())) {
                        val model = snapshot.getValue(
                            LiveUserModel::class.java
                        )
                        allLiveStreaming.remove(model!!.getStreamingId())
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            }
            rootref!!.child(Variables.LiveStreaming).addChildEventListener(streamingEventListener!!)
        }
    }

    private fun removeStreamingListener() {
        if (rootref != null && streamingEventListener != null) {
            rootref!!.child(Variables.LiveStreaming).removeEventListener(streamingEventListener!!)
            streamingEventListener = null
        }
    }

    private fun addOnlineListener() {
        if (onlineEventListener == null) {
            addOnlineStatus()
            onlineEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (!TextUtils.isEmpty(snapshot.value.toString())) {
                        val item = snapshot.getValue(
                            UserOnlineModel::class.java
                        )
                        allOnlineUser[item!!.getUserId()] = item
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    if (!TextUtils.isEmpty(snapshot.value.toString())) {
                        val item = snapshot.getValue(
                            UserOnlineModel::class.java
                        )
                        allOnlineUser.remove(item!!.getUserId())
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            }
            rootref!!.child(Variables.onlineUser).addChildEventListener(onlineEventListener!!)
        }
    }

    private fun removeOnlineListener() {
        if (rootref != null && onlineEventListener != null) {
            removeOnlineStatus()
            rootref!!.child(Variables.onlineUser).removeEventListener(onlineEventListener!!)
            onlineEventListener = null
        }
    }

    private fun removeOnlineStatus() {
        if (Functions.getSharedPreference(applicationContext)
                .getBoolean(Variables.IS_LOGIN, false)
        ) {
            rootref!!.child(Variables.onlineUser).child(
                Functions.getSharedPreference(
                    applicationContext
                ).getString(Variables.U_ID, "0")!!
            )
                .removeValue().addOnCompleteListener {
                    Log.d(
                        Constants.TAG_, "removeOnlineStatus: " + Functions.getSharedPreference(
                            applicationContext
                        ).getString(Variables.U_ID, "0")
                    )
                }
        }
    }

    private fun addOnlineStatus() {
        if (Functions.getSharedPreference(applicationContext)
                .getBoolean(Variables.IS_LOGIN, false)
        ) {
            val onlineModel = UserOnlineModel()
            onlineModel.setUserId(
                Functions.getSharedPreference(
                    applicationContext
                ).getString(Variables.U_ID, "0")
            )
            onlineModel.setUserName(
                Functions.getSharedPreference(
                    applicationContext
                ).getString(Variables.U_NAME, "")
            )
            onlineModel.setUserPic(
                Functions.getSharedPreference(
                    applicationContext
                ).getString(Variables.U_PIC, "")
            )
            rootref!!.child(Variables.onlineUser).child(
                Functions.getSharedPreference(
                    applicationContext
                ).getString(Variables.U_ID, "0")!!
            ).onDisconnect().removeValue()
            rootref!!.child(Variables.onlineUser).child(
                Functions.getSharedPreference(
                    applicationContext
                ).getString(Variables.U_ID, "0")!!
            ).keepSynced(true)
            rootref!!.child(Variables.onlineUser).child(
                Functions.getSharedPreference(
                    applicationContext
                ).getString(Variables.U_ID, "0")!!
            ).setValue(onlineModel)
                .addOnCompleteListener {
                    Log.d(
                        Constants.TAG_,
                        "addOnlineStatus: " + onlineModel.getUserId()
                    )
                }
        }
    }

    private fun addFirebaseToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                Log.d(Constants.TAG_, "token: $token")
                val editor = Functions.getSharedPreference(
                    applicationContext
                ).edit()
                editor.putString(Variables.DEVICE_TOKEN, "" + token)
                editor.apply()
            })
    }

    private fun newProxy(): HttpProxyCacheServer {
        return HttpProxyCacheServer.Builder(applicationContext)
            .maxCacheSize((1024 * 1024 * 1024).toLong())
            .maxCacheFilesCount(50)
            .cacheDirectory(
                File(
                    Functions.getAppFolder(
                        applicationContext
                    ) + "videoCache"
                )
            )
            .build()
    }

    // check how much memory is available for cache video
    private fun freeMemory() {
        try {
            val dir = cacheDir
            deleteDir(dir)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        System.runFinalization()
        Runtime.getRuntime().gc()
        System.gc()
    }

    // delete the cache if it is full
    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }

    private var mRtcEngine: RtcEngine? = null
    private val mGlobalConfig = EngineConfig()
    private val mHandler = AgoraEventHandler()
    private val mStatsManager = StatsManager()
    private fun initConfig() {
        try {
            mRtcEngine = RtcEngine.create(applicationContext, getString(R.string.agora_app_id), mHandler)
            mRtcEngine!!.setChannelProfile(io.agora.rtc.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            mRtcEngine!!.enableVideo()
            mRtcEngine!!.setLogFile(FileUtil.initializeLogFile(applicationContext))

        } catch (e: Exception) {
            e.printStackTrace()
        }
        val pref = PrefManager.getPreferences(
            applicationContext
        )

        mGlobalConfig.videoDimenIndex = pref.getInt(
            com.uni.astro.activitesfragments.livestreaming.Constants.PREF_RESOLUTION_IDX,
            com.uni.astro.activitesfragments.livestreaming.Constants.DEFAULT_PROFILE_IDX
        )

        val showStats = pref.getBoolean(
            com.uni.astro.activitesfragments.livestreaming.Constants.PREF_ENABLE_STATS,
            false
        )

        mGlobalConfig.setIfShowVideoStats(false)
        mStatsManager.enableStats(false)
        mGlobalConfig.mirrorLocalIndex = pref.getInt(
            com.uni.astro.activitesfragments.livestreaming.Constants.PREF_MIRROR_LOCAL,
            0
        )
        mGlobalConfig.mirrorRemoteIndex = pref.getInt(
            com.uni.astro.activitesfragments.livestreaming.Constants.PREF_MIRROR_REMOTE,
            0
        )
        mGlobalConfig.mirrorEncodeIndex = pref.getInt(
            com.uni.astro.activitesfragments.livestreaming.Constants.PREF_MIRROR_ENCODE,
            0
        )
    }

    fun engineConfig(): EngineConfig {
        return mGlobalConfig
    }

    fun rtcEngine(): RtcEngine? {
        return mRtcEngine
    }

    fun statsManager(): StatsManager {
        return mStatsManager
    }

    fun registerEventHandler(handler: EventHandler?) {
        mHandler.addHandler(handler)
    }

    fun removeEventHandler(handler: EventHandler?) {
        mHandler.removeHandler(handler)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onTerminate() {
        super.onTerminate()
        unregisterActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        backgroundDetectorHandler!!.onActivityResumed(activity)
        Functions.RegisterConnectivity(activity) { _, response ->
            if (response.equals("disconnected", ignoreCase = true)) {
                removeOnlineListener()
                removeStreamingListener()
                Functions.showToastOnTop(
                    activity,
                    null,
                    activity.getString(R.string.your_network_is_unstable)
                )
            } else {
                addOnlineListener()
                addStreamingListener()
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        backgroundDetectorHandler!!.onActivityPaused(activity)
        Functions.unRegisterConnectivity(activity)
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onAppGoesToBackground(context: Context?) {
        Log.d(Constants.TAG_, "onAppGoesToBackground")
        removeOnlineListener()
        removeStreamingListener()
    }

    override fun onAppGoesToForeground(context: Context?) {
        Log.d(Constants.TAG_, "onAppGoesToForeground")
        addOnlineListener()
        addStreamingListener()
    }


    private var mWorkerThread: WorkerThread? = null

    @Synchronized
    fun initWorkerThread() {
        if (mWorkerThread == null) {
            mWorkerThread = WorkerThread(applicationContext)
            mWorkerThread?.start()
            mWorkerThread?.waitForReady()
        }
    }

    @Synchronized
    fun getWorkerThread(): WorkerThread? {
        return mWorkerThread
    }

    @Synchronized
    fun deInitWorkerThread() {
        mWorkerThread?.exit()

        try {
            mWorkerThread?.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        mWorkerThread = null
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        @JvmField
        var appLevelContext: Context? = null
        var simpleCache: SimpleCache? = null
        var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor? = null
        var exoDatabaseProvider: DatabaseProvider? = null
        var exoPlayerCacheSize = (100 * 1024 * 1024).toLong()
        var allOnlineUser = HashMap<String, UserOnlineModel?>()

        @JvmField
        var allLiveStreaming = HashMap<String, LiveUserModel?>()

        @JvmField
        var mAudioSettings: CurrentUserSettings = CurrentUserSettings()

        // below code is for cache the videos in local
        @JvmStatic
        fun getProxy(context: Context): HttpProxyCacheServer {
            val app = context.applicationContext as Astro
            return try {
                if (app.proxy == null) app.newProxy().also { app.proxy = it } else app.proxy!!
            } catch (e: Exception) {
                app.newProxy()
            }
        }
    }
}