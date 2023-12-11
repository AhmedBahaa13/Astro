package com.uni.astro.activitesfragments.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import com.uni.astro.Constants;
import com.uni.astro.R;
import com.uni.astro.activitesfragments.WatchVideosA;
import com.uni.astro.adapters.MyVideosAdapter;
import com.uni.astro.apiclasses.ApiLinks;
import com.uni.astro.models.HomeModel;
import com.uni.astro.simpleclasses.Functions;
import com.uni.astro.simpleclasses.Variables;
import com.volley.plus.VPackages.VolleyRequest;
import com.volley.plus.interfaces.Callback;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class RepostVideoF extends Fragment {

    RecyclerView recyclerView;
    ArrayList<HomeModel> dataList;
    MyVideosAdapter adapter;
    View view;
    Context context;
    TextView tvTitleNoData,tvMessageNoData;
    RelativeLayout noDataLayout;


    int pageCount = 0;
    boolean ispostFinsh;
    ProgressBar loadMoreProgress;
    GridLayoutManager linearLayoutManager;

    String isUserAlreadyBlock="0";
    String userId="",userName="",blockedByUserName="";
    boolean is_my_profile = true;


    public RepostVideoF() {

    }

    public RepostVideoF(boolean is_my_profile, String userId, String userName,String isUserAlreadyBlock) {
        this.is_my_profile = is_my_profile;
        this.userId = userId;
        this.userName=userName;
        this.isUserAlreadyBlock=isUserAlreadyBlock;
    }

    public static RepostVideoF newInstance(boolean is_my_profile, String userId, String userName,String isUserAlreadyBlock) {
        RepostVideoF fragment = new RepostVideoF(is_my_profile,userId,userName,isUserAlreadyBlock);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_repost_video, container, false);

        context = getContext();

        loadMoreProgress = view.findViewById(R.id.load_more_progress);
        recyclerView = view.findViewById(R.id.recylerview);
        linearLayoutManager = new GridLayoutManager(context, 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);


        dataList = new ArrayList<>();
        adapter = new MyVideosAdapter(context, dataList, "myProfile", (view, pos, object) -> {
            HomeModel item = (HomeModel) object;
            openWatchVideo(pos);

        });
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems,scrollInItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollInItem=linearLayoutManager.findFirstVisibleItemPosition();
                scrollOutitems = linearLayoutManager.findLastVisibleItemPosition();

                if (scrollInItem == 0)
                {
                    recyclerView.setNestedScrollingEnabled(true);
                }
                else
                {
                    recyclerView.setNestedScrollingEnabled(false);
                }
                if (userScrolled && (scrollOutitems == dataList.size() - 1)) {
                    userScrolled = false;

                    if (loadMoreProgress.getVisibility() != View.VISIBLE && !ispostFinsh) {
                        loadMoreProgress.setVisibility(View.VISIBLE);
                        pageCount = pageCount + 1;
                        callApiMyvideos(false);
                    }
                }


            }
        });

        noDataLayout = view.findViewById(R.id.no_data_layout);
        tvTitleNoData=view.findViewById(R.id.tvTitleNoData);
        tvMessageNoData=view.findViewById(R.id.tvMessageNoData);

        return view;
    }



    private void setNoData() {
        if (is_my_profile)
        {
            tvTitleNoData.setVisibility(View.GONE);
            tvMessageNoData.setVisibility(View.GONE);
        }
        else
        {
            tvTitleNoData.setVisibility(View.GONE);
            tvMessageNoData.setVisibility(View.VISIBLE);
            tvMessageNoData.setText(view.getContext().getString(R.string.this_user_has_not_repost_any_video));
        }
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                pageCount = 0;
                callApiMyvideos(true);
            }, 200);
        }
    }


    Boolean isApiRun = false;

    //this will get the all videos data of user and then parse the data
    private void callApiMyvideos(boolean isScrollToTop) {
        if (isUserAlreadyBlock.equalsIgnoreCase("1"))
        {
            pageCount = 0;
            tvTitleNoData.setText(view.getContext().getString(R.string.alert));
            tvMessageNoData.setText(view.getContext().getString(R.string.you_are_block_by)+" "+blockedByUserName);
        }
        else
        {
            setNoData();
        }

        if (dataList == null)
            dataList = new ArrayList<>();

        isApiRun = true;
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.U_ID, ""));

            if (!is_my_profile) {
                parameters.put("other_user_id", userId);
            }
            parameters.put("starting_point", "" + pageCount);

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(getActivity(), ApiLinks.showUserRepostedVideos, parameters,Functions.getHeaders(getActivity()), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(getActivity(),resp);
                isApiRun = false;
                parseData(resp,isScrollToTop);
            }
        });


    }

    public void parseData(String responce,boolean isScrollToTop) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msg = jsonObject.optJSONArray("msg");
                ArrayList<HomeModel> temp_list = new ArrayList<>();

                for (int i = 0; i < msg.length(); i++) {
                    JSONObject itemdata = msg.optJSONObject(i);

                    JSONObject video = itemdata.optJSONObject("Video");
                    JSONObject user = video.optJSONObject("User");
                    JSONObject sound = video.optJSONObject("Sound");
                    JSONObject userPrivacy = user.optJSONObject("PrivacySetting");
                    JSONObject userPushNotification = user.optJSONObject("PushNotification");

                    HomeModel item = Functions.parseVideoData(user, sound, video, userPrivacy, userPushNotification);

                    if (!(isUserAlreadyBlock.equalsIgnoreCase("1")))
                    {
                        if (item.user_id!=null && !(item.user_id.equals("null")) && !(item.user_id.equals("0")))
                        {
                            temp_list.add(item);
                        }
                    }
                }

                if (pageCount == 0) {
                    dataList.clear();
                    dataList.addAll(temp_list);
                } else {
                    dataList.addAll(temp_list);
                }
                adapter.notifyDataSetChanged();
            }
            else
            {
                if (pageCount==0)
                {
                    pageCount=0;
                    dataList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            if (dataList.isEmpty()) {
                view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.no_data_layout).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.d(Constants.TAG_,"Exception: "+e);
        } finally {
            loadMoreProgress.setVisibility(View.GONE);
        }
    }


    // open the videos in full screen on click
    private void openWatchVideo(int postion) {

        Intent intent = new Intent(getActivity(), WatchVideosA.class);
        intent.putExtra("arraylist", dataList);
        intent.putExtra("position", postion);
        intent.putExtra("pageCount", pageCount);
        intent.putExtra("userId",userId);
        intent.putExtra("whereFrom","repostVideo");
        resultCallback.launch(intent);
    }

    ActivityResultLauncher<Intent> resultCallback = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data.getBooleanExtra("isShow",false))
                        {
                            dataList.clear();
                            dataList.addAll((ArrayList<HomeModel>) data.getSerializableExtra("arraylist"));
                            pageCount=data.getIntExtra("pageCount",0);
                            adapter.notifyDataSetChanged();
                        }

                    }
                }
            });


}
