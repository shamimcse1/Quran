package com.codercamp.quran.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.codercamp.quran.R
import com.codercamp.quran.application.Constant.Companion.EMAIL
import com.codercamp.quran.database.ApplicationData
import com.codercamp.quran.databinding.ActivitySettingsBinding
import com.codercamp.quran.external.TACPP
import com.codercamp.quran.theme.ApplicationTheme
import com.codercamp.quran.utils.ContextUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var applicationData: ApplicationData
    private lateinit var binding: ActivitySettingsBinding


    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when(result.resultCode) {
                RESULT_OK -> {
                    reCreate()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationTheme(this)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.clipToOutline = true
        binding.green.clipToOutline = true
        binding.blue.clipToOutline = true
        binding.orange.clipToOutline = true
        binding.openFont.clipToOutline = true

        MobileAds.initialize(this) {}
        getAdsIsView()

        binding.back.setOnClickListener { finish() }

        applicationData = ApplicationData(this)

        binding.language.setOnClickListener {
            startForResult.launch(Intent(this, LanguageActivity::class.java))
        }

        binding.openFont.setOnClickListener {
            if (binding.fontSizeExpandable.isExpanded) {
//                binding.openFont.animate()
//                    .rotation(0f)
//                    .start()
                binding.openFont.rotation = 0f
                binding.fontSizeExpandable.collapse()
            }
            else {
//                binding.openFont.animate()
//                    .rotation(180f)
//                    .start()
                binding.openFont.rotation = 180f
                binding.fontSizeExpandable.expand()
            }
        }

        binding.about.setOnClickListener {
            startActivity(
                Intent(
                    this, AboutActivity::class.java
                )
            )
        }

        binding.feedback.setOnClickListener {
            startActivity(
                Intent.createChooser(
                    Intent(
                        Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto",
                            EMAIL, null
                        )
                    ), "Send email...")
            )
        }

        binding.switchTheme.isChecked = applicationData.darkTheme
        binding.switchTheme.setOnCheckedChangeListener{ _, isChecked ->
            applicationData.darkTheme = isChecked
            reCreate()
        }

        //
        checkColor()
        initColorClick()
        //
        checkArabic()
        initArabicClick()
        //
        checkTranslation()
        initTranslationClick()
        //
        checkTransliteration()
        initTransliterationClick()
        //
        checkFontSeekBar()
        initFontSeekBarSeek()

    }

    private fun checkColor() {
        binding.greenCheck.visibility = View.GONE
        binding.blueCheck.visibility = View.GONE
        binding.orangeCheck.visibility = View.GONE
        when (applicationData.primaryColor) {
            ApplicationData.PURPLE -> binding.greenCheck.visibility = View.VISIBLE
            ApplicationData.BLUE -> binding.blueCheck.visibility = View.VISIBLE
            ApplicationData.ORANGE -> binding.orangeCheck.visibility = View.VISIBLE
        }
    }

    private fun initColorClick() {
        binding.green.setOnClickListener {
            applicationData.primaryColor = ApplicationData.PURPLE
            reCreate()
        }
        binding.blue.setOnClickListener {
            applicationData.primaryColor = ApplicationData.BLUE
            reCreate()
        }
        binding.orange.setOnClickListener {
            applicationData.primaryColor = ApplicationData.ORANGE
            reCreate()
        }
    }

    private fun checkArabic() {
        binding.arabicGroup.check(
            if (applicationData.arabic)
                R.id.uthmani else R.id.indoPk
        )
    }

    private fun initArabicClick() {
        binding.arabicGroup.setOnCheckedChangeListener { group, checkedId ->
            applicationData.arabic = checkedId != R.id.indoPk
        }
    }

    private fun checkTranslation() {
        binding.translationSwitch.isChecked = true
        binding.translationExpandable.expand(false)
        binding.translationGroup.check(
            when(applicationData.translation) {
                ApplicationData.TAISIRUL -> R.id.c_taisirul
                ApplicationData.MUHIUDDIN -> R.id.c_muhiuddin
                ApplicationData.ENGLISH -> R.id.c_eng
                else -> {
                    binding.translationExpandable.collapse(false)
                    binding.translationSwitch.isChecked = false
                    R.id.c_taisirul
                }
            }
        )
    }

    private fun initTranslationClick() {
        binding.translationGroup.setOnCheckedChangeListener { _, checkedId ->
            applicationData.translation = when(checkedId) {
                R.id.c_taisirul -> ApplicationData.TAISIRUL
                R.id.c_muhiuddin -> ApplicationData.MUHIUDDIN
                R.id.c_eng -> ApplicationData.ENGLISH
                else -> ApplicationData.OFF
            }
        }

        binding.translationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.translationGroup.check(R.id.c_muhiuddin)
                applicationData.translation = ApplicationData.MUHIUDDIN
                binding.translationExpandable.expand()
            } else {
                applicationData.translation = ApplicationData.OFF
                binding.translationExpandable.collapse()
            }
        }
    }

    private fun checkTransliteration() {
        binding.transliterationSwitch.isChecked = applicationData.transliteration
    }

    private fun initTransliterationClick() {
        binding.transliterationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            applicationData.transliteration = isChecked
        }
    }

    @SuppressLint("SetTextI18n")
    private fun checkFontSeekBar() {
        val arabic = applicationData.arabicFontSize.toInt()
        binding.arabicSizeText.text = "${arabic}sp"
        binding.arabicTranslationSeek.progress = (arabic-16)/2

        val transliteration = applicationData.transliterationFontSize.toInt()
        binding.transliterationSizeText.text = "${transliteration}sp"
        binding.transliterationFontSeek.progress = (transliteration-16)/2

        val translation = applicationData.translationFontSize.toInt()
        binding.translationSizeText.text = "${translation}sp"
        binding.translationSeek.progress = (translation-16)/2
    }

    private fun initFontSeekBarSeek() {
        binding.arabicTranslationSeek.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean) {
                    applicationData.arabicFontSize = ((progress*2)+16).toFloat()
                    binding.arabicSizeText.text = "${(progress*2)+16}sp"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    Log.e("onStartTrackingTouch", "onStartTrackingTouch")
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    Log.e("onStopTrackingTouch", "onStopTrackingTouch")
                }

            }
        )

        binding.transliterationFontSeek.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean) {
                    applicationData.transliterationFontSize = ((progress*2)+16).toFloat()
                    binding.transliterationSizeText.text = "${(progress*2)+16}sp"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    Log.e("Start", "Tracking")
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    Log.e("Stop", "Not Tracking")
                }

            }
        )

        binding.translationSeek.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean) {
                    applicationData.translationFontSize = ((progress*2)+16).toFloat()
                    binding.translationSizeText.text = "${(progress*2)+16}sp"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    Log.e("Start", "Tracking")
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    Log.e("Stop", "Not Tracking")
                }

            }
        )

        binding.termsCondition.setOnClickListener {
            TACPP(this).launch(1)
        }

        binding.policy.setOnClickListener {
            TACPP(this).launch(0)
        }
    }

    private fun reCreate() {
        setResult(Activity.RESULT_OK, Intent())
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
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

    override fun attachBaseContext(newBase: Context?) {
        val localeToSwitchTo = Locale(ApplicationData(newBase!!).language)
        val localeUpdatedContext: ContextWrapper = ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }

    private fun getAdsIsView() {

        val database = FirebaseDatabase.getInstance().reference.child("isAdsView")

        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.value == null) {
                    return
                }
                val database = dataSnapshot.value

                if (database != null) {
                    if (database as Boolean){
                        binding.adView.visibility =View.VISIBLE
                        loadAds()
                    }
                    else{
                        binding.adView.visibility =View.GONE
                    }
                }
                Log.e("lol", "onDataChange: " + database)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        database.addValueEventListener(listener)
    }
    private fun loadAds() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        binding.adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                val toastMessage: String = "ad fail to load"
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                val toastMessage: String = "ad loaded"

            }

            override fun onAdOpened() {
                super.onAdOpened()
                val toastMessage: String = "ad is open"

            }

            override fun onAdClicked() {
                super.onAdClicked()
                val toastMessage: String = "ad is clicked"
            }

            override fun onAdClosed() {
                super.onAdClosed()
                val toastMessage: String = "ad is closed"

            }

            override fun onAdImpression() {
                super.onAdImpression()
                val toastMessage: String = "ad impression"

            }
        }
    }

    override fun onPause() {
        binding.adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.adView.resume()
    }

    override fun onDestroy() {
        binding.adView.destroy()
        super.onDestroy();
    }
}