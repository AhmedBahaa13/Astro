package com.uni.astro.activitesfragments.profile;


import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.uni.astro.activitesfragments.InboxA;
import com.uni.astro.activitesfragments.NotificationA;
import com.uni.astro.activitesfragments.livestreaming.activities.ConcertSelectionA;
import com.uni.astro.activitesfragments.accounts.LoginA;
import com.uni.astro.activitesfragments.accounts.ManageAccountsF;
import com.uni.astro.activitesfragments.profile.privatevideos.PrivateVideoF;
import com.uni.astro.activitesfragments.profile.usersstory.ViewStoryA;
import com.uni.astro.activitesfragments.WebviewA;
import com.uni.astro.adapters.ViewPagerAdapter;
import com.uni.astro.apiclasses.ApiLinks;
import com.uni.astro.Constants;
import com.uni.astro.models.InboxModel;
import com.uni.astro.models.StoryModel;
import com.uni.astro.models.StoryVideoModel;
import com.uni.astro.simpleclasses.CircleDivisionView;
import com.uni.astro.simpleclasses.DebounceClickHandler;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import com.uni.astro.interfaces.FragmentCallBack;
import com.google.android.material.tabs.TabLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uni.astro.activitesfragments.profile.likedvideos.LikedVideoF;
import com.uni.astro.activitesfragments.profile.uservideos.UserVideoF;
import com.uni.astro.models.PrivacyPolicySettingModel;
import com.uni.astro.models.PushNotificationSettingModel;
import com.uni.astro.models.UserModel;
import com.uni.astro.R;
import com.uni.astro.simpleclasses.DataParsing;
import com.uni.astro.simpleclasses.Functions;
import com.uni.astro.simpleclasses.Variables;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileTabF extends Fragment {
    View view;
    Context context;
    private TextView username, username2Txt,tvBio,tvLink,tvEditProfile;
    private SimpleDraweeView imageView;
    private TextView followCountTxt, fansCountTxt, heartCountTxt;
    String totalLikes="";
    private LinearLayout tabAccount,tabLink;
    ImageView settingBtn;
    UserVideoF myVideosTab;

    protected TabLayout tabLayout;
    protected ViewPager2 pager;
    private ViewPagerAdapter adapter;

    RelativeLayout tabNotification,tabNotificationCount,tabViewsHistory,tabVisitorCount,tabInbox,tabInboxCount;
    TextView tvInboxCount,tvInboxPlus,tvVisitorCount,tvVisitorPlus,tvNotificationCount,tvNotificationPlus;
    private String picUrl,profileGif,followerCount,followingCount;
    private LinearLayout createPopupLayout,tabPrivacyLikes;
    private int myvideoCount = 0;

    PushNotificationSettingModel pushNotificationSettingModel;
    PrivacyPolicySettingModel privacyPolicySettingModel;

    CircleDivisionView circleDivisionView;
    ArrayList<StoryModel> storyDataList=new ArrayList<>();

    NewVideoBroadCast mNewVideoReceiver;
    NotificationBroadCast mNotificationReceiver;


    public ProfileTabF() {

    }

    public static ProfileTabF newInstance() {
        ProfileTabF fragment = new ProfileTabF();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile_tab, container, false);
        context = getContext();
        return init();
    }

    private void openProfileViewHistory() {
        Intent intent=new Intent(view.getContext(),ViewProfileHistoryA.class);
        resultUserDetailCallback.launch(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }

    private void openInboxF() {
        Intent intent=new Intent(view.getContext(), InboxA.class);
        resultUserDetailCallback.launch(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }


    ActivityResultLauncher<Intent> resultUserDetailCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false))
                        {
                            callApiForUserDetail();
                        }

                    }
                }
            });




    public void openStoryDetail()
    {
        Intent myIntent = new Intent(context, ViewStoryA.class);
        myIntent.putExtra("storyList",storyDataList); //Optional parameters
        myIntent.putExtra("position", 0); //Optional parameters
        startActivity(myIntent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);

    }


    String streamingId="";
    private void getLiveStreamingId() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID,"0"));
            parameters.put("started_at", Functions.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Functions.showLoader(getActivity(),false,false);
        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.liveStream,parameters, Functions.getHeaders(context),new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                Functions.cancelLoader();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        JSONObject msgObj=jsonObject.getJSONObject("msg");
                        JSONObject streamingObj=msgObj.getJSONObject("LiveStreaming");
                        streamingId=streamingObj.optString("id");
                        goLive();
                    }
                } catch (Exception e) {
                    Log.d(Constants.TAG_,"Exception : "+e);
                }
            }
        });

    }

    private void goLive() {
        Intent intent = new Intent(getActivity(), ConcertSelectionA.class);
        intent.putExtra("userId", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
        intent.putExtra("userName", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
        intent.putExtra("userPicture", Functions.getSharedPreference(context).getString(Variables.U_PIC, ""));
        intent.putExtra("userRole", io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER);
        intent.putExtra("streamingId",streamingId);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }

    private void openInviteFriends() {
        Intent intent=new Intent(view.getContext(), InviteFriendsA.class);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }


    private void openNotifications() {
        Intent intent=new Intent(view.getContext(), NotificationA.class);
        resultUserDetailCallback.launch(intent);
        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
    }


    private void openProfileDetail() {
        boolean isGif=false;
        String mediaURL;
        if (profileGif!=null && !(profileGif.equals(Constants.BASE_URL)))
        {
            isGif=true;
            mediaURL=profileGif;
        }
        else
        {
            isGif=false;
            mediaURL=picUrl;
        }
        final ShareAndViewProfileF fragment = new ShareAndViewProfileF(isGif,mediaURL,
                Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID,"")
                , new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle.getString("action").equals("profileShareMessage")) {
                    if (Functions.checkLoginUser(getActivity()))
                    {
                        // firebase sharing
                    }
                }

            }
        });
        fragment.show(getChildFragmentManager(), "");

    }


    private void showMyLikesCounts() {
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.show_likes_alert_popup_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        final TextView tvMessage,tvDone;
        tvDone=dialog.findViewById(R.id.tvDone);
        tvMessage=dialog.findViewById(R.id.tvMessage);

        tvMessage.setText(username.getText()+" "+view.getContext().getString(R.string.received_a_total_of)+" "+totalLikes+" "+view.getContext().getString(R.string.likes_across_all_video));
        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void openWebUrl(String title, String url) {
        Intent intent=new Intent(view.getContext(), WebviewA.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }



    private void openManageMultipleAccounts() {
        ManageAccountsF f = new ManageAccountsF(new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle.getBoolean("isShow",false))
                {
                    Functions.hideSoftKeyboard(getActivity());
                    Intent intent = new Intent(getActivity(), LoginA.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                }
            }
        });
        f.show(getChildFragmentManager(), "");
    }




    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN, false)) {
                        updateProfile();
                        callApiForUserDetail();
                    }
                }
            }, 200);
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        showDraftCount();

    }

    private View init() {
        username = view.findViewById(R.id.username);
        username2Txt = view.findViewById(R.id.username2_txt);
        tvLink=view.findViewById(R.id.tvLink);
        tvBio=view.findViewById(R.id.tvBio);
        imageView = view.findViewById(R.id.user_image);
        imageView.setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (circleDivisionView.getVisibility()==View.VISIBLE)
                {
                    openStoryDetail();
                }
                else
                {
                    openProfileDetail();
                }
            }
        }));
        followCountTxt = view.findViewById(R.id.follow_count_txt);
        fansCountTxt = view.findViewById(R.id.fan_count_txt);
        heartCountTxt = view.findViewById(R.id.heart_count_txt);

        circleDivisionView=view.findViewById(R.id.circleStatusBar);
        circleDivisionView.setStrokeLineColor(getContext().getColor(R.color.colorAccent));

        showDraftCount();

        tvEditProfile=view.findViewById(R.id.edit_profile_btn);
        tvEditProfile.setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEditProfile();
            }
        }));

        tabAccount=view.findViewById(R.id.tabAccount);
        tabAccount.setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openManageMultipleAccounts();
            }
        }));

        tabLink=view.findViewById(R.id.tabLink);
        tabLink.setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWebUrl(view.getContext().getString(R.string.web_browser),""+tvLink.getText().toString());
            }
        }));

        settingBtn = view.findViewById(R.id.message_btn);
        settingBtn.setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(view.getContext(),SettingAndPrivacyA.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
            }
        }));

        tabInbox=view.findViewById(R.id.tabInbox);
        tabInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInboxF();
            }
        });
        tabNotificationCount=view.findViewById(R.id.tabNotificationCount);
        tvNotificationCount=view.findViewById(R.id.tvNotificationCount);
        tvNotificationPlus=view.findViewById(R.id.tvNotificationPlus);

        tabNotification =view.findViewById(R.id.tabNotification);
        tabNotification.setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNotifications();
            }
        }));
        tabViewsHistory =view.findViewById(R.id.tabViewsHistory);
        tabViewsHistory.setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openProfileViewHistory();
            }
        }));

        tabInboxCount=view.findViewById(R.id.tabInboxCount);
        tvInboxCount=view.findViewById(R.id.tvInboxCount);
        tvInboxPlus=view.findViewById(R.id.tvInboxPlus);

        tabVisitorCount=view.findViewById(R.id.tabVisitorCount);
        tvVisitorCount=view.findViewById(R.id.tvVisitorCount);
        tvVisitorPlus=view.findViewById(R.id.tvVisitorPlus);

        tabPrivacyLikes=view.findViewById(R.id.tabPrivacyLikes);
        tabPrivacyLikes.setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMyLikesCounts();
            }
        }));

        createPopupLayout = view.findViewById(R.id.create_popup_layout);

        view.findViewById(R.id.following_layout).setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFollowing();
            }
        }));
        view.findViewById(R.id.fans_layout).setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFollowers();
            }
        }));
        view.findViewById(R.id.add_friends_btn).setOnClickListener(new DebounceClickHandler(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInviteFriends();
            }
        }));

        SetTabs();

        mNewVideoReceiver = new NewVideoBroadCast();
        getActivity().registerReceiver(mNewVideoReceiver, new IntentFilter("newVideo"));
        mNotificationReceiver = new NotificationBroadCast();
        getActivity().registerReceiver(mNotificationReceiver, new IntentFilter("NotificationHit"));

        return view;
    }


    public void SetTabs() {
        adapter = new ViewPagerAdapter(this);
        pager = (ViewPager2) view.findViewById(R.id.pager);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);

        pager.setOffscreenPageLimit(2);
        registerFragmentWithPager();
        pager.setAdapter(adapter);
        addTabs();
        setupTabIcons();
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });
    }

    private void addTabs() {
        TabLayoutMediator tabLayoutMediator=new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position==0)
                {
                    tab.setText(context.getString(R.string.my_videos));
                }
                else
                if (position==1)
                {
                    tab.setText(context.getString(R.string.liked_videos));
                }
                else
                if (position==2)
                {
                    tab.setText(context.getString(R.string.repost));
                }
                else
                if (position==3)
                {
                    tab.setText(context.getString(R.string.favourite));
                }
                else
                if (position==4)
                {
                    tab.setText(context.getString(R.string.private_));
                }
            }
        });
        tabLayoutMediator.attach();
    }

    private void registerFragmentWithPager() {
        myVideosTab=UserVideoF.newInstance(true, Functions.getSharedPreference(context).getString(Variables.U_ID, ""),Functions.getSharedPreference(context).getString(Variables.U_NAME, ""),"");
        adapter.addFrag(myVideosTab);
        adapter.addFrag(LikedVideoF.newInstance(true, Functions.getSharedPreference(context).getString(Variables.U_ID, ""),Functions.getSharedPreference(context).getString(Variables.U_NAME, ""),true,""));
        adapter.addFrag(RepostVideoF.newInstance(true, Functions.getSharedPreference(context).getString(Variables.U_ID, ""),Functions.getSharedPreference(context).getString(Variables.U_NAME, ""),""));
        adapter.addFrag(FavouriteTabF.newInstance());
        adapter.addFrag(PrivateVideoF.newInstance());
    }



    public void showDraftCount() {
        try {

            String path = Functions.getAppFolder(getActivity())+Variables.DRAFT_APP_FOLDER;
            File directory = new File(path);
            File[] files = directory.listFiles();
            if (files.length <= 0) {
                //draf gone
            } else {
                //draf visible
            }
        } catch (Exception e) {

        }
    }


    // place the profile data
    private void updateProfile() {
        username2Txt.setText(Functions.showUsername(Functions.getSharedPreference(context).getString(Variables.U_NAME, "")));
        String firstName = Functions.getSharedPreference(context).getString(Variables.F_NAME, "");
        String lastName = Functions.getSharedPreference(context).getString(Variables.L_NAME, "");
        if (firstName.equalsIgnoreCase("") && lastName.equalsIgnoreCase("")) {
            username.setText(Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
        } else {
            username.setText(firstName + " " + lastName);
        }
        if (TextUtils.isEmpty(Functions.getSharedPreference(context).getString(Variables.U_BIO, "")))
        {
            tvBio.setVisibility(View.GONE);
        }
        else
        {
            tvBio.setVisibility(View.VISIBLE);
            tvBio.setText(Functions.getSharedPreference(context).getString(Variables.U_BIO, ""));
        }
        if (TextUtils.isEmpty(Functions.getSharedPreference(context).getString(Variables.U_LINK, "")))
        {
            tabLink.setVisibility(View.GONE);
        }
        else
        {
            tabLink.setVisibility(View.VISIBLE);
            tvLink.setText(Functions.getSharedPreference(context).getString(Variables.U_LINK, ""));
        }
        picUrl = Functions.getSharedPreference(context).getString(Variables.U_PIC, "");
        profileGif=Functions.getSharedPreference(context).getString(Variables.U_GIF, "");
        if (profileGif.equals(Constants.BASE_URL))
        {
            imageView.setController(Functions.frescoImageLoad(picUrl,imageView,false));
        }
        else
        {
            imageView.setController(Functions.frescoImageLoad(profileGif,R.drawable.ic_user_icon,imageView,true));
        }

    }


    // change the icons of the tab
    private void setupTabIcons() {

        View view1 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView1 = view1.findViewById(R.id.image);
        imageView1.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_my_video_select));
        imageView1.setColorFilter(ContextCompat.getColor(context,R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);

        tabLayout.getTabAt(0).setCustomView(view1);

        View view2 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView2 = view2.findViewById(R.id.image);
        imageView2.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_liked_video_gray));
        imageView2.setColorFilter(ContextCompat.getColor(context,R.color.darkgray), android.graphics.PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).setCustomView(view2);


        View view3 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView3 = view3.findViewById(R.id.image);
        imageView3.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_repost_gray));
        imageView3.setColorFilter(ContextCompat.getColor(context,R.color.darkgray), android.graphics.PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(2).setCustomView(view3);


        View view4 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView4 = view4.findViewById(R.id.image);
        imageView4.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_fav_gray));
        imageView4.setColorFilter(ContextCompat.getColor(context,R.color.darkgray), android.graphics.PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(3).setCustomView(view4);

        View view5 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView5 = view5.findViewById(R.id.image);
        imageView5.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_lock_gray));
        imageView5.setColorFilter(ContextCompat.getColor(context,R.color.darkgray), android.graphics.PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(4).setCustomView(view5);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition(),true);
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:
                        if (myvideoCount > 0) {
                            createPopupLayout.setVisibility(View.GONE);
                        } else {
                            createPopupLayout.setVisibility(View.VISIBLE);
                            Animation aniRotate = AnimationUtils.loadAnimation(context, R.anim.up_and_down_animation);
                            createPopupLayout.startAnimation(aniRotate);
                        }

                        image.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_my_video_select));
                        image.setColorFilter(ContextCompat.getColor(context,R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);

                        break;

                    case 1:
                        createPopupLayout.clearAnimation();
                        createPopupLayout.setVisibility(View.GONE);
                        image.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_liked_video_color));
                        image.setColorFilter(ContextCompat.getColor(context,R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                        break;

                    case 2:
                        createPopupLayout.clearAnimation();
                        createPopupLayout.setVisibility(View.GONE);
                        image.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_repost_gray));
                        image.setColorFilter(ContextCompat.getColor(context,R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                        break;
                    case 3:
                        createPopupLayout.clearAnimation();
                        createPopupLayout.setVisibility(View.GONE);
                        image.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_fav_black));
                        image.setColorFilter(ContextCompat.getColor(context,R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                        break;
                    case 4:
                        createPopupLayout.clearAnimation();
                        createPopupLayout.setVisibility(View.GONE);
                        image.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_lock_black));
                        image.setColorFilter(ContextCompat.getColor(context,R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:
                        image.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_my_video_gray));
                        image.setColorFilter(ContextCompat.getColor(context,R.color.darkgray), android.graphics.PorterDuff.Mode.SRC_IN);
                        break;
                    case 1:
                        image.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_liked_video_gray));
                        image.setColorFilter(ContextCompat.getColor(context,R.color.darkgray), android.graphics.PorterDuff.Mode.SRC_IN);
                        break;
                    case 2:
                        image.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_repost_gray));
                        image.setColorFilter(ContextCompat.getColor(context,R.color.darkgray), android.graphics.PorterDuff.Mode.SRC_IN);
                        break;
                    case 3:
                        image.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_fav_gray));
                        image.setColorFilter(ContextCompat.getColor(context,R.color.darkgray), android.graphics.PorterDuff.Mode.SRC_IN);
                        break;
                    case 4:
                        image.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_lock_gray));
                        image.setColorFilter(ContextCompat.getColor(context,R.color.darkgray), android.graphics.PorterDuff.Mode.SRC_IN);
                        break;
                }

                tab.setCustomView(v);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

    }


    //this will get the all videos data of user and then parse the data
    private void callApiForUserDetail() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showUserDetail, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                parseData(resp);
            }
        });


    }

    public void parseData(String responce) {


        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {


                JSONObject msg = jsonObject.optJSONObject("msg");
                JSONObject pushNotificationSetting = msg.optJSONObject("PushNotification");
                JSONObject privacyPolicySetting = msg.optJSONObject("PrivacySetting");
                JSONObject userObj = msg.optJSONObject("User");
                UserModel userDetailModel= DataParsing.getUserDataModel(userObj);


                try {
                    JSONArray storyArray=userObj.getJSONArray("story");
                    storyDataList.clear();
                    StoryModel storyItem=new StoryModel();
                    storyItem.setUserModel(userDetailModel);
                    ArrayList<StoryVideoModel> storyVideoList=new ArrayList<>();
                    for (int i=0; i<storyArray.length();i++)
                    {
                        JSONObject itemObj=storyArray.getJSONObject(i);
                        StoryVideoModel storyVideoItem=DataParsing.getVideoDataModel(itemObj.optJSONObject("Video"));
                        storyVideoList.add(storyVideoItem);
                    }
                    storyItem.setVideoList(storyVideoList);
                    if (storyVideoList.size()>0)
                    {
                        storyDataList.add(storyItem);

                        circleDivisionView.setVisibility(View.VISIBLE);
                        circleDivisionView.setCounts(storyVideoList.size());
                    }
                    else
                    {
                        circleDivisionView.setVisibility(View.GONE);
                    }
                }
                catch (Exception e)
                {
                    Log.d(Constants.TAG_,"Exception: "+e);
                }


                pushNotificationSettingModel = new PushNotificationSettingModel();
                if (pushNotificationSetting != null) {
                    pushNotificationSettingModel.setComments("" + pushNotificationSetting.optString("comments"));
                    pushNotificationSettingModel.setLikes("" + pushNotificationSetting.optString("likes"));
                    pushNotificationSettingModel.setNewfollowers("" + pushNotificationSetting.optString("new_followers"));
                    pushNotificationSettingModel.setMentions("" + pushNotificationSetting.optString("mentions"));
                    pushNotificationSettingModel.setDirectmessage("" + pushNotificationSetting.optString("direct_messages"));
                    pushNotificationSettingModel.setVideoupdates("" + pushNotificationSetting.optString("video_updates"));
                }

                privacyPolicySettingModel = new PrivacyPolicySettingModel();
                if (privacyPolicySetting != null) {
                    privacyPolicySettingModel.setVideos_download("" + privacyPolicySetting.optString("videos_download"));
                    privacyPolicySettingModel.setDirect_message("" + privacyPolicySetting.optString("direct_message"));
                    privacyPolicySettingModel.setDuet("" + privacyPolicySetting.optString("duet"));
                    privacyPolicySettingModel.setLiked_videos("" + privacyPolicySetting.optString("liked_videos"));
                    privacyPolicySettingModel.setVideo_comment("" + privacyPolicySetting.optString("video_comment"));
                }

                Paper.book(Variables.PrivacySetting).write(Variables.PushSettingModel, pushNotificationSettingModel);
                Paper.book(Variables.PrivacySetting).write(Variables.PrivacySettingModel, privacyPolicySettingModel);

                username2Txt.setText(Functions.showUsername(userDetailModel.getUsername()));


                if (TextUtils.isEmpty(userDetailModel.getBio()))
                {
                    tvBio.setVisibility(View.GONE);
                }
                else
                {
                    tvBio.setVisibility(View.VISIBLE);
                    tvBio.setText(userDetailModel.getBio());
                }

                if (TextUtils.isEmpty(userDetailModel.getWebsite()))
                {
                    tabLink.setVisibility(View.GONE);
                }
                else
                {
                    tabLink.setVisibility(View.VISIBLE);
                    tvLink.setText(userDetailModel.getWebsite());
                }

                String firstName = userDetailModel.getFirstName();
                String lastName = userDetailModel.getLastName();

                if (firstName.equalsIgnoreCase("") && lastName.equalsIgnoreCase("")) {
                    username.setText(userDetailModel.getUsername());
                } else {
                    username.setText(firstName + " " + lastName);
                }

                picUrl = userDetailModel.getProfilePic();
                profileGif=userDetailModel.getProfileGif();
                if (profileGif.equals(Constants.BASE_URL))
                {
                    imageView.setController(Functions.frescoImageLoad(picUrl,imageView,false));
                }
                else
                {
                    imageView.setController(Functions.frescoImageLoad(profileGif,R.drawable.ic_user_icon,imageView,true));
                }

                SharedPreferences.Editor editor = Functions.getSharedPreference(view.getContext()).edit();
                editor.putString(Variables.U_PIC,picUrl);
                editor.putString(Variables.U_GIF, userDetailModel.getProfileGif());
                editor.putString(Variables.U_PROFILE_VIEW, userDetailModel.getProfileView());
                editor.putString(Variables.U_WALLET, ""+userDetailModel.getWallet());
                editor.putString(Variables.U_total_coins_all_time,""+userDetailModel.getTotal_all_time_coins());
                editor.putString(Variables.REFERAL_CODE, ""+userDetailModel.getReferalCode());
                editor.putString(Variables.IS_VERIFIED, ""+userDetailModel.getVerified());
                editor.commit();

                followingCount=userDetailModel.getFollowingCount();
                followerCount=userDetailModel.getFollowersCount();
                totalLikes=userDetailModel.getLikesCount();
                followCountTxt.setText(Functions.getSuffix(followingCount));
                fansCountTxt.setText(Functions.getSuffix(followerCount));
                heartCountTxt.setText(Functions.getSuffix(totalLikes));

                myvideoCount = Functions.parseInterger(userDetailModel.getVideoCount());

                if (myvideoCount != 0) {
                    createPopupLayout.setVisibility(View.GONE);
                    createPopupLayout.clearAnimation();
                } else {

                    createPopupLayout.setVisibility(View.VISIBLE);
                    Animation aniRotate = AnimationUtils.loadAnimation(context, R.anim.up_and_down_animation);
                    createPopupLayout.startAnimation(aniRotate);

                }

                String verified = userDetailModel.getVerified();
                if (verified != null && verified.equalsIgnoreCase("1")) {
                    view.findViewById(R.id.varified_btn).setVisibility(View.VISIBLE);
                }



                if (myVideosTab!=null)
                {
                    myVideosTab.updatePlaylistCreate();
                    if (msg.has("Playlist"))
                    {
                        JSONArray playlistArray = msg.getJSONArray("Playlist");
                        myVideosTab.updateUserPlaylist(playlistArray, userDetailModel.getVerified(), new FragmentCallBack() {
                            @Override
                            public void onResponce(Bundle bundle) {
                                if (bundle.getBoolean("isShow",false))
                                {
                                    callApiForUserDetail();
                                }
                            }
                        });
                    }
                }

                updateProfileNotificationCount(userDetailModel);
                updateProfileVisitorCount(userDetailModel);


            } else {
                Functions.showToast(getActivity(), jsonObject.optString("msg"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            getInboxList();
        }

    }

    private void updateProfileNotificationCount(UserModel userDetailModel) {
        if (userDetailModel.getNotificationCount()>0)
        {
            tabNotificationCount.setVisibility(View.VISIBLE);
            if (userDetailModel.getNotificationCount()>99)
            {
                tvNotificationPlus.setVisibility(View.VISIBLE);
                tvNotificationCount.setText("99");
            }
            else
            {
                tvNotificationPlus.setVisibility(View.GONE);
                tvNotificationCount.setText(""+userDetailModel.getNotificationCount());
            }
        }
        else
        {
            tabNotificationCount.setVisibility(View.GONE);
        }
    }
    private void updateProfileVisitorCount(UserModel userDetailModel) {
        if (userDetailModel.getVisitorCount()>0)
        {
            tabVisitorCount.setVisibility(View.VISIBLE);
            if (userDetailModel.getVisitorCount()>99)
            {
                tvVisitorPlus.setVisibility(View.VISIBLE);
                tvVisitorCount.setText("99");
            }
            else
            {
                tvVisitorPlus.setVisibility(View.GONE);
                tvVisitorCount.setText(""+userDetailModel.getVisitorCount());
            }
        }
        else
        {
            tabVisitorCount.setVisibility(View.GONE);
        }
    }

    private void openEditProfile() {
        Intent intent=new Intent(view.getContext(),EditProfileA.class);
        resultCallback.launch(intent);
        getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false))
                        {
                            updateProfile();
                        }

                    }
                }
            });



    // open the following fragment
    private void openFollowing() {
        Intent intent=new Intent(view.getContext(), FollowsMainTabA.class);
        intent.putExtra("id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
        intent.putExtra("from_where", "following");
        intent.putExtra("userName", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
        intent.putExtra("followingCount",""+followingCount);
        intent.putExtra("followerCount",""+followerCount);
        resultUserDetailCallback.launch(intent);
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);


    }


    // open the followers fragment
    private void openFollowers() {
        Intent intent=new Intent(view.getContext(), FollowsMainTabA.class);
        intent.putExtra("id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));
        intent.putExtra("from_where", "fan");
        intent.putExtra("userName", Functions.getSharedPreference(context).getString(Variables.U_NAME, ""));
        intent.putExtra("followingCount",""+followingCount);
        intent.putExtra("followerCount",""+followerCount);
        resultUserDetailCallback.launch(intent);
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private ActivityResultLauncher<String[]> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onActivityResult(Map<String, Boolean> result) {

                    boolean allPermissionClear=true;
                    List<String> blockPermissionCheck=new ArrayList<>();
                    for (String key : result.keySet())
                    {
                        if (!(result.get(key)))
                        {
                            allPermissionClear=false;
                            blockPermissionCheck.add(Functions.getPermissionStatus(getActivity(),key));
                        }
                    }
                    if (blockPermissionCheck.contains("blocked"))
                    {
                        Functions.showPermissionSetting(view.getContext(),getString(R.string.we_need_camera_and_recording_permission_for_live_streaming));
                    }
                    else
                    if (allPermissionClear)
                    {
                        getLiveStreamingId();
                    }

                }
            });



    ArrayList<InboxModel> notReadInboxlist=new ArrayList<>();
    public void getInboxList() {
        Query inboxQuery= FirebaseDatabase.getInstance().getReference().child("Inbox")
                .child(Functions.getSharedPreference(view.getContext()).getString(Variables.U_ID, "0"))
                .orderByChild("date");
        inboxQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    notReadInboxlist.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        InboxModel model = ds.getValue(InboxModel.class);
                        model.setId(ds.getKey());
                        if (model.getStatus() !=null && model.getStatus().equals("0"))
                        {
                            notReadInboxlist.add(model);
                        }
                    }
                }

                updateInboxCounts();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateInboxCounts();
            }
        });


    }

    private void updateInboxCounts() {
        if (notReadInboxlist.size()>0)
        {
            tabInboxCount.setVisibility(View.VISIBLE);
            if (notReadInboxlist.size()>99)
            {
                tvInboxPlus.setVisibility(View.VISIBLE);
                tvInboxCount.setText("99");
            }
            else
            {
                tvInboxPlus.setVisibility(View.GONE);
                tvInboxCount.setText(""+notReadInboxlist.size());
            }
        }
        else
        {
            tabInboxCount.setVisibility(View.GONE);
        }
    }

    private class NewVideoBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            callApiForUserDetail();
        }
    }

    private class NotificationBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            callApiForUserDetail();
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (mPermissionResult!=null)
        {
            mPermissionResult.unregister();
        }
        if (mNewVideoReceiver != null) {
            getActivity().unregisterReceiver(mNewVideoReceiver);
            mNewVideoReceiver = null;
        }
        if (mNotificationReceiver != null) {
            getActivity().unregisterReceiver(mNotificationReceiver);
            mNotificationReceiver = null;
        }
    }


}
