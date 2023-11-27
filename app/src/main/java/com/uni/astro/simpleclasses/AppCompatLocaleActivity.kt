package com.uni.astro.simpleclasses

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.uni.astro.R
import java.util.Arrays
import java.util.Locale

open class AppCompatLocaleActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val languageArray = newBase.resources.getStringArray(R.array.app_language_code)
        val languageCode = listOf(*languageArray)
        val language = Functions.getSharedPreference(newBase)
            .getString(Variables.APP_LANGUAGE_CODE, Variables.DEFAULT_LANGUAGE_CODE)

        if (languageCode.contains(language)) {
            val newLocale = Locale(language)
            super.attachBaseContext(ContextWrapper.wrap(newBase, newLocale))
        } else {
            super.attachBaseContext(newBase)
        }
    }
}