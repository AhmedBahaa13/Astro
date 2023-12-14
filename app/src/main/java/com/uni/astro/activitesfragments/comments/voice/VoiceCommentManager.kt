package com.uni.astro.activitesfragments.comments.voice

import android.app.Activity
import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import android.widget.EditText
import com.uni.astro.Constants
import com.uni.astro.Constants.TAG_
import com.uni.astro.activitesfragments.comments.voice.MP3Encoder.encodeMP3ToBase64
import com.uni.astro.apiclasses.ApiLinks
import com.uni.astro.simpleclasses.Functions
import com.uni.astro.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class VoiceCommentManager(val videoId: String, context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var recordingTimer: Timer? = null
    private var recordingStartTime: Long = 0

    private var fileName: String

    init {
        val formattedDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ", Locale.ENGLISH).format(
            Calendar.getInstance().time
        )

        Log.d(TAG_, "SendVoiceComment: $formattedDate")
        fileName = Functions.getAppFolder(context) + videoId + ".mp3"
        Log.d(TAG_, "dir: $fileName")
    }


    fun uploadAudio(activity: Activity, callback: (String?) -> Unit) {
        val parameters = JSONObject()
        try {
            parameters.put("user_id", Functions.getSharedPreference(activity).getString(Variables.U_ID, "0"))
            parameters.put("video_id", videoId)
            parameters.put("comment", encodeMP3ToBase64(File(fileName)))
            parameters.put("type", "audio")
        } catch (e: Exception) {
            Log.d(TAG_, "uploadAudio: $e")
        }

        VolleyRequest.JsonPostRequest(activity, ApiLinks.postCommentOnVideo, parameters, Functions.getHeaders(activity)) { resp: String ->
            Log.d(TAG_, "uploadAudio: $resp")

            val code = try {
                val response = JSONObject(resp)
                response.optString("code")
            } catch (e: Exception) { "500" }

            callback(code)
        }
    }

    fun startVoiceRecording(activity: Activity) {
        try {
            if (mediaRecorder != null) {
                mediaRecorder!!.stop()
                mediaRecorder!!.reset()
                mediaRecorder!!.release()
                mediaRecorder = null
            }

            mediaRecorder = MediaRecorder()
            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder?.setOutputFile(fileName)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                if (mediaRecorder != null) mediaRecorder?.prepare()
            } catch (e: Exception) {
                Log.e(TAG_, "prepare() failed")
            }

            recordingTimerLimit(activity)
            if (mediaRecorder != null) mediaRecorder?.start()

        } catch (_: Exception) { }
    }

    private fun recordingTimerLimit(activity: Activity) {
        recordingStartTime = System.currentTimeMillis()
        recordingTimer = Timer()

        recordingTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentDuration = System.currentTimeMillis() - recordingStartTime
                if (currentDuration >= 5 * 60 * 1000) {
                    finishRecording(activity)
                    recordingTimer!!.cancel()
                }
            }
        }, 1000, 1000)
    }

    fun finishRecording(activity: Activity?): String {
        try {
            if (mediaRecorder != null) {
                mediaRecorder?.stop()
                mediaRecorder?.reset()
                mediaRecorder?.release()
                mediaRecorder = null

                //uploadAudio(activity);
            }
        } catch (_: Exception) { }

        return fileName
    }

    fun stopTimer() {
        Log.d(TAG_, "stopTimer: Stopping timer")
        try {
            if (mediaRecorder != null) {
                mediaRecorder?.stop()
                mediaRecorder?.reset()
                mediaRecorder?.release()
                mediaRecorder = null
            }
        } catch (_: Exception) { }
    }
}
