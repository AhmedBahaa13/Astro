package com.uni.astro.simpleclasses;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import com.uni.astro.interfaces.FragmentCallBack;
import com.uni.astro.R;
import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    Activity activity;
    ActivityResultLauncher<String[]> mPermissionResult;

    public PermissionUtils(Activity activity,ActivityResultLauncher<String[]> mPermissionResult) {
        this.activity = activity;
        this.mPermissionResult=mPermissionResult;
    }

    public void takeStorageCameraRecordingPermission()
    {
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.S_V2)
        {
            String[] permissions = {Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
            mPermissionResult.launch(permissions);
        }
        else
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
            mPermissionResult.launch(permissions);

        }
        else
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
            mPermissionResult.launch(permissions);
        }
    }

    public void takeStorageCameraPermission()
    {
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.S_V2)
        {
            String[] permissions = {Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.CAMERA};
            mPermissionResult.launch(permissions);
        }
        else
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
            mPermissionResult.launch(permissions);
        }
        else
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
            mPermissionResult.launch(permissions);
        }
    }

    public void takeStorageRecordingPermission()
    {
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.S_V2)
        {
            String[] permissions = {Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.RECORD_AUDIO};
            mPermissionResult.launch(permissions);
        }
        else
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO};
            mPermissionResult.launch(permissions);
        }
        else
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO};
            mPermissionResult.launch(permissions);
        }
    }

    public void takeStoragePermission()
    {
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.S_V2)
        {
            String[] permissions = {Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO};
            mPermissionResult.launch(permissions);
        }
        else
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            mPermissionResult.launch(permissions);
        }
        else
        {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
            mPermissionResult.launch(permissions);
        }
    }

    public void takeCameraPermission()
    {
        String[] permissions = {Manifest.permission.CAMERA};
        mPermissionResult.launch(permissions);
    }

    public void takeCameraRecordingPermission()
    {
        String[] permissions = {Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        mPermissionResult.launch(permissions);
    }

    public void takeContactPermission()
    {
        String[] permissions = {Manifest.permission.READ_CONTACTS};
        mPermissionResult.launch(permissions);
    }


    public void showCameraPermissionDailog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions = {Manifest.permission.CAMERA};
        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponce(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeCameraPermission();
                            }
                        }
                    });
            return;
        }
        takeCameraPermission();

    }

    public boolean isCameraPermissionGranted()
    {
        int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        return (cameraPermission== PackageManager.PERMISSION_GRANTED);
    }

    public void showStorageCameraPermissionDailog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions;
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.S_V2)
        {
            permissions =new String[]{Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.CAMERA};
        }
        else
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
            permissions =new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
        }
        else
        {
            permissions =new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
        }

        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponce(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeStorageCameraPermission();
                            }
                        }
                    });
            return;
        }
        takeStorageCameraPermission();

    }

    public boolean isStorageCameraPermissionGranted()
    {
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.S_V2)
        {
            int mediaImagesPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES);
            int mediaVideoPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO);
            int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            return (mediaImagesPermission== PackageManager.PERMISSION_GRANTED && mediaVideoPermission== PackageManager.PERMISSION_GRANTED && cameraPermission== PackageManager.PERMISSION_GRANTED);
        }
        else
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && cameraPermission== PackageManager.PERMISSION_GRANTED);
        }
        else
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && writeExternalStoragePermission== PackageManager.PERMISSION_GRANTED && cameraPermission== PackageManager.PERMISSION_GRANTED);
        }
    }

    public void showStoragePermissionDailog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions ;
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.S_V2)
        {
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO};
        }
        else
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        else
        {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponce(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeStoragePermission();
                            }
                        }
                    });
            return;
        }
        takeStoragePermission();

    }

    public boolean isStoragePermissionGranted()
    {
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.S_V2)
        {
            int mediaImagesPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES);
            int mediaVideoPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO);
            return (mediaImagesPermission== PackageManager.PERMISSION_GRANTED && mediaVideoPermission== PackageManager.PERMISSION_GRANTED);
        }
        else
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED);
        }
        else
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && writeExternalStoragePermission== PackageManager.PERMISSION_GRANTED );
        }
    }

    public void showCameraRecordingPermissionDailog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions = {Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponce(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeCameraRecordingPermission();
                            }
                        }
                    });
            return;
        }
        takeCameraRecordingPermission();

    }

    public boolean isCameraRecordingPermissionGranted()
    {
        int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        int recordAudioPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        return (cameraPermission== PackageManager.PERMISSION_GRANTED && recordAudioPermission== PackageManager.PERMISSION_GRANTED );
    }

    public void showContactPermissionDailog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions = {Manifest.permission.READ_CONTACTS};
        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponce(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeContactPermission();
                            }
                        }
                    });
            return;
        }
        takeContactPermission();

    }

    public boolean isContactPermissionGranted()
    {
        int contactPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS);
        return (contactPermission== PackageManager.PERMISSION_GRANTED);
    }

    public void showStorageRecordingPermissionDailog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions;
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.S_V2)
        {
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.RECORD_AUDIO};
        }
        else
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
            permissions =new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO};
        }
        else
        {
            permissions =new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO};
        }

        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponce(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeStorageRecordingPermission();
                            }
                        }
                    });
            return;
        }
        takeStorageRecordingPermission();

    }

    public boolean isStorageRecordingPermissionGranted()
    {
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.S_V2)
        {
            int mediaImagesPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES);
            int mediaVideoPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO);
            int recordingPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
            return (mediaImagesPermission== PackageManager.PERMISSION_GRANTED && mediaVideoPermission== PackageManager.PERMISSION_GRANTED && recordingPermission== PackageManager.PERMISSION_GRANTED);
        }
        else
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int recordingPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && recordingPermission== PackageManager.PERMISSION_GRANTED);
        }
        else
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int recordingPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && writeExternalStoragePermission== PackageManager.PERMISSION_GRANTED && recordingPermission== PackageManager.PERMISSION_GRANTED);
        }
    }

    public void showStorageCameraRecordingPermissionDailog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions ;
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.S_V2)
        {
            permissions =new String[]{Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        }
        else
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
            permissions =new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        }
        else
        {
             permissions =new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        }

        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponce(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeStorageCameraRecordingPermission();
                            }
                        }
                    });
            return;
        }
        takeStorageCameraRecordingPermission();

    }

    public boolean isStorageCameraRecordingPermissionGranted()
    {
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.S_V2)
        {
            int mediaImagesPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES);
            int mediaVideoPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO);
            int recordingPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
            int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            return (mediaImagesPermission== PackageManager.PERMISSION_GRANTED && mediaVideoPermission== PackageManager.PERMISSION_GRANTED && cameraPermission== PackageManager.PERMISSION_GRANTED && recordingPermission== PackageManager.PERMISSION_GRANTED);
        }
        else
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.P)
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int recordingPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
            int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && cameraPermission== PackageManager.PERMISSION_GRANTED && recordingPermission== PackageManager.PERMISSION_GRANTED);
        }
        else
        {
            int readExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeExternalStoragePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int recordingPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
            int cameraPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
            return (readExternalStoragePermission== PackageManager.PERMISSION_GRANTED && writeExternalStoragePermission== PackageManager.PERMISSION_GRANTED && cameraPermission== PackageManager.PERMISSION_GRANTED && recordingPermission== PackageManager.PERMISSION_GRANTED);
        }
    }

    public boolean isPostNotificationPermissionGranted()
    {
        int postNotificationPermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS);
        return (postNotificationPermission== PackageManager.PERMISSION_GRANTED);
    }
    public void takePostNotificationPermission()
    {
        String[] permissions = {Manifest.permission.POST_NOTIFICATIONS};
        mPermissionResult.launch(permissions);
    }

    public boolean isLocationPermissionGranted()
    {
        int coursePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
        int finePermission= ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        return (coursePermission== PackageManager.PERMISSION_GRANTED && finePermission== PackageManager.PERMISSION_GRANTED);
    }

    public void showLocationPermissionDailog(String message)
    {
        List<String> permissionStatusList=new ArrayList<>();
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
        for (String keyStr:permissions)
        {
            permissionStatusList.add(Functions.getPermissionStatus(activity,keyStr));
        }

        if (permissionStatusList.contains("denied"))
        {
            Functions.showDoubleButtonAlert(activity, activity.getString(R.string.permission_alert),message,
                    activity.getString(R.string.cancel_), activity.getString(R.string.permission), false, new FragmentCallBack() {
                        @Override
                        public void onResponce(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                takeLocationPermission();
                            }
                        }
                    });
            return;
        }
        takeLocationPermission();

    }

    public void takeLocationPermission()
    {
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
        mPermissionResult.launch(permissions);
    }


}
