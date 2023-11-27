package com.uni.astro.simpleclasses;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.android.volley.misc.AsyncTask;

public class VideoThumbnailExtractor {

    public interface ThumbnailListener {
        void onThumbnail(Bitmap thumbnail);
    }

    public static void getThumbnailFromVideoFilePath(String videoFilePath, ThumbnailListener listener) {
        new ThumbnailExtractorTask(listener).execute(videoFilePath);
    }

    private static class ThumbnailExtractorTask extends AsyncTask<String, Void, Bitmap> {
        private final ThumbnailListener listener;

        ThumbnailExtractorTask(ThumbnailListener listener) {
            this.listener = listener;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                String videoFilePath = params[0];
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(videoFilePath);

                // Specify the time in microseconds where you want to get the frame (e.g., 1000000 for 1 second)
                long timeInMicroseconds = 1000000;
                Bitmap bitmap = retriever.getFrameAtTime(timeInMicroseconds, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                retriever.release();
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (listener != null) {
                listener.onThumbnail(result);
            }
        }
    }
}
