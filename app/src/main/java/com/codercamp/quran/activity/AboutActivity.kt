package com.codercamp.quran.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.codercamp.quran.BuildConfig
import com.codercamp.quran.R
import com.codercamp.quran.application.Constant.Companion.EMAIL
import com.codercamp.quran.application.Constant.Companion.FACEBOOK
import com.codercamp.quran.application.Constant.Companion.FACEBOOK_WEB
import com.codercamp.quran.application.Constant.Companion.GITHUB
import com.codercamp.quran.application.Constant.Companion.INSTAGRAM
import com.codercamp.quran.application.Constant.Companion.INSTAGRAM_WEB
import com.codercamp.quran.application.Constant.Companion.PHONE
import com.codercamp.quran.application.Constant.Companion.TWITTER
import com.codercamp.quran.application.Constant.Companion.TWITTER_WEB
import com.codercamp.quran.database.ApplicationData
import com.codercamp.quran.databinding.ActivityAboutBinding
import com.codercamp.quran.theme.ApplicationTheme
import com.codercamp.quran.uiClass.CustomToast
import com.codercamp.quran.utils.ContextUtils
import com.facebook.ads.Ad
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class AboutActivity : AppCompatActivity() {

    private var facebookAdsView: com.facebook.ads.AdView?= null
    private var facebookInterstitialAd: com.facebook.ads.InterstitialAd? = null
    private val TAG: String = AboutActivity::class.java.simpleName

    var interstitialAd: InterstitialAd? = null
    private lateinit var binding: ActivityAboutBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationTheme(this)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.closeAbout.setOnClickListener { finish() }
        AudienceNetworkAds.initialize(this)
        binding.aboutVersion.text = "Version - ${BuildConfig.VERSION_NAME}"

        binding.aboutEmail.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto",
                    EMAIL, null
                )
            )
            startActivity(Intent.createChooser(intent, "Send email..."))
        }

        binding.aboutCall.setOnClickListener {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE), 9
            )
        }

        binding.aboutWhatsapp.setOnClickListener {
            try {
                packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
                val i = Intent(Intent.ACTION_VIEW)
                i.setPackage("com.whatsapp")
                i.data = Uri.parse("https://api.whatsapp.com/send?phone=$PHONE")
                startActivity(i)
            } catch (e: PackageManager.NameNotFoundException) {
                CustomToast(this).show(
                    "WhatsApp is not installed in your phone", CustomToast.TOAST_NEGATIVE
                )
            }
        }


        MobileAds.initialize(this) {}
        loadFacebookBannerAds()
        showFacebookInterstitialAd()
    }

    fun loadFacebookBannerAds(){
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$PHONE")
            startActivity(callIntent)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val localeToSwitchTo = Locale(ApplicationData(newBase!!).language)
        val localeUpdatedContext: ContextWrapper =
            ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}