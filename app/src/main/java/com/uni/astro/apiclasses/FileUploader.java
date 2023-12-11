package com.uni.astro.apiclasses;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.uni.astro.Constants;
import com.uni.astro.models.UploadVideoModel;
import com.uni.astro.simpleclasses.Functions;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileUploader {

    long filesize = 0L;
    UploadVideoModel uploadModel;
    private FileUploaderCallback mFileUploaderCallback;


    public FileUploader(File file, Context context, UploadVideoModel uploadModel) {
        this.uploadModel = uploadModel;
        filesize = file.length();

        InterfaceFileUpload interfaceFileUpload = ApiClient.getRetrofitInstance(context)
                .create(InterfaceFileUpload.class);

        Log.d(Constants.TAG_, "UploadFile: " + file.getAbsolutePath());
        PRRequestBody mFile = new PRRequestBody(file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("video",
                file.getName(), mFile);


        RequestBody PrivacyType = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.getPrivacyPolicy());

        RequestBody UserId = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.getUserId());

        RequestBody SoundId = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.getSoundId());

        RequestBody AllowComments = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.getAllowComments());

        RequestBody Description = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.getDescription());

        RequestBody AllowDuet = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.getAllowDuet());

        RequestBody UsersJson = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.getUsersJson());

        RequestBody HashtagsJson = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.getHashtagsJson());

        RequestBody storyJson = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.getVideoType());


        Call<Object> fileUpload;
        if (uploadModel.getVideoId().equalsIgnoreCase("0")) {
            RequestBody videoId = RequestBody.create(
                    okhttp3.MultipartBody.FORM, uploadModel.getVideoId());

            fileUpload = interfaceFileUpload.UploadFile(fileToUpload, PrivacyType, UserId,
                    SoundId, AllowComments, Description, AllowDuet, UsersJson, HashtagsJson, storyJson, videoId);
        } else {
            RequestBody videoId = RequestBody.create(
                    okhttp3.MultipartBody.FORM, uploadModel.getVideoId());
            RequestBody duet = RequestBody.create(
                    okhttp3.MultipartBody.FORM, uploadModel.getDuet());

            fileUpload = interfaceFileUpload.UploadFile(fileToUpload, PrivacyType, UserId,
                    SoundId, AllowComments, Description, AllowDuet, UsersJson, HashtagsJson, videoId, storyJson, duet);
        }

        Log.d(Constants.TAG_, "******** before call: " + fileUpload.request().url());

        fileUpload.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {

                String bodyRes = new Gson().toJson(response.body());
                Log.d(Constants.TAG_, "Response1: " + bodyRes + " \nCall: " + call);

                try {
                    JSONObject jsonObject = new JSONObject(bodyRes);
                    int code = jsonObject.optInt("code", 0);
                    if (code == 200) {
                        mFileUploaderCallback.onFinish(bodyRes);
                    }
                } catch (Exception e) {
                    Log.d(Constants.TAG_, "Exception1: " + e);
                    mFileUploaderCallback.onError();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.d(Constants.TAG_, "1- Exception onFailure :" + t);
                mFileUploaderCallback.onError();
            }
        });
    }



    public FileUploader(File imagefile, File nullFile, Context context, String userID) {

        InterfaceFileUpload interfaceFileUpload = ApiClient.getRetrofitInstance(context)
                .create(InterfaceFileUpload.class);

        PRRequestBody mImageFile = new PRRequestBody(imagefile);
        MultipartBody.Part imageFileToUpload = MultipartBody.Part.createFormData("file",
                imagefile.getName(), mImageFile);

        RequestBody UserId = RequestBody.create(
                okhttp3.MultipartBody.FORM, userID);

        RequestBody ExtensionId = RequestBody.create(
                okhttp3.MultipartBody.FORM, "png");

        Call<Object> fileUpload = interfaceFileUpload.UploadProfileImageVideo(imageFileToUpload, UserId, ExtensionId);

        Log.d(Constants.TAG_, "URL: " + fileUpload.request().url());
        Log.d(Constants.TAG_, "file: " + imagefile.getAbsolutePath());
        Log.d(Constants.TAG_, "UserId: " + userID);
        Log.d(Constants.TAG_, "ExtensionId: " + "png");

        fileUpload.enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                String bodyRes = new Gson().toJson(response.body());
                Log.d(Constants.TAG_, "Response2: " + bodyRes);

                try {
                    JSONObject jsonObject = new JSONObject(bodyRes);
                    int code = jsonObject.optInt("code", 0);
                    if (code == 200) {
                        mFileUploaderCallback.onFinish(bodyRes);
                    } else {
                        mFileUploaderCallback.onError();
                    }
                } catch (Exception e) {
                    Log.d(Constants.TAG_, "Exception2: " + e);
                    mFileUploaderCallback.onError();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.d(Constants.TAG_, "2- Exception onFailure2: " + t);
                mFileUploaderCallback.onError();
            }
        });
    }



    public FileUploader(File file, Context context, String userID) {
        InterfaceFileUpload interfaceFileUpload = ApiClient.getRetrofitInstance(context)
                .create(InterfaceFileUpload.class);

        PRRequestBody mFile = new PRRequestBody(file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file",
                file.getName(), mFile);

        RequestBody UserId = RequestBody.create(
                okhttp3.MultipartBody.FORM, userID);

        RequestBody ExtensionId = RequestBody.create(
                okhttp3.MultipartBody.FORM, "mp4");

        Call<Object> fileUpload = interfaceFileUpload.UploadProfileImageVideo(fileToUpload, UserId, ExtensionId);

        Log.d(Constants.TAG_, "URL: " + fileUpload.request().url());
        Log.d(Constants.TAG_, "file: " + file.getAbsolutePath());
        Log.d(Constants.TAG_, "UserId: " + userID);
        Log.d(Constants.TAG_, "ExtensionId: " + "mp4");

        fileUpload.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                String bodyRes = new Gson().toJson(response.body());
                Log.d(Constants.TAG_, "Response3: " + bodyRes);

                try {
                    JSONObject jsonObject = new JSONObject(bodyRes);
                    int code = jsonObject.optInt("code", 0);
                    if (code == 200) {
                        mFileUploaderCallback.onFinish(bodyRes);
                    } else {
                        mFileUploaderCallback.onError();
                    }
                } catch (Exception e) {
                    Log.d(Constants.TAG_, "Exception3: " + e);
                    mFileUploaderCallback.onError();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.d(Constants.TAG_, "3- Exception onFailure3: " + t);
                mFileUploaderCallback.onError();
            }
        });
    }


    public void SetCallBack(FileUploaderCallback fileUploaderCallback) {
        this.mFileUploaderCallback = fileUploaderCallback;
    }


    public interface FileUploaderCallback {
        void onError();

        void onFinish(String responses);

        void onProgressUpdate(int currentpercent, int totalpercent, String msg);
    }


    public class PRRequestBody extends RequestBody {
        private static final int DEFAULT_BUFFER_SIZE = 1024;
        private final File mFile;

        public PRRequestBody(final File file) {
            mFile = file;
        }

        @Override
        public MediaType contentType() {
            // i want to upload only images
            return MediaType.parse("multipart/form-data");
        }

        @Override
        public long contentLength() {
            return mFile.length();
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            long fileLength = mFile.length();
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            FileInputStream in = new FileInputStream(mFile);
            long uploaded = 0;
//            Source source = null;

            try {
                int read;
//                source = Okio.source(mFile);
                Handler handler = new Handler(Looper.getMainLooper());
                while ((read = in.read(buffer)) != -1) {

                    // update progress on UI thread
                    handler.post(new ProgressUpdater(uploaded, fileLength));
                    uploaded += read;
                    sink.write(buffer, 0, read);
                }
            } catch (Exception e) {
                Log.d(Constants.TAG_, "Exception : " + e);
            } finally {
                in.close();
            }
        }
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded = 0;
        private long mTotal = 0;

        ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            int current_percent = (int) (100 * mUploaded / mTotal);
            int total_percent = (int) (100 * (mUploaded) / mTotal);
            mFileUploaderCallback.onProgressUpdate(current_percent, total_percent,
                    "File Size: " + Functions.readableFileSize(filesize));
        }
    }


}
