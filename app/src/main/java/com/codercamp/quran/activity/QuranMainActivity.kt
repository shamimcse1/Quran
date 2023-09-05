package com.codercamp.quran.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentPagerAdapter
import com.codercamp.quran.R
import com.codercamp.quran.adapter.PagerAdapter
import com.codercamp.quran.database.ApplicationData
import com.codercamp.quran.database.LastRead
import com.codercamp.quran.databinding.ActivityQuranMainBinding
import com.codercamp.quran.external.Search
import com.codercamp.quran.fragment.Bookmark
import com.codercamp.quran.fragment.Para
import com.codercamp.quran.fragment.Surah
import com.codercamp.quran.theme.ApplicationTheme
import com.codercamp.quran.utils.ContextUtils
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import java.text.NumberFormat
import java.util.*
import com.google.android.play.core.install.model.AppUpdateType
import com.prongbang.appupdate.AppUpdateInstallerListener
import com.prongbang.appupdate.AppUpdateInstallerManager
import com.prongbang.appupdate.InAppUpdateInstallerManager

class QuranMainActivity : AppCompatActivity() {

    private var lang = ""
    private var dark = false
    private var currentTheme = 0

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    if (dark != ApplicationData(this).darkTheme
                        || currentTheme != ApplicationData(this).primaryColor
                        || lang != ApplicationData(this).language
                    )
                        reCreate()
                }
            }
        }

    private var binding: ActivityQuranMainBinding? = null

    private val appUpdateInstallerManager: AppUpdateInstallerManager by lazy {
        InAppUpdateInstallerManager(this)
    }
    private fun popupSnackBarForCompleteUpdate() {
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            "An update has just been downloaded.",
            Snackbar.LENGTH_INDEFINITE
        )
        snackBar.setAction("RESTART") { appUpdateInstallerManager.completeUpdate() }
        snackBar.setActionTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        snackBar.show()
    }
    private val appUpdateInstallerListener by lazy {
        object : AppUpdateInstallerListener() {
            // On downloaded but not installed.
            override fun onDownloadedButNotInstalled() = popupSnackBarForCompleteUpdate()

            // On failure
            override fun onFailure(e: Exception)
            {

            }

            // On not update
            override fun onNotUpdate() {

            }

            // On cancelled update
            override fun onCancelled() {

            }
        }
    }
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationTheme(this)
        ApplicationData(this).run {
            dark = darkTheme
            lang = language
            currentTheme = primaryColor
        }
        binding = ActivityQuranMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.let {
            it.settings.clipToOutline = true
            it.headerCard.clipToOutline = true
            //it.back.setOnClickListener { finish() }

            it.quranPager.adapter = PagerAdapter(
                supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
            ).apply {
                addFragment(Surah())
                addFragment(Para())
                addFragment(Bookmark())
            }
            it.quranPager.offscreenPageLimit = 3
            it.tabLayout.tabGravity = TabLayout.GRAVITY_FILL

            it.quranPager.addOnPageChangeListener(
                TabLayout.TabLayoutOnPageChangeListener(binding?.tabLayout)
            )

            it.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {
                    /****/
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    /****/
                }

                override fun onTabSelected(tab: TabLayout.Tab) {
                    it.quranPager.currentItem = tab.position
                }
            })

            it.search.setOnClickListener {
                Search(this).searchSheet()
            }

            it.settings.setOnClickListener {
                startForResult.launch(
                    Intent(
                        this,
                        SettingsActivity::class.java
                    )
                )
            }
        }
        appUpdateInstallerManager.addAppUpdateListener(appUpdateInstallerListener)
        appUpdateInstallerManager.startCheckUpdate()
    }

    private fun reCreate() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(
                Intent(
                    this,
                    QuranMainActivity::class.java
                )
            )
            Objects.requireNonNull(
                overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )
            )
            finish()
        }, 150)
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        appUpdateInstallerManager.resumeCheckUpdate(AppUpdateType.FLEXIBLE)
        binding?.let {
            LastRead(this).let { last ->
                it.surahName.text =
                    resources.getStringArray(R.array.surah_name)[last.surahNo] //last.surahName
                it.ayahNo.text =
                    if (last.ayatNo == 0) "${resources.getString(R.string.ayat_no)}: " +
                            NumberFormat.getInstance(Locale(ApplicationData(this).language))
                                .format(1)
                    else "${resources.getString(R.string.ayat_no)}: " +
                            NumberFormat.getInstance(
                                Locale(
                                    ApplicationData(this)
                                        .language
                                )
                            ).format(last.ayatNo)
                it.headerCard.setOnClickListener { _ ->
                    SurahActivity.launch(this, last.surahNo, last.ayatNo)
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val localeToSwitchTo = Locale(ApplicationData(newBase!!).language)
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }

    override fun onBackPressed() {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Exit!!")
        builder.setMessage("Do you want to exit the app?")
        builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id ->
            finish()
            super.onBackPressed()
        })
        builder.setNegativeButton("No",
            DialogInterface.OnClickListener { dialog, id -> null })
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        appUpdateInstallerManager.onActivityResult(requestCode, resultCode, data)
    }
}