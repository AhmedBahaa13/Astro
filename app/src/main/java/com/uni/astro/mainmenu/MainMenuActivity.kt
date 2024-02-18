package com.uni.astro.mainmenu

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.uni.astro.Constants
import com.uni.astro.R
import com.uni.astro.activitesfragments.DiscoverF
import com.uni.astro.activitesfragments.HomeF
import com.uni.astro.activitesfragments.WatchVideosA
import com.uni.astro.activitesfragments.accounts.LoginA
import com.uni.astro.activitesfragments.chat.ChatA
import com.uni.astro.activitesfragments.livestreaming.activities.MultiViewLiveA
import com.uni.astro.activitesfragments.livestreaming.activities.SingleCastJoinA
import com.uni.astro.activitesfragments.livestreaming.model.LiveUserModel
import com.uni.astro.activitesfragments.profile.ProfileA
import com.uni.astro.activitesfragments.profile.ProfileTabF
import com.uni.astro.activitesfragments.profile.settings.ShowLocationPermissionF
import com.uni.astro.activitesfragments.profile.settings.ShowPostNotificationPermissionF
import com.uni.astro.activitesfragments.spaces.RiseHandForSpeakF
import com.uni.astro.activitesfragments.spaces.RiseHandUsersF
import com.uni.astro.activitesfragments.spaces.RoomDetailBottomSheet
import com.uni.astro.activitesfragments.spaces.SpaceTabF
import com.uni.astro.activitesfragments.spaces.models.HomeUserModel
import com.uni.astro.activitesfragments.spaces.services.RoomStreamService
import com.uni.astro.activitesfragments.spaces.utils.RoomManager.*
import com.uni.astro.activitesfragments.videorecording.CreateContentF
import com.uni.astro.activitesfragments.videorecording.PostVideoA
import com.uni.astro.activitesfragments.walletandwithdraw.MyWallet
import com.uni.astro.adapters.ViewPagerAdapter
import com.uni.astro.apiclasses.ApiLinks
import com.uni.astro.databinding.ActivityMainMenuBinding
import com.uni.astro.firebasenotification.NotificationActionHandler
import com.uni.astro.interfaces.FragmentCallBack
import com.uni.astro.models.InviteForSpeakModel
import com.uni.astro.models.StreamJoinModel
import com.uni.astro.models.UserModel
import com.uni.astro.simpleclasses.*
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject


class MainMenuActivity : AppCompatLocaleActivity() {
    private lateinit var binding: ActivityMainMenuBinding

    var mBackPressed: Long = 0
    var context: Context? = null
    var takePermissionUtils: PermissionUtils? = null
    protected var pager: ViewPager2? = null
    private var adapter: ViewPagerAdapter? = null
    var rootRef: DatabaseReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } catch (_: Exception) {
        }

        Functions.setLocale(
            Functions.getSharedPreference(this@MainMenuActivity)
                .getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE),
            this,
            javaClass,
            false
        )

        binding = DataBindingUtil.setContentView(this@MainMenuActivity, R.layout.activity_main_menu)

        context = this@MainMenuActivity
        mainMenuActivity = this
        rootRef = FirebaseDatabase.getInstance().reference

        val intent = intent
        chechDeepLink(intent)
        if (intent != null && intent.hasExtra("type")) {
            val actionIntent = Intent(this, NotificationActionHandler::class.java)
            actionIntent.putExtra("title", "" + intent.getStringExtra("title"))
            actionIntent.putExtra("body", "" + intent.getStringExtra("body"))
            actionIntent.putExtra("image", "" + intent.getStringExtra("image"))
            actionIntent.putExtra("receiver_id", "" + intent.getStringExtra("receiver_id"))
            actionIntent.putExtra("sender_id", "" + intent.getStringExtra("sender_id"))
            actionIntent.putExtra("user_id", "" + intent.getStringExtra("user_id"))
            actionIntent.putExtra("video_id", "" + intent.getStringExtra("video_id"))
            actionIntent.putExtra("type", "" + intent.getStringExtra("type"))
            sendBroadcast(actionIntent)
        }

        if (Functions.getSharedPreference(this).getBoolean(Variables.IS_LOGIN, false)) {
            publicIP
        }

        Functions.makeDirectry(Functions.getAppFolder(this) + Variables.APP_HIDED_FOLDER)
        Functions.makeDirectry(Functions.getAppFolder(this) + Variables.DRAFT_APP_FOLDER)
        setIntent(null)
        if (Functions.getSharedPreference(this).getString(Variables.countryRegion, "null")
                .equals("null", ignoreCase = true)
        ) {
            val region = Functions.getCountryCode(this)
            Functions.getSharedPreference(this).edit().putString(Variables.countryRegion, region)
                .commit()
        }

        checkCurrentLocationUpdates()
        checkPostNotificationPermission()
        SetTabs()

        //setRoomListerner();
    }

    private fun checkPostNotificationPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            takePermissionUtils =
                PermissionUtils(this@MainMenuActivity, mPermissionPostNotificationResult)
            if (!takePermissionUtils!!.isPostNotificationPermissionGranted) {
                postNotificationPermission
            }
        }
    }

    private val postNotificationPermission: Unit
        private get() {
            val fragment = ShowPostNotificationPermissionF.newInstance { bundle ->
                if (bundle.getBoolean("isShow")) {
                    takePermissionUtils!!.takePostNotificationPermission()
                }
            }
            fragment.show(supportFragmentManager, "ShowPostNotificationPermissionF")
        }

    private fun checkCurrentLocationUpdates() {
        takePermissionUtils = PermissionUtils(this@MainMenuActivity, mPermissionLocationResult)
        if (takePermissionUtils!!.isLocationPermissionGranted) {
            loactionLatlng
        } else {
            //locationPermission
        }
    }

    fun SetTabs() {
        adapter = ViewPagerAdapter(this)
        pager = findViewById(R.id.viewpager)
        tabLayout = findViewById(R.id.tabs)
        pager!!.offscreenPageLimit = 4
        registerFragmentWithPager()
        pager!!.adapter = adapter
        addTabs()
        setupTabIcons()
        pager!!.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout!!.getTabAt(position)!!.select()
            }
        })
        pager!!.isUserInputEnabled = false
    }

    private fun setupTabIcons() {
        val view1 = LayoutInflater.from(context).inflate(R.layout.item_tablayout, null)
        val imageView1 = view1.findViewById<ImageView>(R.id.image)
        val title1 = view1.findViewById<TextView>(R.id.text)
        imageView1.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.video_library))
        imageView1.setColorFilter(
            ContextCompat.getColor(context!!, R.color.whiteColor),
            PorterDuff.Mode.SRC_IN
        )
        title1.text = context!!.getString(R.string.home)
        title1.setTextColor(ContextCompat.getColor(context!!, R.color.whiteColor))
//        view1.updateLayoutParams {
//            (this as LinearLayout.LayoutParams).topMargin =
//                resources.getDimension(R.dimen._16sdp).toInt()
//        }
        tabLayout!!.getTabAt(0)!!.customView = view1
        val view2 = LayoutInflater.from(context).inflate(R.layout.item_tablayout, null)
        val imageView2 = view2.findViewById<ImageView>(R.id.image)
        val title2 = view2.findViewById<TextView>(R.id.text)
        imageView2.setImageDrawable(
            ContextCompat.getDrawable(
                context!!,
                R.drawable.explore_trend_icon
            )
        )
        imageView2.setColorFilter(
            ContextCompat.getColor(context!!, R.color.unselected_tab),
            PorterDuff.Mode.SRC_IN
        )
        title2.text = context!!.getString(R.string.discover)
        title2.setTextColor(ContextCompat.getColor(context!!, R.color.unselected_tab))
        tabLayout!!.getTabAt(1)!!.customView = view2
        val view3 = LayoutInflater.from(context).inflate(R.layout.item_add_tab_layout, null)
        tabLayout!!.getTabAt(2)!!.customView = view3
        val view4 = LayoutInflater.from(context).inflate(R.layout.space_item_tablayout, null)
        val imageView4 = view4.findViewById<ImageView>(R.id.image)
        val title4 = view4.findViewById<TextView>(R.id.text)
        imageView4.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.space_ic))
        imageView4.setColorFilter(
            ContextCompat.getColor(context!!, R.color.unselected_tab),
            PorterDuff.Mode.SRC_IN
        )
        title4.text = context!!.getString(R.string.uni)
        title4.setTextColor(ContextCompat.getColor(context!!, R.color.unselected_tab))
        tabLayout!!.getTabAt(3)!!.customView = view4
        val view5 = LayoutInflater.from(context).inflate(R.layout.item_tablayout, null)
        val imageView5 = view5.findViewById<ImageView>(R.id.image)
        val title5 = view5.findViewById<TextView>(R.id.text)
        imageView5.setImageDrawable(
            ContextCompat.getDrawable(
                context!!,
                R.drawable.profile_icon
            )
        )
        imageView5.setColorFilter(
            ContextCompat.getColor(context!!, R.color.unselected_tab),
            PorterDuff.Mode.SRC_IN
        )
        title5.text = context!!.getString(R.string.profile)
        title5.setTextColor(ContextCompat.getColor(context!!, R.color.unselected_tab))
        tabLayout!!.getTabAt(4)!!.customView = view5
        tabLayout!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val v = tab.customView
                val image = v!!.findViewById<ImageView>(R.id.image)
                val title = v.findViewById<TextView>(R.id.text)
                when (tab.position) {
                    0 -> {
                        Functions.blackStatusBar(this@MainMenuActivity)
                        onHomeClick()
                        image.setImageDrawable(
                            ContextCompat.getDrawable(
                                context!!,
                                R.drawable.video_library
                            )
                        )
                        image.setColorFilter(
                            ContextCompat.getColor(context!!, R.color.whiteColor),
                            PorterDuff.Mode.SRC_IN
                        )
                        title.setTextColor(ContextCompat.getColor(context!!, R.color.whiteColor))
                    }

                    1 -> {
                        Functions.whiteStatusBar(this@MainMenuActivity)
                        onotherTabClick()
                        image.setImageDrawable(
                            ContextCompat.getDrawable(
                                context!!,
                                R.drawable.explore_trend_icon
                            )
                        )
                        image.setColorFilter(
                            ContextCompat.getColor(context!!, R.color.white),
                            PorterDuff.Mode.SRC_IN
                        )
                        title.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                    }

                    3 -> {
                        Functions.whiteStatusBar(this@MainMenuActivity)
                        onotherTabClick()
                        image.setImageDrawable(
                            ContextCompat.getDrawable(
                                context!!,
                                R.drawable.space_ic
                            )
                        )
                        image.setColorFilter(
                            ContextCompat.getColor(context!!, R.color.white),
                            PorterDuff.Mode.SRC_IN
                        )
                        title.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                    }

                    4 -> {
                        Functions.whiteStatusBar(this@MainMenuActivity)
                        onotherTabClick()
                        image.setImageDrawable(
                            ContextCompat.getDrawable(
                                context!!,
                                R.drawable.profile_icon
                            )
                        )
                        image.setColorFilter(
                            ContextCompat.getColor(context!!, R.color.white),
                            PorterDuff.Mode.SRC_IN
                        )
                        title.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                    }
                }
                tab.customView = v
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val v = tab.customView
                val image = v!!.findViewById<ImageView>(R.id.image)
                val title = v.findViewById<TextView>(R.id.text)
                when (tab.position) {
                    0 -> {
                        image.setImageDrawable(
                            ContextCompat.getDrawable(
                                context!!,
                                R.drawable.ic_home_gray
                            )
                        )
                        image.setColorFilter(
                            ContextCompat.getColor(context!!, R.color.unselected_tab),
                            PorterDuff.Mode.SRC_IN
                        )
                        title.setTextColor(ContextCompat.getColor(context!!, R.color.unselected_tab))
                    }

                    1 -> {
                        image.setImageDrawable(
                            ContextCompat.getDrawable(
                                context!!,
                                R.drawable.explore_trend_icon
                            )
                        )
                        image.setColorFilter(
                            ContextCompat.getColor(context!!, R.color.unselected_tab),
                            PorterDuff.Mode.SRC_IN
                        )
                        title.setTextColor(ContextCompat.getColor(context!!, R.color.unselected_tab))
                    }

                    3 -> {
                        image.setImageDrawable(
                            ContextCompat.getDrawable(
                                context!!,
                                R.drawable.space_ic
                            )
                        )
                        image.setColorFilter(
                            ContextCompat.getColor(context!!, R.color.unselected_tab),
                            PorterDuff.Mode.SRC_IN
                        )
                        title.setTextColor(ContextCompat.getColor(context!!, R.color.unselected_tab))
                    }

                    4 -> {
                        image.setImageDrawable(
                            ContextCompat.getDrawable(
                                context!!,
                                R.drawable.ic_profile_gray
                            )
                        )
                        image.setColorFilter(
                            ContextCompat.getColor(context!!, R.color.unselected_tab),
                            PorterDuff.Mode.SRC_IN
                        )
                        title.setTextColor(ContextCompat.getColor(context!!, R.color.unselected_tab))
                    }
                }
                tab.customView = v
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        val tabStrip = tabLayout!!.getChildAt(0) as LinearLayout
        tabStrip.isEnabled = false
        tabStrip.getChildAt(2).isClickable = false
        view3.setOnClickListener { v: View? ->
            takePermissionUtils = PermissionUtils(this@MainMenuActivity, mPermissionResult)
            if (takePermissionUtils!!.isStorageCameraRecordingPermissionGranted) {
                uploadNewVideo()
            } else {
                takePermissionUtils!!.showStorageCameraRecordingPermissionDailog(
                    context!!.getString(
                        R.string.we_need_storage_camera_recording_permission_for_make_new_video
                    )
                )
            }
        }

        tabStrip.getChildAt(3).isClickable = false
        view4.setOnClickListener { v: View? ->
            if (Functions.checkLoginUser(this@MainMenuActivity)) {
                val tab = tabLayout!!.getTabAt(3)
                tab!!.select()
            }
        }
        tabStrip.getChildAt(4).isClickable = false
        view5.setOnClickListener { v: View? ->
            if (Functions.checkLoginUser(this@MainMenuActivity)) {
                val tab = tabLayout!!.getTabAt(4)
                tab!!.select()
            }
        }
        onHomeClick()
        if (intent != null) {
            if (intent.hasExtra("action_type")) {
                if (Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN, false)) {
                    val action_type = intent.extras!!.getString("action_type")
                    if (action_type == "message") {
                        Handler(Looper.getMainLooper()).postDelayed({
                            val tab = tabLayout!!.getTabAt(3)
                            tab!!.select()
                        }, 1500)
                        val id = intent.extras!!.getString("senderid")
                        val name = intent.extras!!.getString("title")
                        val icon = intent.extras!!.getString("icon")
                        chatFragment(id, name, icon)
                    }
                }
            }
        }
        for (tabNum in 0 until tabLayout!!.tabCount) {
            val tab = tabLayout!!.getTabAt(tabNum)
            tab?.customView?.updateLayoutParams {
                if (tab.position == 2) {
                    (this as ViewGroup.MarginLayoutParams).bottomMargin =
                        resources.getDimension(R.dimen._9sdp).toInt()
                }
//                else (this as LinearLayout.LayoutParams).topMargin =
//                    resources.getDimension(R.dimen._1sdp).toInt()
            }
        }
    }

    // open the chat fragment when click on notification of message
    fun chatFragment(receiverid: String?, name: String?, picture: String?) {
        val intent = Intent(context, ChatA::class.java)
        intent.putExtra("user_id", receiverid)
        intent.putExtra("user_name", name)
        intent.putExtra("user_pic", picture)
        resultChatCallback.launch(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }

    private fun uploadNewVideo() {
        Functions.makeDirectry(Functions.getAppFolder(context) + Variables.APP_HIDED_FOLDER)
        Functions.makeDirectry(Functions.getAppFolder(context) + Variables.DRAFT_APP_FOLDER)
        if (Functions.checkLoginUser(this@MainMenuActivity)) {
            val giftFragment = CreateContentF { bundle ->
                if (bundle != null) {
                }
            }
            giftFragment.show(supportFragmentManager, "")
        }
    }

    fun gotopostScreen() {
        val intent = Intent(this@MainMenuActivity, PostVideoA::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }


    // add the listener of home bth which will open the recording screen
    fun onHomeClick() {
        val tab1 = tabLayout!!.getTabAt(1)
        val view1 = tab1!!.customView
        val imageView1 = view1!!.findViewById<ImageView>(R.id.image)
        imageView1.setColorFilter(
            ContextCompat.getColor(context!!, R.color.unselected_tab),
            PorterDuff.Mode.SRC_IN
        )
        val tex1 = view1.findViewById<TextView>(R.id.text)
        tex1.setTextColor(ContextCompat.getColor(context!!, R.color.unselected_tab))
        tab1.customView = view1
        val tab2 = tabLayout!!.getTabAt(2)
        val view2 = tab2!!.customView
        val image = view2!!.findViewById<ImageView>(R.id.image)
//        image.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.add_video_tab_ic))
        view2.updateLayoutParams {
                (this as ViewGroup.MarginLayoutParams).bottomMargin =
                    resources.getDimension(R.dimen._9sdp).toInt()

        }
        tab2.customView = view2
        val tab3 = tabLayout!!.getTabAt(3)
        val view3 = tab3!!.customView
        val imageView3 = view3!!.findViewById<ImageView>(R.id.image)
        imageView3.setColorFilter(
            ContextCompat.getColor(context!!, R.color.unselected_tab),
            PorterDuff.Mode.SRC_IN
        )
        val tex3 = view3.findViewById<TextView>(R.id.text)
        tex3.setTextColor(ContextCompat.getColor(context!!, R.color.unselected_tab))
        tab3.customView = view3
        val tab4 = tabLayout!!.getTabAt(4)
        val view4 = tab4!!.customView
        val imageView4 = view4!!.findViewById<ImageView>(R.id.image)
        imageView4.setColorFilter(
            ContextCompat.getColor(context!!, R.color.unselected_tab),
            PorterDuff.Mode.SRC_IN
        )
        val tex4 = view4.findViewById<TextView>(R.id.text)
        tex4.setTextColor(ContextCompat.getColor(context!!, R.color.unselected_tab))
        tab4.customView = view4
//        tabLayout!!.background =
//            ContextCompat.getDrawable(context!!, R.drawable.d_top_gray_line_trans)
        window.navigationBarColor = ContextCompat.getColor(context!!, R.color.blackColor)
        window.decorView.systemUiVisibility = 0
    }

    // profile and notification tab click listener handler when user is not login into app
    fun onotherTabClick() {
        val tab1 = tabLayout!!.getTabAt(1)
        val view1 = tab1!!.customView
        val tex1 = view1!!.findViewById<TextView>(R.id.text)
        val imageView1 = view1.findViewById<ImageView>(R.id.image)
        imageView1.setColorFilter(
            ContextCompat.getColor(context!!, R.color.unselected_tab),
            PorterDuff.Mode.SRC_IN
        )
        tex1.setTextColor(ContextCompat.getColor(context!!, R.color.unselected_tab))
        tab1.customView = view1
        val tab2 = tabLayout!!.getTabAt(2)
        val view2 = tab2!!.customView
        val image = view2!!.findViewById<ImageView>(R.id.image)
//        image.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.add_video_tab_ic))
//        image.background =
//            ResourcesCompat.getDrawable(resources, R.drawable.light_under_add_video, theme)
//        image.layoutParams = image.layoutParams.apply {
//            height = resources.getDimension(R.dimen._32sdp).toInt()
//            width = resources.getDimension(R.dimen._32sdp).toInt()
//        }
        tab2.customView = view2
        val tab3 = tabLayout!!.getTabAt(3)
        val view3 = tab3!!.customView
        val imageView3 = view3!!.findViewById<ImageView>(R.id.image)
        imageView3.setColorFilter(
            ContextCompat.getColor(context!!, R.color.unselected_tab),
            PorterDuff.Mode.SRC_IN
        )
//        view3.updateLayoutParams {
//            (this as LinearLayout.LayoutParams).topMargin =
//                resources.getDimension(R.dimen._16sdp).toInt()
//        }
        val tex3 = view3.findViewById<TextView>(R.id.text)
        tex3.setTextColor(ContextCompat.getColor(context!!, R.color.unselected_tab))
        tab3.customView = view3
        val tab4 = tabLayout!!.getTabAt(4)
        val view4 = tab4!!.customView
        val imageView4 = view4!!.findViewById<ImageView>(R.id.image)
        imageView4.setColorFilter(
            ContextCompat.getColor(context!!, R.color.unselected_tab),
            PorterDuff.Mode.SRC_IN
        )
//        view4.updateLayoutParams {
//            (this as LinearLayout.LayoutParams).topMargin =
//                resources.getDimension(R.dimen._16sdp).toInt()
//        }
        val tex4 = view4.findViewById<TextView>(R.id.text)
        tex4.setTextColor(ContextCompat.getColor(context!!, R.color.unselected_tab))
        tab4.customView = view4
//        tabLayout!!.background = ContextCompat.getDrawable(context!!, R.drawable.ractengle_white)
        window.navigationBarColor = ContextCompat.getColor(context!!, R.color.white)
        if (DarkModePrefManager(this@MainMenuActivity).isNightMode) {
            window.decorView.systemUiVisibility = 0
        } else {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private val mPermissionResult: ActivityResultLauncher<Array<String>> =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(
                        Functions.getPermissionStatus(
                            this@MainMenuActivity,
                            key
                        )
                    )
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    context,
                    context!!.getString(R.string.we_need_storage_camera_recording_permission_for_make_new_video)
                )
            } else if (allPermissionClear) {
                uploadNewVideo()
            }
        }

    private fun addTabs() {
        val tabLayoutMediator = TabLayoutMediator(tabLayout!!, pager!!) { tab, position ->
            if (position == 0) {
                tab.text = context!!.getString(R.string.home)
            } else if (position == 1) {
                tab.text = context!!.getString(R.string.discover)
            } else if (position == 2) {
                tab.text = context!!.getString(R.string.upload)
            } else if (position == 3) {
                tab.text = context!!.getString(R.string.uni)
            } else if (position == 4) {
                tab.text = context!!.getString(R.string.me)
            }
        }
        tabLayoutMediator.attach()
    }

    private fun registerFragmentWithPager() {
        adapter!!.addFrag(HomeF.newInstance())
        adapter!!.addFrag(DiscoverF.newInstance())
        adapter!!.addFrag(BlankFragment.newInstance())
        adapter!!.addFrag(SpaceTabF.newInstance())
        adapter!!.addFrag(ProfileTabF.newInstance())
    }

    private val locationPermission: Unit
        private get() {
            val fragment = ShowLocationPermissionF.newInstance { bundle ->
                if (bundle.getBoolean("isShow")) {
                    takePermissionUtils!!.showLocationPermissionDailog(getString(R.string.we_need_location_permission_to_show_you_nearby_contents))
                }
            }
            fragment.show(supportFragmentManager, "ShowLocationPermissionF")
        }

    // Handle the case where the necessary services are not available
    private val loactionLatlng: Unit
        private get() {
            val locationTracker = LocationTracker(this)
            if (locationTracker.isGooglePlayServicesAvailable && locationTracker.isGPSEnabled) {
                val latitude = locationTracker.latitude
                val longitude = locationTracker.longitude
                Functions.getSharedPreference(this@MainMenuActivity).edit()
                    .putString(Variables.DEVICE_LAT, "" + latitude).commit()
                Functions.getSharedPreference(this@MainMenuActivity).edit()
                    .putString(Variables.DEVICE_LNG, "" + longitude).commit()
                locationTracker.stopUsingGPS()
            } else {
                Log.d(Constants.TAG_, "You Have no services")
                // Handle the case where the necessary services are not available
            }
        }
    private val mPermissionPostNotificationResult =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(
                        Functions.getPermissionStatus(
                            this@MainMenuActivity,
                            key
                        )
                    )
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    context,
                    context!!.getString(R.string.we_need_location_permission_to_show_you_nearby_contents)
                )
            } else if (allPermissionClear) {
            }
        }

    private val mPermissionLocationResult =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->

            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(
                        Functions.getPermissionStatus(
                            this@MainMenuActivity,
                            key
                        )
                    )
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    context,
                    context!!.getString(R.string.we_need_location_permission_to_show_you_nearby_contents)
                )
            } else if (allPermissionClear) {
                loactionLatlng
            }

        }


    var streamId = ""
    private fun chechDeepLink(intent: Intent?) {
        try {
            val uri = intent!!.data
            val linkUri = "" + uri
            var userId = ""
            var videoId = ""
            val profileURL =
                Variables.https + "://" + getString(R.string.share_profile_domain_second) + getString(
                    R.string.share_profile_endpoint_second
                )
            val streamURL =
                Variables.https + "://" + getString(R.string.share_profile_domain_second) + getString(
                    R.string.share_stream_endpoint_second
                )
            if (linkUri.contains(streamURL)) {
                val parts =
                    linkUri.split(streamURL.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                streamId = parts[1]
                streamingOpen()
            } else if (linkUri.contains(profileURL)) {
                val parts = linkUri.split(profileURL.toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                userId = parts[1]
                OpenProfileScreen(userId)
            } else if (linkUri.contains(getString(R.string.share_referal_code))) {
                Log.d(Constants.TAG_, "Link : $linkUri")
                val parts =
                    linkUri.split("code=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                userId = parts[1]
                OpenRegisterationScreen(userId)
            } else if (linkUri.contains(Constants.BASE_URL)) {
                val parts =
                    linkUri.split(Constants.BASE_URL.toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                videoId = parts[1].substring(4, parts[1].length - 3)
                openWatchVideo(videoId)
            }
        } catch (e: Exception) {
            Log.d(Constants.TAG_, "Exception Link : $e")
        }
    }

    private fun OpenRegisterationScreen(referalCode: String) {
        Functions.hideSoftKeyboard(this@MainMenuActivity)
        val intent = Intent(this@MainMenuActivity, LoginA::class.java)
        intent.putExtra("referalCode", referalCode)
        startActivity(intent)
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    private fun streamingOpen() {
        takePermissionUtils = PermissionUtils(this@MainMenuActivity, mPermissionStreamResult)
        if (takePermissionUtils!!.isCameraRecordingPermissionGranted) {
            goLive(streamId)
        } else {
            takePermissionUtils!!.showCameraRecordingPermissionDailog(getString(R.string.we_need_camera_and_recording_permission_for_live_streaming))
        }
    }

    private val mPermissionStreamResult =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            var allPermissionClear = true
            val blockPermissionCheck: MutableList<String> = ArrayList()
            for (key in result.keys) {
                if (!result[key]!!) {
                    allPermissionClear = false
                    blockPermissionCheck.add(
                        Functions.getPermissionStatus(
                            this@MainMenuActivity,
                            key
                        )
                    )
                }
            }
            if (blockPermissionCheck.contains("blocked")) {
                Functions.showPermissionSetting(
                    context,
                    context!!.getString(R.string.we_need_camera_and_recording_permission_for_live_streaming)
                )
            } else if (allPermissionClear) {
                goLive(streamId)
            }
        }

    private fun goLive(streamId: String) {
        Log.d(Constants.TAG_, "StreamingID: $streamId")
        rootRef!!.child("LiveStreamingUsers")
            .child(streamId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val selectLiveModel = snapshot.getValue(
                            LiveUserModel::class.java
                        )
                        if (selectLiveModel!!.getJoinStreamPrice() != null) {
                            if (selectLiveModel.getJoinStreamPrice()
                                    .equals("0", ignoreCase = true)
                            ) {
                                runOnUiThread { joinStream(selectLiveModel) }
                            } else {
                                rootRef!!.child("LiveStreamingUsers")
                                    .child(selectLiveModel.getStreamingId())
                                    .child("FeePaid").child(
                                        Functions.getSharedPreference(context)
                                            .getString(Variables.U_ID, "")!!
                                    ).addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            runOnUiThread {
                                                if (snapshot.exists()) {
                                                    joinStream(selectLiveModel)
                                                } else {
                                                    showPriceOffJoin(selectLiveModel)
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            runOnUiThread { showPriceOffJoin(selectLiveModel) }
                                        }
                                    })
                            }
                        } else {
                            Toast.makeText(
                                context,
                                context!!.getString(R.string.user) + " " + context!!.getString(R.string.is_offline_now),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                context,
                                context!!.getString(R.string.user) + " " + context!!.getString(R.string.is_offline_now),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun joinStream(selectLiveModel: LiveUserModel?) {
        Log.d(Constants.TAG_, "getOnlineType: " + selectLiveModel!!.getOnlineType())
        if (selectLiveModel.getOnlineType() == "single" || selectLiveModel.getOnlineType() == "oneTwoOne") {
            Functions.showLoader(this@MainMenuActivity, false, false)
            rootRef!!.child("LiveStreamingUsers").child(selectLiveModel.getStreamingId())
                .child("JoinStream")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val joinStreamMap = HashMap<String?, StreamJoinModel>()
                        for (postData in snapshot.children) {
                            val item = postData.getValue(
                                StreamJoinModel::class.java
                            )
                            if (item != null && item.getUserId() != null) {
                                joinStreamMap[item.getUserId()] = item
                            }
                        }
                        runOnUiThread {
                            Functions.cancelLoader()
                            if (snapshot.exists()) {
                                if (joinStreamMap.keys.size > 0) {
                                    if (joinStreamMap.keys.size == 1) {
                                        if (joinStreamMap.containsKey(
                                                Functions.getSharedPreference(
                                                    context
                                                ).getString(Variables.U_ID, "")
                                            )
                                        ) {
                                            goSingleLive(selectLiveModel)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                context!!.getString(R.string.streaming_already_join_by_an_other_user),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context!!.getString(R.string.streaming_already_join_by_an_other_user),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    goSingleLive(selectLiveModel)
                                }
                            } else {
                                goSingleLive(selectLiveModel)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        runOnUiThread { Functions.cancelLoader() }
                        Log.d(Constants.TAG_, "DatabaseError: $error")
                    }
                })
        } else if (selectLiveModel.getOnlineType() == "multicast") {
            val dataList = ArrayList<LiveUserModel?>()
            dataList.add(selectLiveModel)
            val intent = Intent()
            intent.putExtra("user_id", selectLiveModel.getUserId())
            intent.putExtra("user_name", selectLiveModel.getUserName())
            intent.putExtra("user_picture", selectLiveModel.getUserPicture())
            intent.putExtra("user_role", io.agora.rtc.Constants.CLIENT_ROLE_AUDIENCE)
            intent.putExtra("onlineType", "multicast")
            intent.putExtra("description", selectLiveModel.getDescription())
            intent.putExtra("secureCode", "")
            intent.putExtra("dataList", dataList)
            intent.putExtra("position", 0)
            intent.setClass(context!!, MultiViewLiveA::class.java)
            startActivity(intent)
        }
    }

    private fun showPriceOffJoin(selectLiveModel: LiveUserModel?) {
        val alertDialog = Dialog(context!!)
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog.setContentView(R.layout.price_to_join_stream_view)
        alertDialog.window!!.setBackgroundDrawable(context!!.getDrawable(R.drawable.d_round_white_background))
        val tabAccept = alertDialog.findViewById<RelativeLayout>(R.id.tabAccept)
        val closeBtn = alertDialog.findViewById<ImageView>(R.id.closeBtn)
        val tvJoiningAmount = alertDialog.findViewById<TextView>(R.id.tvJoiningAmount)
        tvJoiningAmount.text =
            "" + selectLiveModel!!.getJoinStreamPrice() + " " + context!!.getString(R.string.coins_are_deducted_from_your_wallet_to_join_the_stream)
        closeBtn.setOnClickListener { alertDialog.dismiss() }
        tabAccept.setOnClickListener {
            alertDialog.dismiss()
            deductPriceFromWallet(selectLiveModel)
        }
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun deductPriceFromWallet(selectLiveModel: LiveUserModel?) {
        val parameters = JSONObject()
        try {
            parameters.put(
                "user_id",
                Functions.getSharedPreference(context).getString(Variables.U_ID, "0")
            )
            parameters.put("live_streaming_id", selectLiveModel!!.getStreamingId())
            parameters.put("coin", selectLiveModel.getJoinStreamPrice())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Functions.showLoader(this@MainMenuActivity, false, false)
        VolleyRequest.JsonPostRequest(
            this@MainMenuActivity,
            ApiLinks.watchLiveStream,
            parameters,
            Functions.getHeaders(context)
        ) { resp ->
            Functions.checkStatus(this@MainMenuActivity, resp)
            Functions.cancelLoader()
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code == "200") {
                    val msgObj = jsonObject.getJSONObject("msg")
                    val userDetailModel = DataParsing.getUserDataModel(msgObj.getJSONObject("User"))
                    Functions.getSharedPreference(context).edit()
                        .putString(Variables.U_WALLET, "" + userDetailModel.wallet).commit()
                    val userId =
                        Functions.getSharedPreference(context).getString(Variables.U_ID, "")
                    val map = HashMap<String, String?>()
                    map["userId"] = userId
                    rootRef!!.child("LiveStreamingUsers").child(selectLiveModel!!.getStreamingId())
                        .child("FeePaid").child(userId!!)
                        .setValue(map)
                    joinStream(selectLiveModel)
                } else {
                    startActivity(Intent(context, MyWallet::class.java))
                }
            } catch (e: Exception) {
                Log.d(Constants.TAG_, "Exception : $e")
            }
        }
    }

    private fun goSingleLive(selectLiveModel: LiveUserModel?) {
        val intent = Intent(context, SingleCastJoinA::class.java)
        intent.putExtra("bookingId", selectLiveModel!!.getStreamingId())
        intent.putExtra("dataModel", selectLiveModel)
        intent.putExtra(
            com.uni.astro.activitesfragments.livestreaming.Constants.KEY_CLIENT_ROLE,
            io.agora.rtc.Constants.CLIENT_ROLE_AUDIENCE
        )
        val astro = this@MainMenuActivity.application as Astro
        astro.engineConfig().channelName = selectLiveModel.getStreamingId()
        startActivity(intent)
    }

    private fun openWatchVideo(videoId: String) {
        val intent = Intent(this@MainMenuActivity, WatchVideosA::class.java)
        intent.putExtra("video_id", videoId)
        intent.putExtra("position", 0)
        intent.putExtra("pageCount", 0)
        intent.putExtra(
            "userId",
            Functions.getSharedPreference(this@MainMenuActivity).getString(Variables.U_ID, "")
        )
        intent.putExtra("whereFrom", "IdVideo")
        startActivity(intent)
    }

    private fun OpenProfileScreen(userId: String) {
        val parameters = JSONObject()
        try {
            parameters.put("user_id", userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        VolleyRequest.JsonPostRequest(
            this@MainMenuActivity,
            ApiLinks.showUserDetail,
            parameters,
            Functions.getHeaders(this)
        ) { resp ->
            Functions.checkStatus(this@MainMenuActivity, resp)
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code == "200") {
                    val msg = jsonObject.optJSONObject("msg")
                    val userDetailModel = DataParsing.getUserDataModel(msg.optJSONObject("User"))
                    moveToProfile(
                        userDetailModel.id!!,
                        userDetailModel.username!!,
                        userDetailModel.getProfilePic()!!
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun moveToProfile(id: String, username: String, pic: String) {
        if (Functions.checkProfileOpenValidation(id)) {
            val intent = Intent(this@MainMenuActivity, ProfileA::class.java)
            intent.putExtra("user_id", id)
            intent.putExtra("user_name", username)
            intent.putExtra("user_pic", pic)
            startActivity(intent)
            overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        chechDeepLink(intent)
        if (intent != null) {
            val type = intent.getStringExtra("type")
            if (type != null && type.equals("message", ignoreCase = true)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    val chatIntent = Intent(this@MainMenuActivity, ChatA::class.java)
                    chatIntent.putExtra("user_id", intent.getStringExtra("user_id"))
                    chatIntent.putExtra("user_name", intent.getStringExtra("user_name"))
                    chatIntent.putExtra("user_pic", intent.getStringExtra("user_pic"))
                    resultChatCallback.launch(chatIntent)
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
                }, 2000)
            }
        }
    }

    var resultChatCallback = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data!!.getBooleanExtra("isShow", false)) {
            }
        }
    }

    val publicIP: Unit
        get() {
            VolleyRequest.JsonGetRequest(this, "https://api.ipify.org/?format=json") { s ->
                try {
                    val responce = JSONObject(s)
                    val ip = responce.optString("ip")
                    Functions.getSharedPreference(this@MainMenuActivity).edit()
                        .putString(Variables.DEVICE_IP, ip).commit()
                    if (Functions.getSharedPreference(this@MainMenuActivity)
                            .getString(Variables.DEVICE_TOKEN, "").equals("", ignoreCase = true)
                    ) {
                        addFirebaseToken()
                    } else {
                        Functions.addDeviceData(this@MainMenuActivity)
                    }
                } catch (e: Exception) {
                    Log.d(Constants.TAG_, "Exception : $e")
                }
            }
        }

    fun addFirebaseToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                Functions.getSharedPreference(this@MainMenuActivity).edit()
                    .putString(Variables.DEVICE_TOKEN, token).commit()
                Functions.addDeviceData(this@MainMenuActivity)
            })
    }

    override fun onBackPressed() {
        val count = this.supportFragmentManager.backStackEntryCount
        if (count == 0) {
            if (pager!!.currentItem != 0) {
                tabLayout!!.getTabAt(0)!!.select()
                return
            }
            mBackPressed = if (mBackPressed + 2000 > System.currentTimeMillis()) {
                super.onBackPressed()
                return
            } else {
                Functions.showToast(
                    baseContext,
                    getString(R.string.tap_to_exist)
                )
                System.currentTimeMillis()
            }
        } else {
            val frag = supportFragmentManager.fragments[supportFragmentManager.fragments.size - 1]
            if (frag != null) {
                val childCount = frag.childFragmentManager.backStackEntryCount
                if (childCount == 0) {
                    super.onBackPressed()
                } else {
                    frag.childFragmentManager.popBackStack()
                }
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        mPermissionResult?.unregister()
        removeListener()
        super.onDestroy()
    }

    @JvmField
    var roomManager: RoomManager? = null

    @JvmField
    var roomFirebaseManager: RoomFirebaseManager? = null
    var model: MainStreamingModel? = null
    var myUserModel: HomeUserModel? = null
    var reference: DatabaseReference? = null
    fun setRoomListerner() {
        reference = FirebaseDatabase.getInstance().reference
        roomManager = RoomManager.getInstance(this)
        roomFirebaseManager = RoomFirebaseManager.getInstance(this)
        roomFirebaseManager!!.setListerner1(object : RoomFirebaseListener {
            override fun createRoom(bundle: Bundle?) {}
            override fun JoinedRoom(bundle: Bundle?) {
                if (bundle != null) {
                    val roomId = bundle.getString("roomId")
                    Functions.printLog(Constants.TAG_, "JoinedRoom roomId$roomId")
                    if (!TextUtils.isEmpty(bundle.getString("roomId"))) {
                        roomManager!!.showRoomDetailAfterJoin(roomId)
                    }
                }
            }

            override fun onRoomLeave(bundle: Bundle?) {
                stopRoomService()
                Dialogs.closeInvitationCookieBar(this@MainMenuActivity)
                roomFirebaseManager!!.removeAllListener()
                binding.sheetBottomBar.visibility = View.GONE
            }

            override fun onRoomDelete(bundle: Bundle?) {
                stopRoomService()
                roomFirebaseManager!!.removeAllListener()
                binding.sheetBottomBar.visibility = View.GONE
            }

            override fun onRoomUpdate(bundle: Bundle?) {
                model = roomFirebaseManager!!.mainStreamingModel
                myUserModel = roomFirebaseManager!!.myUserModel
            }

            override fun onRoomUsersUpdate(bundle: Bundle?) {
                model = roomFirebaseManager!!.mainStreamingModel
                myUserModel = roomFirebaseManager!!.myUserModel
                if (roomFirebaseManager!!.speakersUserList.size > 0) {
                    val userModel = roomFirebaseManager!!.speakersUserList[0]
                    binding.ivJoinProfileOne.controller = Functions.frescoImageLoad(
                        this@MainMenuActivity,
                        Functions.getUserName(userModel?.getUserModel()),
                        userModel?.getUserModel()?.getProfilePic(),
                        binding.ivJoinProfileOne
                    )
                }

                if (roomFirebaseManager!!.speakersUserList.size > 1) {
                    binding.ivJoinProfileTwo.visibility = View.VISIBLE
                    val userModel = roomFirebaseManager!!.speakersUserList[1]
                    binding.ivJoinProfileTwo.controller = Functions.frescoImageLoad(
                        this@MainMenuActivity,
                        Functions.getUserName(userModel?.getUserModel()),
                        userModel?.getUserModel()?.getProfilePic(),
                        binding.ivJoinProfileTwo
                    )

                } else if (roomFirebaseManager!!.audienceUserList.size > 0) {
                    binding.ivJoinProfileTwo.visibility = View.VISIBLE
                    val userModel = roomFirebaseManager!!.audienceUserList[0]
                    binding.ivJoinProfileTwo.controller = Functions.frescoImageLoad(
                        this@MainMenuActivity,
                        Functions.getUserName(userModel?.getUserModel()),
                        userModel?.getUserModel()?.getProfilePic(),
                        binding.ivJoinProfileTwo
                    )

                } else {
                    binding.ivJoinProfileTwo.visibility = View.GONE
                }

                val totalCount =
                    roomFirebaseManager!!.speakersUserList.size + roomFirebaseManager!!.audienceUserList.size
                if (totalCount > 2) {
                    binding.tabJoinCount.visibility = View.VISIBLE
                    binding.tvJoinCount.text = "+" + (totalCount - 2)
                } else {
                    binding.tabJoinCount.visibility = View.GONE
                }
            }

            override fun onMyUserUpdate(bundle: Bundle?) {
                model = roomFirebaseManager!!.mainStreamingModel
                myUserModel = roomFirebaseManager!!.myUserModel
                if (myUserModel!!.getUserRoleType() == null) {
                    myUserModel!!.setUserRoleType("0")
                }
                if (myUserModel!!.getUserRoleType() == "1" || myUserModel!!.getUserRoleType() == "2") {
                    if (myUserModel!!.getMice() == "1") {
                        binding.ivMice.setImageDrawable(
                            ContextCompat.getDrawable(
                                binding.root.context,
                                R.drawable.ic_mice
                            )
                        )

                        if (RoomStreamService.streamingInstance != null) {
                            if (RoomStreamService.streamingInstance!!.ismAudioMuted()) {
                                RoomStreamService.streamingInstance!!.enableVoiceCall()
                            }
                        }

                    } else {
                        binding.ivMice.setImageDrawable(
                            ContextCompat.getDrawable(
                                binding.root.context,
                                R.drawable.ic_mice_mute
                            )
                        )

                        if (RoomStreamService.streamingInstance != null && !RoomStreamService.streamingInstance!!.ismAudioMuted()) RoomStreamService.streamingInstance!!.muteVoiceCall()
                    }
                    binding.tabMice.visibility = View.VISIBLE
                    binding.tabRaiseHand.visibility = View.GONE
                    binding.tabRiseHandUser.visibility = View.VISIBLE
                } else {
                    if (myUserModel!!.getRiseHand() == "1") {
                        binding.ivRaiseHand.setImageDrawable(
                            ContextCompat.getDrawable(
                                binding.root.context, R.drawable.ic_hand
                            )
                        )
                    } else {
                        binding.ivRaiseHand.setImageDrawable(
                            ContextCompat.getDrawable(
                                binding.root.context, R.drawable.ic_hand_black
                            )
                        )
                    }
                    if (RoomStreamService.streamingInstance != null && !RoomStreamService.streamingInstance!!.ismAudioMuted()) RoomStreamService.streamingInstance!!.muteVoiceCall()
                    binding.tabMice.visibility = View.GONE
                    binding.tabRiseHandUser.visibility = View.GONE
                }
                if (myUserModel!!.getUserRoleType() == "1") {
                    binding.tabRiseHandUser.visibility = View.VISIBLE
                }
            }

            override fun onSpeakInvitationReceived(bundle: Bundle) {
                if (bundle != null) {
                    val invitation = bundle.getSerializable("data") as InviteForSpeakModel?
                    if (invitation!!.getInvite() == "1") {
                        Dialogs.showInvitationDialog(
                            this@MainMenuActivity,
                            invitation.getUserName()
                        ) { bndl ->
                            if (bndl != null) {
                                roomFirebaseManager!!.removeInvitation()
                                val updateRise = HashMap<String, Any>()
                                updateRise["riseHand"] = "0"
                                reference!!.child(Variables.roomKey)
                                    .child(model!!.model.id).child(Variables.roomUsers)
                                    .child(
                                        Functions.getSharedPreference(context)
                                            .getString(Variables.U_ID, "")!!
                                    )
                                    .updateChildren(updateRise)
                                if (bndl.getBoolean("isShow")) {
                                    if (RoomStreamService.streamingInstance != null && RoomStreamService.streamingInstance!!.ismAudioMuted()) {
                                        RoomStreamService.streamingInstance!!.enableVoiceCall()
                                    }

                                    roomManager!!.speakerJoinRoomHitApi(
                                        Functions.getSharedPreference(
                                            context
                                        ).getString(Variables.U_ID, ""), model!!.model.id, "2"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            override fun onWaveUserUpdate(bundle: Bundle) {}
        })
        roomManager!!.addResponseListener(object : RoomApisListener {
            override fun roomInvitationsSended(bundle: Bundle) {
                Functions.printLog(Constants.TAG_, "roomInvitationsSended")
                if (bundle.getString("action") == "roomInvitationSended") {
                    Functions.showSuccess(
                        this@MainMenuActivity,
                        binding.root.context.getString(R.string.room_invitation_send_successfully)
                    )
                    roomManager!!.selectedInviteFriends = null
                }
            }

            override fun goAheadForRoomGenrate(bundle: Bundle) {
                if (bundle.getString("action") == "goAheadForRoomGenrate") {
                    if (roomManager!!.roomName != null && roomManager!!.privacyType != null) {
                        Log.d(Constants.TAG_, "roomName: " + roomManager!!.roomName)
                        roomManager!!.createRoomBYUserId()
                    } else {
                        Functions.showError(
                            this@MainMenuActivity,
                            binding.root.context.getString(R.string.something_went_wrong)
                        )
                    }
                }
            }

            override fun onRoomJoined(bundle: Bundle) {
                Functions.printLog(Constants.TAG_, "onRoomJoined")
                val myUserModel = bundle.getSerializable("model") as HomeUserModel?
                val roomID = bundle.getString("roomId")
                roomFirebaseManager!!.joinRoom(roomID, myUserModel)
            }

            override fun onRoomReJoin(bundle: Bundle) {
                Functions.printLog(Constants.TAG_, "onRoomReJoin")
                val myUserModel = bundle.getSerializable("model") as HomeUserModel?
                val roomID = bundle.getString("roomId")
                roomFirebaseManager!!.joinRoom(roomID, myUserModel)
                if (!TextUtils.isEmpty(bundle.getString("roomId"))) {
                    roomManager!!.showRoomDetailAfterJoin(roomID)
                }
            }

            override fun onRoomMemberUpdate(bundle: Bundle) {
                if (bundle != null) {
                    val homeUserModel = bundle.getSerializable("model") as HomeUserModel?
                    roomFirebaseManager!!.updateMemberModel(homeUserModel)
                }
            }

            override fun doRoomLeave(bundle: Bundle) {
                Functions.printLog(Constants.TAG_, "doRoomLeave")
                if (bundle.getString("action") == "leaveRoom") {
                    val roomId = bundle.getString("roomId")
                    roomManager!!.leaveRoom(roomId)
                }
            }

            override fun doRoomDelete(bundle: Bundle) {
                Functions.printLog(Constants.TAG_, "doRoomDelete")
                if (bundle.getString("action") == "deleteRoom") {
                    val roomId = bundle.getString("roomId")
                    roomManager!!.deleteRoom(roomId)
                }
            }

            override fun onRoomLeave(bundle: Bundle) {
                roomFirebaseManager!!.removeUserLeaveNode(model!!.model.id)
            }

            override fun onRoomDelete(bundle: Bundle) {
                roomFirebaseManager!!.removeRoomNode(model!!.model.id)
            }

            override fun goAheadForRoomJoin(bundle: Bundle) {
                Functions.printLog(Constants.TAG_, "goAheadForRoomJoin")
                if (bundle.getString("action") == "goAheadForJoinRoom") {
                    val roomId = bundle.getString("roomId")
                    Log.d(Constants.TAG_, "roomId: $roomId")
                    roomManager!!.joinRoom(
                        Functions.getSharedPreference(context).getString(Variables.U_ID, ""),
                        roomId,
                        "0"
                    )
                }
            }

            override fun roomCreated(bundle: Bundle) {
                Functions.printLog(Constants.TAG_, "roomCreated")
                if (bundle.getString("action") == "roomCreated") {
                    val model = bundle.getSerializable("model") as MainStreamingModel?
                    if (roomManager!!.selectedInviteFriends != null && roomManager!!.selectedInviteFriends.size > 0) {
                        roomManager!!.inviteMembersIntoRoom(
                            Functions.getSharedPreference(context).getString(Variables.U_ID, ""),
                            roomManager!!.selectedInviteFriends
                        )
                    }
                    roomManager!!.roomName = null
                    roomManager!!.privacyType = null
                    roomManager!!.selectedInviteFriends = null
                    roomFirebaseManager!!.createRoomNode(model)
                }
            }

            @SuppressLint("SuspiciousIndentation")
            override fun showRoomDetailAfterJoin(bundle: Bundle) {
                Functions.printLog(Constants.TAG_, "showRoomDetailAfterJoin()")
                if (bundle != null) {
                    model = bundle["model"] as MainStreamingModel?
                    roomFirebaseManager!!.mainStreamingModel = model
                    roomFirebaseManager!!.addAllRoomListerner()
                    startRoomService()
                    binding.sheetBottomBar.visibility = View.VISIBLE
                    Functions.printLog(Constants.TAG_, "showRoomDetailAfterJoin()")
                    if (tabLayout!!.selectedTabPosition == 3) openRoomScreen()
                }
            }
        })
        binding.tabRaiseHand.setOnClickListener { openRiseHandToSpeak() }
        binding.tabMice.setOnClickListener { updateMyMiceStatus() }
        binding.tabRiseHandUser.setOnClickListener { openRiseHandList() }
        binding.tabQueitly.setOnClickListener { removeRoom() }
        binding.sheetBottomBar.setOnClickListener { openRoomScreen() }
    }

    fun removeListener() {
        if (roomFirebaseManager != null) {
            roomFirebaseManager!!.removeMainListener()
        }
    }

    fun startRoomService() {
        val mService = RoomStreamService()
        if (!Functions.isMyServiceRunning(this@MainMenuActivity, mService.javaClass)) {
            val intent = Intent(applicationContext, mService.javaClass)
            var userModel: HomeUserModel? = null
            for (homeUserModel in roomFirebaseManager!!.mainStreamingModel.userList) {
                if (homeUserModel.getUserModel().id == Functions.getSharedPreference(context)
                        .getString(Variables.U_ID, "")
                ) {
                    userModel = homeUserModel
                }
            }
            if (userModel != null) {
                intent.putExtra(
                    "title",
                    "" + userModel.getUserModel().firstName + " " + userModel.getUserModel().lastName
                )
            } else {
                intent.putExtra("title", "")
            }
            intent.putExtra(
                "message",
                getString(R.string.connected_with_space) + " " + roomFirebaseManager!!.mainStreamingModel.model.title
            )
            intent.putExtra("roomId", roomFirebaseManager!!.mainStreamingModel.model.id)
            intent.putExtra(
                "userId",
                Functions.getSharedPreference(context).getString(Variables.U_ID, "")
            )
            intent.action = "start"
            ContextCompat.startForegroundService(applicationContext, intent)
        }
    }

    fun stopRoomService() {
        val mService = RoomStreamService()
        if (Functions.isMyServiceRunning(applicationContext, mService.javaClass)) {
            val intent = Intent(applicationContext, mService.javaClass)
            intent.action = "stop"
            ContextCompat.startForegroundService(applicationContext, intent)
        }
    }

    private fun openRoomScreen() {
        if (model != null) {
            val f = RoomDetailBottomSheet.newInstance(model) { }
            val transaction = supportFragmentManager.beginTransaction()
            val bundle = Bundle()
            f.arguments = bundle
            transaction.setCustomAnimations(
                R.anim.in_from_bottom,
                R.anim.out_to_top,
                R.anim.in_from_top,
                R.anim.out_from_bottom
            )
            transaction.addToBackStack("RoomDetailBottomSheet")
            transaction.replace(R.id.mainMenuFragment, f, "RoomDetailBottomSheet").commit()
        }
    }

    private fun removeRoom() {
        val bundle = roomManager!!.checkRoomCanDeleteOrLeave(roomFirebaseManager!!.speakersUserList)
        if (bundle.getString("action") == "removeRoom") {
            roomManager!!.deleteRoom(model!!.model.id)
        } else if (bundle.getString("action") == "leaveRoom") {
            roomManager!!.leaveRoom(model!!.model.id)
        } else {
            val speakerAsModeratorModel = bundle.getSerializable("model") as HomeUserModel?
            makeRoomModeratorAndLeave(speakerAsModeratorModel)
        }
    }

    private fun makeRoomModeratorAndLeave(itemUpdate: HomeUserModel?) {
        if (model!!.model != null) {
            reference!!.child(Variables.roomKey).child(model!!.model.id).child(Variables.roomUsers)
                .child(itemUpdate!!.getUserModel().id!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val dataItem = snapshot.getValue(
                                HomeUserModel::class.java
                            )
                            dataItem!!.setUserRoleType("1")
                            reference!!.child(Variables.roomKey).child(model!!.model.id)
                                .child(Variables.roomUsers)
                                .child(itemUpdate.getUserModel().id!!)
                                .setValue(dataItem).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        roomManager!!.leaveRoom(model!!.model.id)
                                    }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun openRiseHandToSpeak() {
        val riseHandForSpeakF =
            RiseHandForSpeakF { bundle ->
                if (bundle.getBoolean("isShow")) {
                    if (bundle.getString("action") == "riseHandForSpeak") {
                        val riseHandMap = HashMap<String, Any>()
                        riseHandMap["riseHand"] = "1"
                        reference!!.child(Variables.roomKey).child(model!!.model.id)
                            .child(Variables.roomUsers).child(
                                Functions.getSharedPreference(context)
                                    .getString(Variables.U_ID, "")!!
                            )
                            .updateChildren(riseHandMap)
                    } else if (bundle.getString("action") == "neverMind") {
                        val riseHandMap = HashMap<String, Any>()
                        riseHandMap["riseHand"] = "0"
                        reference!!.child(Variables.roomKey).child(model!!.model.id)
                            .child(Variables.roomUsers).child(
                                Functions.getSharedPreference(context)
                                    .getString(Variables.U_ID, "")!!
                            )
                            .updateChildren(riseHandMap)
                    }
                }
            }
        riseHandForSpeakF.show(supportFragmentManager, "RiseHandForSpeakF")
    }

    private fun updateMyMiceStatus() {
        if (RoomStreamService.streamingInstance != null) {
            val updateMice = HashMap<String, Any>()
            if (RoomStreamService.streamingInstance!!.ismAudioMuted()) {
                updateMice["mice"] = "1"
            } else {
                updateMice["mice"] = "0"
            }

            reference!!.child(Variables.roomKey).child(model!!.model.id)
                .child(Variables.roomUsers)
                .child(Functions.getSharedPreference(context).getString(Variables.U_ID, "")!!)
                .updateChildren(updateMice).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                    }
                }
        }
    }

    private fun openRiseHandList() {
        val fragment =
            RiseHandUsersF(
                model!!.model.id,
                roomFirebaseManager!!.mainStreamingModel.model.riseHandRule
            ) { bundle ->
                if (bundle.getBoolean("isShow")) {
                    if (bundle.getString("action") == "invite") {
                        val itemUpdate =
                            bundle.getSerializable("itemModel") as HomeUserModel?
                        sendInvitationForSpeak(itemUpdate!!.userModel)
                    }
                }
            }
        fragment.show(supportFragmentManager, "RiseHandUsersF")
    }

    private fun sendInvitationForSpeak(userModel: UserModel) {
        if (model != null) {
            val invitation = InviteForSpeakModel()
            invitation.setInvite("1")
            invitation.setUserId(
                Functions.getSharedPreference(context).getString(Variables.U_ID, "")
            )
            invitation.setUserName(
                Functions.getSharedPreference(context).getString(Variables.U_NAME, "")
            )
            reference!!.child(Variables.roomKey).child(model!!.model.id)
                .child(Variables.roomInvitation)
                .child(userModel.id!!)
                .setValue(invitation).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Functions.showSuccess(
                            this@MainMenuActivity,
                            binding.root.context.getString(R.string.great_we_are_sent_them_an_invite)
                        )
                    }
                }
        }
    }

    companion object {
        @JvmField
        var mainMenuActivity: MainMenuActivity? = null

        @JvmField
        var tabLayout: TabLayout? = null
        private val PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
    }
}