package com.codercamp.quran.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.codercamp.quran.R
import com.codercamp.quran.database.ApplicationData
import com.codercamp.quran.databinding.ActivityLanguageBinding
import com.codercamp.quran.theme.ApplicationTheme
import com.codercamp.quran.utils.ContextUtils
import com.facebook.ads.Ad
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class LanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageBinding
    private var facebookAdsView: AdView? = null
    private var facebookInterstitialAd: com.facebook.ads.InterstitialAd? = null
    private val TAG: String = LanguageActivity::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationTheme(this)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener { finish() }

        when (ApplicationData(this).language) {
            "en" -> binding.languageGroup.check(R.id.english)
            "bn" -> binding.languageGroup.check(R.id.bangla)
            "tr" -> binding.languageGroup.check(R.id.turkish)
        }

        binding.languageGroup.setOnCheckedChangeListener { group, checkedId ->
            ApplicationData(this).language =
                when (checkedId) {
                    R.id.english -> "en"
                    R.id.bangla -> "bn"
                    R.id.turkish -> "tr"
                    else -> "en"
                }
            recreate()
        }
        setResult(Activity.RESULT_OK, Intent())

        MobileAds.initialize(this) {}
        binding.adView.visibility = View.VISIBLE
        loadFacebookBannerAds()
        showFacebookInterstitialAd()
    }

    override fun attachBaseContext(newBase: Context?) {
        val localeToSwitchTo = Locale(ApplicationData(newBase!!).language)
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }


    override fun onPause() {

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {

        super.onDestroy();
    }

    fun loadFacebookBannerAds() {
        facebookAdsView = AdView(this, resources.getString(R.string.facebook_banner_ad_unit_id), AdSize.BANNER_HEIGHT_50)
        binding.adView.addView(facebookAdsView)
        facebookAdsView!!.loadAd()

    }

    private fun showFacebookInterstitialAd() {
        facebookInterstitialAd =
            com.facebook.ads.InterstitialAd(
                this,
                resources.getString(R.string.facebook_interstitial_id)
            )
        val interstitialAdListener: InterstitialAdListener = object : InterstitialAdListener {
            override fun onError(ad: Ad, adError: com.facebook.ads.AdError) {
                Log.e(TAG, "Interstitial ad failed to load: " + adError.errorMessage)
            }

            override fun onAdLoaded(ad: Ad) {
                facebookInterstitialAd!!.show()
            }

            override fun onAdClicked(ad: Ad) {
                Log.d(TAG, "Interstitial ad clicked!")
            }

            override fun onLoggingImpression(ad: Ad) {
                Log.d(TAG, "Interstitial ad impression logged!")
            }

            override fun onInterstitialDisplayed(ad: Ad) {
                Log.e(TAG, "Interstitial ad displayed.")
            }

            override fun onInterstitialDismissed(ad: Ad) {
                Log.e(TAG, "Interstitial ad dismissed.")
            }
        }
        facebookInterstitialAd!!.loadAd(
            facebookInterstitialAd!!.buildLoadAdConfig()
                .withAdListener(interstitialAdListener)
                .build()
        )
    }
}