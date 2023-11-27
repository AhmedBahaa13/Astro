package com.uni.astro.activitesfragments.argear.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.uni.astro.activitesfragments.argear.api.ContentsResponse;
import com.uni.astro.Constants;

public class ContentsViewModel extends AndroidViewModel {

    private MutableLiveData<ContentsResponse> mutableLiveData;
    private ContentsRepository contentsRepository;

    public ContentsViewModel(Application application) {
        super(application);
        contentsRepository = ContentsRepository.getInstance();
        mutableLiveData = contentsRepository.getContents(Constants.API_KEY_ARGEAR);
    }

    public LiveData<ContentsResponse> getContents() {
        return mutableLiveData;
    }
}
