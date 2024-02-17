package com.uni.astro.activitesfragments.videorecording

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uni.astro.Constants
import com.uni.astro.R
import com.uni.astro.activitesfragments.livestreaming.activities.ConcertSelectionA
import com.uni.astro.activitesfragments.spaces.services.RoomStreamService
import com.uni.astro.apiclasses.ApiLinks
import com.uni.astro.databinding.FragmentVideoCreationBinding
import com.uni.astro.interfaces.FragmentCallBack
import com.uni.astro.services.UploadService
import com.uni.astro.simpleclasses.Functions
import com.uni.astro.simpleclasses.Variables
import com.volley.plus.VPackages.VolleyRequest
import org.json.JSONObject

class CreateContentF(private var fragmentCallBack: FragmentCallBack?) : BottomSheetDialogFragment() {
    private lateinit var bind: FragmentVideoCreationBinding

    var streamingId = ""
    private var mBehavior: BottomSheetBehavior<*>? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(context!!, R.style.MyTransparentBottomSheetDialogTheme)
        bind = FragmentVideoCreationBinding.inflate(layoutInflater)

        dialog.setContentView(bind.root)
        mBehavior = BottomSheetBehavior.from(bind.root.parent as View)

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = FragmentVideoCreationBinding.inflate(inflater, container, false)

        bind.apply {
            btnPostVideo.setOnClickListener {
                val isOpenGLSupported = Functions.isOpenGLVersionSupported(context, 0x00030001)
                if (isOpenGLSupported) {
                    openVideoCamera()
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.your_device_opengl_verison_is_not_compatible_to_use_this_feature),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            btnGoLive.setOnClickListener {
                if (Functions.isMyServiceRunning(context, RoomStreamService().javaClass)) {
                    Functions.showAlert(
                        activity, getString(R.string.app_name), getString(R.string.creating_streaming_check)
                    )
                } else {
                    liveStreamingId()
                }
            }

            goBack.setOnClickListener {
                dismiss()
            }
        }

        return bind.root
    }

    private fun liveStreamingId() {
        val parameters = JSONObject()
        try {
            parameters.put(
                "user_id",
                Functions.getSharedPreference(requireContext()).getString(Variables.U_ID, "0")
            )
            parameters.put("started_at", Functions.getCurrentDate("yyyy-MM-dd HH:mm:ss"))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Functions.showLoader(activity, false, false)
        VolleyRequest.JsonPostRequest(activity, ApiLinks.liveStream, parameters, Functions.getHeaders(context)) { resp ->
            Functions.checkStatus(activity, resp)
            Functions.cancelLoader()
            try {
                val jsonObject = JSONObject(resp)
                val code = jsonObject.optString("code")
                if (code == "200") {
                    val msgObj = jsonObject.getJSONObject("msg")
                    val streamingObj = msgObj.getJSONObject("LiveStreaming")
                    streamingId = streamingObj.optString("id")
                    goLive()
                }
            } catch (e: Exception) {
                Log.d(Constants.TAG_, "Exception : $e")
            }
        }
    }

    private fun goLive() {
        dismiss()
        val intent = Intent(activity, ConcertSelectionA::class.java)
        intent.putExtra(
            "userId",
            Functions.getSharedPreference(context).getString(Variables.U_ID, "")
        )
        intent.putExtra(
            "userName",
            Functions.getSharedPreference(context).getString(Variables.U_NAME, "")
        )
        intent.putExtra(
            "userPicture",
            Functions.getSharedPreference(context).getString(Variables.U_PIC, "")
        )
        intent.putExtra("userRole", io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER)
        intent.putExtra("streamingId", streamingId)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun openVideoCamera() {
        if (Functions.isMyServiceRunning(context, UploadService().javaClass)) {
            Toast.makeText(
                context, getString(R.string.video_already_in_progress),
                Toast.LENGTH_SHORT
            ).show()

            Functions.showAlert(
                activity, getString(R.string.app_name), getString(R.string.video_already_in_progress)
            )

        } else if (Functions.isMyServiceRunning(context, RoomStreamService().javaClass)) {
            Functions.showAlert(
                activity, getString(R.string.app_name), getString(R.string.creating_post_check)
            )

        } else {
            dismiss()
            val intent = Intent(context, VideoRecoderA::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top)
        }
    }
}