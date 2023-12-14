package com.uni.astro.activitesfragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import cat.ereza.customactivityoncrash.CustomActivityOnCrash
import com.uni.astro.R
import com.uni.astro.databinding.ActivityCustomErrorBinding
import com.uni.astro.simpleclasses.AppCompatLocaleActivity


class CustomErrorActivity : AppCompatLocaleActivity() {
    private lateinit var bind: ActivityCustomErrorBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityCustomErrorBinding.inflate(layoutInflater)
        setContentView(bind.root)

        bind.apply {
            val config = CustomActivityOnCrash.getConfigFromIntent(intent)
            if (config == null) {
                finish()
                return
            }


            detailsBtn.setOnClickListener {
                showAlert()
            }

            sendReportBtn.setOnClickListener {

            }

            if (config.isShowRestartButton && config.restartActivityClass != null) {
                restartBtn.setText(R.string.restart_app)

                restartBtn.setOnClickListener {
                    startActivity(Intent(this@CustomErrorActivity, SplashA::class.java))
                    finish()
                }

            } else {
                restartBtn.setOnClickListener {
                    CustomActivityOnCrash.closeApplication(this@CustomErrorActivity, config)
                }
            }
        }

        showSendReport()
    }


    fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(R.string.customactivityoncrash_error_activity_error_details_title)
        dialog.setMessage(CustomActivityOnCrash.getAllErrorDetailsFromIntent(this@CustomErrorActivity, intent))
        dialog.setPositiveButton(R.string.customactivityoncrash_error_activity_error_details_close, null)
        dialog.setNeutralButton(R.string.customactivityoncrash_error_activity_error_details_copy) { _, _ -> copyErrorToClipboard() }
        dialog.show()
    }

    private fun copyErrorToClipboard() {
        val errorInformation = CustomActivityOnCrash.getAllErrorDetailsFromIntent(this@CustomErrorActivity, intent)
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        val clip = ClipData.newPlainText(getString(R.string.customactivityoncrash_error_activity_error_details_clipboard_label), errorInformation)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this@CustomErrorActivity, R.string.customactivityoncrash_error_activity_error_details_copied, Toast.LENGTH_SHORT).show()
    }

    private fun showSendReport() {
        val pkgName = applicationContext.packageName

        if (pkgName.contains("astro")) {
            bind.sendReportBtn.visibility = View.VISIBLE
        } else {
            bind.sendReportBtn.visibility = View.GONE
        }
    }
}