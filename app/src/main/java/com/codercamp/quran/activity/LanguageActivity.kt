package com.codercamp.quran.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codercamp.quran.R
import com.codercamp.quran.database.ApplicationData
import com.codercamp.quran.databinding.ActivityLanguageBinding
import com.codercamp.quran.theme.ApplicationTheme
import com.codercamp.quran.utils.ContextUtils
import java.util.*

class LanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationTheme(this)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener { finish() }

        when(ApplicationData(this).language) {
            "en" -> binding.languageGroup.check(R.id.english)
            "bn" -> binding.languageGroup.check(R.id.bangla)
            "tr" -> binding.languageGroup.check(R.id.turkish)
        }

        binding.languageGroup.setOnCheckedChangeListener { group, checkedId ->
            ApplicationData(this).language =
                when(checkedId) {
                    R.id.english -> "en"
                    R.id.bangla -> "bn"
                    R.id.turkish -> "tr"
                    else -> "en"
                }
            recreate()
        }
        setResult(Activity.RESULT_OK, Intent())
    }

    override fun attachBaseContext(newBase: Context?) {
        val localeToSwitchTo = Locale(ApplicationData(newBase!!).language)
        val localeUpdatedContext: ContextWrapper = ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }
}