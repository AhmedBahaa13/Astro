package com.uni.astro.simpleclasses;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.realpacific.clickshrinkeffect.ClickShrinkUtils;
import com.uni.astro.R;
import com.uni.astro.activitesfragments.spaces.utils.CookieBar;
import com.uni.astro.interfaces.FragmentCallBack;

public class Dialogs {
    // initialize the loader dialog and show


    //show permission setting screen
    static CookieBar invitationcCookieBar;

    public static void showInvitationDialog(Activity activity, String userName, FragmentCallBack callBack) {
        invitationcCookieBar = CookieBar.build(activity)
                .setCustomView(R.layout.item_speaker_alert)
                .setCustomViewInitializer(view -> {
                    TextView tvTitle = view.findViewById(R.id.tvTitle);
                    LinearLayout tabMaybeLater = view.findViewById(R.id.tabMaybeLater);
                    LinearLayout tabJoinAsSpeaker = view.findViewById(R.id.tabJoinAsSpeaker);
                    String s = "&#128075; <b>" + userName + "</b> " + view.getContext().getString(R.string.invited_you_to_join_as_a_speaker);
                    tvTitle.setText(Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY));

                    tabMaybeLater.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("isShow", false);
                            callBack.onResponce(bundle);
                            CookieBar.dismiss(activity);
                        }
                    });
                    ClickShrinkUtils.applyClickShrink(tabMaybeLater);

                    tabJoinAsSpeaker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("isShow", true);
                            callBack.onResponce(bundle);
                            CookieBar.dismiss(activity);
                        }
                    });
                    ClickShrinkUtils.applyClickShrink(tabJoinAsSpeaker);
                })
                .setDuration(3600000)
                .setSwipeToDismiss(true)
                .show();
    }

    public static void closeInvitationCookieBar(Activity activity) {
        if (invitationcCookieBar != null) {
            CookieBar.dismiss(activity);
        }
    }
}
