package com.uni.astro.activitesfragments.argear.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.google.gson.Gson;
import com.uni.astro.activitesfragments.argear.api.CmsService;
import com.uni.astro.activitesfragments.argear.api.ContentsApi;
import com.uni.astro.activitesfragments.argear.api.ContentsResponse;
import com.uni.astro.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class ContentsRepository {

    @NonNull
    static ContentsRepository getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        private static final ContentsRepository INSTANCE = new ContentsRepository();
    }


    private ContentsApi contentsApi;

    private ContentsRepository() {
        contentsApi = CmsService.createContentsService(Constants.API_URL);
    }

    MutableLiveData<ContentsResponse> getContents(String apiKey) {
        MutableLiveData<ContentsResponse> contents = new MutableLiveData<>();

        contentsApi.getContents(apiKey).enqueue(new Callback<ContentsResponse>() {

            @Override
            public void onResponse(@Nullable Call<ContentsResponse> call, @NonNull Response<ContentsResponse> response) {
                Log.d(Constants.TAG_,"URL: "+call.request().url());
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    String json = gson.toJson(response.body());
                    Log.d(Constants.TAG_,json);

                    contents.setValue(response.body());

                }
            }

            @Override
            public void onFailure(@Nullable Call<ContentsResponse> call, @NonNull Throwable t) {
                contents.setValue(null);
                Log.d(Constants.TAG_,"onFailure"+call.request().toString());
                Log.d(Constants.TAG_,"onFailure"+t);
            }
        });
        return contents;
    }
}
