package com.uni.astro.activitesfragments.videorecording;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appyvet.materialrangebar.RangeBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.uni.astro.Constants;
import com.uni.astro.R;
import com.uni.astro.interfaces.FragmentCallBack;
import com.uni.astro.simpleclasses.Functions;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordingTimeRangF extends BottomSheetDialogFragment implements View.OnClickListener {

    View view;
    Context context;
    RangeBar seekbar;

    int selectedValue = 3;
    int recordingDoneTime = 0, totalTime = 0;
    TextView rangeTxt;

    public RecordingTimeRangF() {
        // Required empty public constructor
    }

    FragmentCallBack fragmentCallBack;

    public RecordingTimeRangF(FragmentCallBack fragmentCallBack) {
        this.fragmentCallBack = fragmentCallBack;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_recording_time_rang, container, false);
        context = getContext();

        Bundle bundle = getArguments();
        if (bundle != null) {
            recordingDoneTime = bundle.getInt("end_time");
            totalTime = bundle.getInt("total_time");
        }
        seekbar = view.findViewById(R.id.seekbar);
        seekbar.setOnlyOnDrag(true);
        seekbar.setTickEnd(totalTime);

        seekbar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {

                Functions.printLog(Constants.TAG_, "" + leftPinIndex);
                Functions.printLog(Constants.TAG_, "" + rightPinIndex);
                Functions.printLog(Constants.TAG_, "" + selectedValue);


                if (leftPinIndex > 0) {
                    seekbar.setRangePinsByValue(0, rightPinIndex);
                } else if (rightPinIndex < recordingDoneTime) {
                    seekbar.setRangePinsByValue(0, recordingDoneTime);
                }

                rangeTxt.setText(rightPinIndex + "s/" + totalTime + "s");
                selectedValue = rightPinIndex;

            }

            @Override
            public void onTouchStarted(RangeBar rangeBar) {

            }

            @Override
            public void onTouchEnded(RangeBar rangeBar) {

            }
        });

        rangeTxt = view.findViewById(R.id.range_txt);
        rangeTxt.setText(totalTime + "s");

        view.findViewById(R.id.start_recording_layout).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_recording_layout:
                Bundle bundle = new Bundle();
                bundle.putInt("end_time", selectedValue);
                fragmentCallBack.onResponce(bundle);
                dismiss();
                break;
        }
    }


}
