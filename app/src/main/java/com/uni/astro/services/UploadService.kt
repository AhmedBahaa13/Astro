package com.uni.astro.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.uni.astro.Constants
import com.uni.astro.R
import com.uni.astro.activitesfragments.HomeF
import com.uni.astro.activitesfragments.WatchVideosA
import com.uni.astro.apiclasses.FileUploader
import com.uni.astro.apiclasses.FileUploader.FileUploaderCallback
import com.uni.astro.mainmenu.MainMenuActivity
import com.uni.astro.models.UploadVideoModel
import com.uni.astro.simpleclasses.Functions
import com.uni.astro.simpleclasses.Variables
import org.json.JSONObject
import java.io.File

// this the background service which will upload the video into database
class UploadService : Service() {
    private val mBinder: IBinder = LocalBinder()
    private var mAllowRebind = false
    private var draft_file: String? = null
    private var duet_video_id: String? = null
    private var videopath: String? = null
    private var description: String? = null
    private var privacy_type: String? = null
    private var allow_comment: String? = null
    private var allow_duet: String? = null
    private var hashtags_json: String? = null
    private var users_json: String? = null
    var sharedPreferences: SharedPreferences? = null

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        return mAllowRebind
    }

    override fun onCreate() {
        sharedPreferences = Functions.getSharedPreference(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // get the all the selected date for send to server during the post video
        if (intent != null && intent.action == "startservice") {
            showNotification()
            videopath = intent.getStringExtra("uri")
            draft_file = intent.getStringExtra("draft_file")
            duet_video_id = intent.getStringExtra("duet_video_id")
            description = intent.getStringExtra("desc")
            privacy_type = intent.getStringExtra("privacy_type")
            allow_comment = intent.getStringExtra("allow_comment")
            allow_duet = intent.getStringExtra("allow_duet")
            hashtags_json = intent.getStringExtra("hashtags_json")
            users_json = intent.getStringExtra("mention_users_json")
            Log.d(
                Constants.TAG_, """
                 VideoInfo: \n
                 videoPath: $videopath
                 draft_file: $draft_file
                 duet_video_id: $duet_video_id
                 description: $description
                 privacy_type: $privacy_type
                 allow_comment: $allow_comment
                 allow_duet: $allow_duet
                 hashtags_json: $hashtags_json
                 users_json: $users_json
                 videoType: ${intent.getStringExtra("videoType")}
                 """.trimIndent()
            )

            Thread {
                val uploadModel = UploadVideoModel()
                uploadModel.userId = sharedPreferences!!.getString(Variables.U_ID, "0")
                uploadModel.soundId = Variables.selectedSoundId
                uploadModel.description = description
                uploadModel.privacyPolicy = privacy_type
                uploadModel.allowComments = allow_comment
                uploadModel.allowDuet = allow_duet
                uploadModel.hashtagsJson = hashtags_json
                uploadModel.usersJson = users_json

                if (duet_video_id != null) {
                    uploadModel.videoId = duet_video_id
                    uploadModel.duet = "" + intent.getStringExtra("duet_orientation")
                } else {
                    uploadModel.videoId = "0"
                }

                try {
                    if (intent.hasExtra("videoType")) {
                        if (intent.getStringExtra("videoType") != null &&
                            intent.getStringExtra("videoType") != "null" && intent.getStringExtra("videoType") == "Story"
                        ) {
                            uploadModel.videoType = "1"
                        } else {
                            uploadModel.videoType = "0"
                        }
                    } else {
                        uploadModel.videoType = "0"
                    }
                } catch (e: Exception) {
                    Log.d(Constants.TAG_, "Exception!: $e")
                    uploadModel.videoType = "0"
                }

                val fileUploader = FileUploader(File(videopath), applicationContext, uploadModel)
                fileUploader.SetCallBack(object : FileUploaderCallback {
                    override fun onError() {
                        //send error broadcast
                        Functions.printLog(Constants.TAG_, "Error...!")
                        stopForeground(true)
                        stopSelf()
                        sendBroadByName("uploadVideo")
                        sendBroadByName("newVideo")
                    }

                    override fun onFinish(responses: String) {
                        Functions.printLog(Constants.TAG_, "videoUploadRes: $responses")
                        try {
                            val jsonObject = JSONObject(responses)
                            val code = jsonObject.optInt("code", 0)
                            if (code == 200) {
                                Variables.reloadMyVideos = true
                                Variables.reloadMyVideosInner = true
                                deleteDraftFile()
                                Functions.showToast(
                                    this@UploadService,
                                    this@UploadService.getString(R.string.your_video_is_uploaded_successfully)
                                )
                            }
                        } catch (e: Exception) {
                            Functions.printLog(Constants.TAG_, "Exception!: $e")
                        }
                        stopForeground(true)
                        stopSelf()
                        sendBroadByName("uploadVideo")
                        sendBroadByName("newVideo")
                        // send finish broadcast
                    }

                    override fun onProgressUpdate(
                        currentpercent: Int,
                        totalpercent: Int,
                        msg: String
                    ) {
                        //send progress broadcast
                        if (currentpercent > 0) {
                            val bundle = Bundle()
                            bundle.putBoolean("isShow", true)
                            bundle.putInt("currentpercent", currentpercent)
                            bundle.putInt("totalpercent", totalpercent)

                            if (HomeF.uploadingCallback != null) {
                                HomeF.uploadingCallback.onResponce(bundle)
                            }

                            if (WatchVideosA.uploadingCallback != null) {
                                WatchVideosA.uploadingCallback.onResponce(bundle)
                            }
                        }
                    }
                })

                val map: MutableMap<String, String?> = HashMap()
                map["user_id"] = sharedPreferences!!.getString(Variables.U_ID, "0")
                map["sound_id"] = Variables.selectedSoundId
                map["description"] = description
                map["privacy_type"] = privacy_type
                map["allow_comments"] = allow_comment
                map["allow_duet"] = allow_duet
                map["hashtags_json"] = hashtags_json
                map["users_json"] = users_json
                map["videoType"] = uploadModel.videoType

                if (duet_video_id != null) {
                    map["video_id"] = duet_video_id
                    map["duet"] = "" + intent.getStringExtra("duet_orientation")
                } else {
                    map["video_id"] = "0"
                }

                Functions.printLog(Constants.TAG_, "VideoDataMap: $map")
            }.start()

        } else if (intent != null && intent.action == "stopservice") {
            stopForeground(true)
            stopSelf()
        }

        return START_STICKY
    }

    private fun sendBroadByName(action: String) {
        val intent = Intent(action)
        intent.setPackage(packageName)
        sendBroadcast(intent)
    }

    // this will show the sticky notification during uploading video
    @SuppressLint("InlinedApi")
    private fun showNotification() {
        val notificationIntent = Intent(this, MainMenuActivity::class.java)
        var pendingIntent: PendingIntent? = null
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                applicationContext, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        } else {
            PendingIntent.getActivity(
                applicationContext,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val CHANNEL_ID = "default"
        val CHANNEL_NAME = "Default"
        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultChannel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(defaultChannel)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle(this@UploadService.getString(R.string.uploading_video))
            .setContentText(this@UploadService.getString(R.string.please_wait_video_is_uploading))
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    this.resources,
                    android.R.drawable.stat_sys_upload
                )
            )
            .setContentIntent(pendingIntent)
        val notification = builder.build()
        startForeground(101, notification)
    }

    // delete the video from draft after post video
    fun deleteDraftFile() {
        try {
            if (draft_file != null) {
                val file = File(draft_file)
                file.delete()
            }
        } catch (e: Exception) {
            Functions.printLog(Constants.TAG_, e.toString())
        }
    }

    inner class LocalBinder : Binder() {
        val service: UploadService
            get() = this@UploadService
    }
}