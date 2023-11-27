package com.uni.astro.activitesfragments.profile;

import com.uni.astro.databinding.ActivitySeeFullImageBinding;
import com.uni.astro.simpleclasses.AppCompatLocaleActivity;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.uni.astro.R;
import com.uni.astro.simpleclasses.Functions;
import com.uni.astro.simpleclasses.Variables;

public class SeeFullImageA extends AppCompatLocaleActivity {

    ActivitySeeFullImageBinding binding;
    String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this, getClass(),false);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_see_full_image);


        imageUrl = getIntent().getStringExtra("image_url");

        binding.ivClose.setOnClickListener(v -> {
          onBackPressed();
        });

        binding.ivProfile.setController(Functions.frescoImageLoad(imageUrl,binding.ivProfile,getIntent().getBooleanExtra("isGif",false)));

    }
}