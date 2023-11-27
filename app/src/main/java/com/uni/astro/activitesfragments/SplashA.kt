package com.uni.astro.activitesfragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.uni.astro.Constants
import com.uni.astro.R
import com.uni.astro.apiclasses.ApiLinks
import com.uni.astro.databinding.ActivitySplashBinding
import com.uni.astro.mainmenu.MainMenuActivity
import com.uni.astro.simpleclasses.AppCompatLocaleActivity
import com.uni.astro.simpleclasses.Functions
import com.uni.astro.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import io.paperdb.Paper
import org.json.JSONObject

class SplashA : AppCompatLocaleActivity() {
    private lateinit var bind: ActivitySplashBinding

    private var countDownTimer: CountDownTimer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideNavigation()

        Functions.setLocale(
            Functions.getSharedPreference(this@SplashA)
                .getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this, javaClass, false
        )

        bind = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(bind.root)

        apiCallHit()
    }

    private fun apiCallHit() {
        callApiForGetad()
        callApiForGetSettings()

        if (Functions.getSharedPreference(this).getString(Variables.DEVICE_ID, "0") == "0") {
            callApiRegisterDevice()
        } else {
            setTimer()
        }
    }

    private fun callApiForGetSettings() {
        val params = JSONObject()

        VolleyRequest.JsonPostRequest(
            this@SplashA,
            ApiLinks.showSettings,
            params,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this@SplashA, resp)
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code != null && code == "200") {
                    val msg = jsonObject.optJSONArray("msg")
                    for (i in 0 until msg.length()) {
                        val jsonObject1 = msg.optJSONObject(i)
                        val settings = jsonObject1.optJSONObject("Setting")
                        val type = settings.getString("type")
                        if (type.equals("show_advert_after", ignoreCase = true)) {
                            Functions.getSettingsPreference(this@SplashA).edit()
                                .putInt(Variables.ShowAdvertAfter, settings.optInt("value", 0))
                                .commit()
                        }
                        if (type.equals("coin_worth", ignoreCase = true)) {
                            Functions.getSettingsPreference(this@SplashA).edit()
                                .putString(Variables.CoinWorth, settings.optString("value", "0"))
                                .commit()
                        }
                        if (type.equals("add_type", ignoreCase = true)) {
                            Functions.getSettingsPreference(this@SplashA).edit()
                                .putString(Variables.AddType, settings.optString("value", "0"))
                                .commit()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun callApiForGetad() {
        val params = JSONObject()
        try {
            if (Functions.getSharedPreference(this@SplashA).getBoolean(Variables.IS_LOGIN, false)) {
                params.put(
                    "user_id",
                    Functions.getSharedPreference(this@SplashA).getString(Variables.U_ID, "")
                )
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }
        VolleyRequest.JsonPostRequest(
            this@SplashA,
            ApiLinks.showVideoDetailAd,
            params,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this@SplashA, resp)
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code != null && code == "200") {
                    val msg = jsonObject.optJSONObject("msg")
                    val video = msg.optJSONObject("Video")
                    val user = msg.optJSONObject("User")
                    val sound = msg.optJSONObject("Sound")
                    val pushNotification = user.optJSONObject("PushNotification")
                    val privacySetting = user.optJSONObject("PrivacySetting")
                    val item = Functions.parseVideoData(
                        user,
                        sound,
                        video,
                        privacySetting,
                        pushNotification
                    )
                    item.promote = "1"
                    Paper.book(Variables.PromoAds).write(Variables.PromoAdsModel, item)
                } else {
                    Paper.book(Variables.PromoAds).destroy()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // show the splash for 3 sec
    private fun setTimer() {
        countDownTimer = object : CountDownTimer(2500, 500) {
            override fun onTick(millisUntilFinished: Long) {
                // this will call on every 500 ms
            }

            override fun onFinish() {
                if (Functions.getSettingsPreference(this@SplashA)
                        .getBoolean(Variables.IsPrivacyPolicyAccept, false)
                ) {
                    val intent = Intent(this@SplashA, MainMenuActivity::class.java)
                    if (getIntent().extras != null) {
                        try {
                            // its for multiple account notification handling
                            val userId = getIntent().getStringExtra("receiver_id")
                            Functions.setUpSwitchOtherAccount(this@SplashA, userId)
                        } catch (e: Exception) {
                        }
                        intent.putExtras(getIntent().extras!!)
                        setIntent(null)
                    }
                    startActivity(intent)
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
                    finish()
                } else {
                    openWebUrl(getString(R.string.terms_of_use), Constants.terms_conditions)
                }
            }
        }.start()
    }

    fun openWebUrl(title: String?, url: String?) {
        val intent = Intent(this@SplashA, WebviewA::class.java)
        intent.putExtra("url", url)
        intent.putExtra("title", title)
        intent.putExtra("from", "splash")
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onDestroy() {
        super.onDestroy()
        if (countDownTimer != null) countDownTimer!!.cancel()
    }

    // register the device on server on application open
    private fun callApiRegisterDevice() {
        val androidId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val param = JSONObject()
        try {
            param.put("key", androidId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this,
            ApiLinks.registerDevice,
            param,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this@SplashA, resp)
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code == "200") {
                    setTimer()
                    val msg = jsonObject.optJSONObject("msg")
                    val Device = msg.optJSONObject("Device")
                    val editor2 = Functions.getSharedPreference(this@SplashA).edit()
                    editor2.putString(Variables.DEVICE_ID, Device.optString("id")).commit()
                } else {
                    setTimer()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // this will hide the bottom mobile navigation controll
    private fun hideNavigation() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // This work only for android 4.4+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = flags

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            val decorView = window.decorView
            decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    decorView.systemUiVisibility = flags
                }
            }
        }
    }
}