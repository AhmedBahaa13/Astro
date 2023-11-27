package com.uni.astro.activitesfragments.livestreaming.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.uni.astro.R;
import com.uni.astro.activitesfragments.livestreaming.adapter.ViewersAdapter;
import com.uni.astro.databinding.FragmentViewersBinding;
import com.uni.astro.interfaces.AdapterClickListener;
import com.uni.astro.interfaces.FragmentCallBack;
import com.uni.astro.models.StreamJoinModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewersListF extends BottomSheetDialogFragment implements View.OnClickListener {

    Context context;

    FragmentCallBack callBack;
    String stramingId;

    ViewersAdapter adapter;

    DatabaseReference rootref;

    FragmentViewersBinding binding;

    public ViewersListF() {

    }

    public ViewersListF(String id, FragmentCallBack callBack) {
        this.stramingId=id;
        this.callBack = callBack;
    }


    public static ViewersListF newInstance(String id, FragmentCallBack callBack) {
        ViewersListF fragment = new ViewersListF(id,callBack);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_viewers, container, false);
        context=getContext();
        rootref = FirebaseDatabase.getInstance().getReference();

        binding.goBack.setOnClickListener(this);
        setAdapter();
        ListenerJoinNode();

        return binding.getRoot();
    }



    private void setAdapter() {
        LinearLayoutManager layoutManager=new LinearLayoutManager(context);
        binding.recylerview.setLayoutManager(layoutManager);
        adapter=new ViewersAdapter(context,jointUserList, new AdapterClickListener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

            }
        });
        binding.recylerview.setAdapter(adapter);
    }



    ArrayList<StreamJoinModel> jointUserList=new ArrayList<>();
    ValueEventListener joinValueEventListener;

    private void ListenerJoinNode() {
        if (joinValueEventListener==null) {
            joinValueEventListener=new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists())
                    {
                        jointUserList.clear();
                        String duetConnectedUserId=snapshot.child("duetConnectedUserId").getValue().toString();
                        for (DataSnapshot childShot:snapshot.child("JoinStream").getChildren())
                        {
                            if (!(TextUtils.isEmpty(childShot.getValue().toString())))
                            {
                                StreamJoinModel model=childShot.getValue(StreamJoinModel.class);
                                if (!(duetConnectedUserId.equals(model.userId)))
                                {
                                    jointUserList.add(model);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            rootref.child("LiveStreamingUsers").child(stramingId)
                    .addValueEventListener(joinValueEventListener);
        }
    }

    public void removeJoinListener() {
        if (rootref!=null && joinValueEventListener != null) {
            rootref.child("LiveStreamingUsers").child(stramingId).removeEventListener(joinValueEventListener);
            joinValueEventListener=null;
        }
    }




    @Override
    public void onDestroy() {
        removeJoinListener();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.goBack:
                dismiss();
                break;
        }
    }
}
