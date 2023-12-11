package com.uni.astro.activitesfragments.comments.voice;

import static com.uni.astro.Constants.TAG_;
import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.EditText;
import com.uni.astro.apiclasses.ApiLinks;
import com.uni.astro.simpleclasses.Functions;
import com.uni.astro.simpleclasses.Variables;
import com.volley.plus.VPackages.VolleyRequest;
import org.json.JSONObject;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class VoiceCommentManager {
    private static String videoId;
    private static String fileName;
    private MediaRecorder mediaRecorder;
    private static Context context;
    private EditText commentField;

    public VoiceCommentManager(String videoId, Context context) {
        this.context = context;
        this.videoId = videoId;
        final String formattedDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ", Locale.ENGLISH).format(
                Calendar.getInstance().getTime()
        );
        Log.d(TAG_, "SendVoiceComment: " + formattedDate);
        fileName =
                Functions.getAppFolder(context) + videoId + ".mp3";
    }
    private long recordingStartTime;
    private Timer recordingTimer;

    public void startVoiceRecording(Activity activity) {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
            }

            mediaRecorder = new MediaRecorder();

            if (mediaRecorder != null)
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            if (mediaRecorder != null)
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            if (mediaRecorder != null)
                mediaRecorder.setOutputFile(fileName);

            if (mediaRecorder != null)
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            try {
                if (mediaRecorder != null)
                    mediaRecorder.prepare();
            } catch (Exception e) {
                Log.e("resp", "prepare() failed");
            }

            recordingTimerLimit(activity);
            if (mediaRecorder != null)
                mediaRecorder.start();

        } catch (Exception e) {

        }
    }
    private void recordingTimerLimit(Activity activity){
        recordingStartTime = System.currentTimeMillis();
        recordingTimer = new Timer();
        recordingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long currentDuration = System.currentTimeMillis() - recordingStartTime;
                if (currentDuration >= 5*60*1000){
                    stopRecording(activity);
                    recordingTimer.cancel();
                }
            }
        }, 1000, 1000);
    }
    public void stopRecording(Activity activity) {
        try {


            stopTimerWithoutRecorder();
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                uploadAudio(activity);
            }

        } catch (Exception e) {

        }
    }

    public void stopTimerWithoutRecorder() {
        try {
            commentField.setText(null);

        } catch (Exception e) {

        }
    }

    public static void uploadAudio(Activity activity) {
        Log.d(TAG_, "uploadAudio: uploading voice comment");
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(activity).getString(Variables.U_ID, "0"));
            parameters.put("video_id", videoId);
            parameters.put("comment", MP3Encoder.encodeMP3ToBase64(new File(fileName)));
            parameters.put("type", "audio");
        } catch (Exception e) {
            Log.d(TAG_, "uploadAudio: " + e);
        }
        VolleyRequest.JsonPostRequest(activity, ApiLinks.postCommentOnVideo, parameters, Functions.getHeaders(activity), (resp) -> {
        });
    }
    public void stopTimer() {
        Log.d(TAG_, "stopTimer: Stopping timer");
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
            }
        } catch (Exception e) { }
    }
}
