package com.uni.astro.activitesfragments.profile.settings;

import static com.uni.astro.Constants.TAG_;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.uni.astro.R;
import com.uni.astro.activitesfragments.profile.models.CloseFriendModel;
import com.uni.astro.adapters.CloseFriendsAdapter;
import com.uni.astro.apiclasses.ApiLinks;
import com.uni.astro.models.UserModel;
import com.uni.astro.simpleclasses.DataParsing;
import com.uni.astro.simpleclasses.Functions;
import com.uni.astro.simpleclasses.Variables;
import com.volley.plus.VPackages.VolleyRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CloseFriendsA extends AppCompatActivity {
    private List<CloseFriendModel> friendsList;
    private List<String> closeFriendsList;
    private String receiverId;
    private CloseFriendsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_close_friends);
        friendsList = getFriends();
        Button saveCloseFriendsButton = findViewById(R.id.saveFriendsButton);
        closeFriendsList = new ArrayList<>();
        adapter = new CloseFriendsAdapter(
                this,
                (position) -> {
                    receiverId = friendsList.get(position).fb_id;
                    closeFriendsList.add(receiverId);
                }
        );
        RecyclerView closeFriendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        closeFriendsRecyclerView.setAdapter(adapter);
        saveCloseFriendsButton.setOnClickListener(
                (view) -> saveCloseFriends(closeFriendsList)
        );
    }

    public List<CloseFriendModel> getFriends() {
        JSONObject parameters = new JSONObject();
        List<CloseFriendModel> _friends = new ArrayList<>();
        try {
            parameters.put("user_id", Functions.getSharedPreference(this).getString(Variables.U_ID, ""));
//            parameters.put("starting_point", 0);
        } catch (JSONException e) {
            Log.d(TAG_, "getFriends: " + e);
        }

        VolleyRequest.JsonPostRequest(this, ApiLinks.showFollowing, parameters, Functions.getHeaders(this), (resp) -> {
            Functions.checkStatus(this, resp);
            try {
                JSONObject response = new JSONObject(resp);
                if (response.optString("code").equals("200")) {
                    JSONArray friendsList = response.getJSONArray("msg");
                    for (int i = 0; i < friendsList.length(); i++) {
                        JSONObject _friend = friendsList.optJSONObject(i);
                        UserModel userDetail = DataParsing.getUserDataModel(_friend.optJSONObject("FollowingList"));
                        CloseFriendModel friend = new CloseFriendModel();
                        friend.fb_id = userDetail.getId();
                        friend.first_name = userDetail.getFirstName();
                        friend.last_name = userDetail.getLastName();
                        friend.username = userDetail.getUsername();
                        friend.setProfile_pic(userDetail.getProfilePic());

                        friend.isClose = _friend.optBoolean("is_close_friend");
                        _friends.add(friend);

                    }
                    adapter.setUsersList(_friends);

                } else if (friendsList.isEmpty()) {
                    findViewById(R.id.tvNoSuggestionFound).setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                Log.d(TAG_, "getFriends: " + e);
            }

        });

        return _friends;
    }


    private void saveCloseFriends(List<String> closeFriendsList){
        for (int i = 0; i < closeFriendsList.size(); i++){
            JSONObject parameters = new JSONObject();
            try {
                parameters.put("sender_id",
                        Functions.getSharedPreference(this).getString(Variables.U_ID, ""));
                parameters.put("receiver_id", closeFriendsList.get(i));
            } catch (JSONException e) {
                Log.d(TAG_, "saveCloseFriends: " + e);
            }

            VolleyRequest.JsonPostRequest(this, ApiLinks.saveCloseFriends, parameters, Functions.getHeaders(this), (resp) ->{
                try {
                    JSONObject response = new JSONObject(resp);
                    if (response.optString("code").equals("200")){
                        Toast.makeText(this, "close friends added successfully", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "an error occurred, please try again", Toast.LENGTH_SHORT).show();
                }

            });
        }
    }

}