package com.uni.astro.activitesfragments.profile.settings;

import com.uni.astro.simpleclasses.AppCompatLocaleActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.uni.astro.activitesfragments.accounts.LoginA;
import com.uni.astro.activitesfragments.accounts.ManageAccountsF;
import com.uni.astro.apiclasses.ApiLinks;
import com.volley.plus.VPackages.VolleyRequest;
import com.uni.astro.Constants;
import com.volley.plus.interfaces.Callback;
import com.uni.astro.interfaces.FragmentCallBack;
import com.uni.astro.mainmenu.MainMenuActivity;
import com.uni.astro.R;
import com.uni.astro.simpleclasses.Functions;
import com.uni.astro.simpleclasses.Variables;

import org.json.JSONObject;

import io.paperdb.Paper;

public class DeleteAccountA extends AppCompatLocaleActivity implements View.OnClickListener {

    TextView tvDeleteTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Functions.setLocale(Functions.getSharedPreference(DeleteAccountA.this).getString(Variables.APP_LANGUAGE_CODE,Variables.DEFAULT_LANGUAGE_CODE)
                , this,getClass(),false);
        setContentView(R.layout.activity_delete_account);

        InitControl();
    }

    private void InitControl() {
        tvDeleteTitle=findViewById(R.id.tvDeleteTitle);
        tvDeleteTitle.setText(getString(R.string.delete_account)+"\n"+
                Functions.getSharedPreference(DeleteAccountA.this).getString(Variables.U_NAME,"")+"?");
        findViewById(R.id.goBack).setOnClickListener(this);
        findViewById(R.id.deleteAccount).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.goBack:
            {
                DeleteAccountA.super.onBackPressed();
            }
            break;
            case R.id.deleteAccount:
            {
                logoutProceed();
            }
        }
    }

    private void callDeleteApi() {
        JSONObject sendobj = new JSONObject();
        try {
            sendobj.put("user_id", Functions.getSharedPreference(DeleteAccountA.this).getString(Variables.U_ID, ""));

        } catch (Exception e) {
            e.printStackTrace();
        }

        VolleyRequest.JsonPostRequest(DeleteAccountA.this, ApiLinks.deleteUserAccount, sendobj,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(DeleteAccountA.this,resp);

                if (resp!=null){

                    try {
                        JSONObject respobj = new JSONObject(resp);

                        if (respobj.optString("code").equals("200")){
                            logout();
                        }
                        else
                        {
                            Functions.showToast(DeleteAccountA.this,respobj.optString("msg"));
                        }

                    } catch (Exception e) {
                        Log.d(Constants.TAG_,"Exception : "+e);
                    }

                }

            }
        });
    }


    private void logoutProceed() {
        if (Paper.book(Variables.MultiAccountKey).getAllKeys().size()>1)
        {
            Functions.showDoubleButtonAlert(DeleteAccountA.this,
                    getString(R.string.are_you_sure_to_delete_account),
                    "",
                    getString(R.string.delete_account),
                    getString(R.string.switch_account),true,
                    new FragmentCallBack() {
                        @Override
                        public void onResponce(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                openManageMultipleAccounts();
                            }
                            else
                            {
                                callDeleteApi();
                            }
                        }
                    }
            );
        }
        else
        {
            Functions.showDoubleButtonAlert(DeleteAccountA.this,
                    getString(R.string.are_you_sure_to_delete_account),
                    "",
                    getString(R.string.cancel_),
                    getString(R.string.delete_account),true,
                    new FragmentCallBack() {
                        @Override
                        public void onResponce(Bundle bundle) {
                            if (bundle.getBoolean("isShow",false))
                            {
                                callDeleteApi();
                            }
                        }
                    }
            );
        }
    }


    public void logout() {

        JSONObject params = new JSONObject();
        try {
            params.put("user_id", Functions.getSharedPreference(DeleteAccountA.this).getString(Variables.U_ID, ""));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Functions.showLoader(DeleteAccountA.this,false,false);
        VolleyRequest.JsonPostRequest(DeleteAccountA.this, ApiLinks.logout, params,Functions.getHeaders(this), new Callback() {
            @Override
            public void onResponce(String resp) {
                Functions.checkStatus(DeleteAccountA.this,resp);
                Functions.cancelLoader();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");

                    if (code.equalsIgnoreCase("200")) {
                       removePreferenceData();
                    }
                    else
                    {
                        removePreferenceData();
                    }

                } catch (Exception e) {
                    Log.d(Constants.TAG_,"Exception : "+e);
                }


            }
        });

    }

    private void removePreferenceData() {
        Paper.book(Variables.PrivacySetting).destroy();

        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(DeleteAccountA.this, gso);
        googleSignInClient.signOut();

        LoginManager.getInstance().logOut();

        Functions.removeMultipleAccount(DeleteAccountA.this);

        SharedPreferences.Editor editor = Functions.getSharedPreference(DeleteAccountA.this).edit();
        editor.clear();
        editor.commit();

        Functions.setUpExistingAccountLogin(DeleteAccountA.this);

        Intent intent=new Intent(DeleteAccountA.this, MainMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    private void openManageMultipleAccounts() {
        ManageAccountsF f = new ManageAccountsF(new FragmentCallBack() {
            @Override
            public void onResponce(Bundle bundle) {
                if (bundle.getBoolean("isShow",false))
                {
                    Functions.hideSoftKeyboard(DeleteAccountA.this);
                    Intent intent = new Intent(DeleteAccountA.this, LoginA.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                }
            }
        });
        f.show(getSupportFragmentManager(), "");
    }
}