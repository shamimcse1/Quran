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
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class AboutActivity : AppCompatActivity() {
    var interstitialAd: InterstitialAd? = null
    private lateinit var binding: ActivityAboutBinding
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationTheme(this)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.closeAbout.setOnClickListener { finish() }

        binding.aboutVersion.text = "Version - ${BuildConfig.VERSION_NAME}"

//        binding.aboutFacebook.setOnClickListener {
//            try {
//                packageManager.getPackageInfo("com.facebook.katana", 0)
//                val i = Intent(Intent.ACTION_VIEW, Uri.parse(FACEBOOK))
//                i.setPackage("com.facebook.katana")
//                startActivity(i)
//            } catch (e: java.lang.Exception) {
//                startActivity(
//                    Intent(
//                        Intent.ACTION_VIEW, Uri.parse(FACEBOOK_WEB)
//                    )
//                )
//            }
//        }
//
//        binding.aboutInstagram.setOnClickListener {
//            try {
//                packageManager.getPackageInfo("com.instagram.android", 0)
//                val i = Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM))
//                i.setPackage("com.instagram.android")
//                startActivity(i)
//            } catch (e: java.lang.Exception) {
//                startActivity(
//                    Intent(
//                        Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_WEB)
//                    )
//                )
//                Log.println(Log.ASSERT, "error", e.toString())
//            }
//        }
//
//        binding.aboutTwitter.setOnClickListener {
//            try {
//                packageManager.getPackageInfo("com.twitter.android", 0)
//                val i = Intent(Intent.ACTION_VIEW, Uri.parse(TWITTER))
//                i.setPackage("com.twitter.android")
//                startActivity(i)
//            } catch (e: Exception) {
//                startActivity(
//                    Intent(
//                        Intent.ACTION_VIEW, Uri.parse(TWITTER_WEB)
//                    )
//                )
//                Log.e("error", "$e")
//            }
//        }
//
//        binding.aboutGithub.setOnClickListener {
//            try {
//                val browserIntent = Intent(
//                    Intent.ACTION_VIEW, Uri.parse(GITHUB)
//                )
//                startActivity(browserIntent)
//            } catch (e: Exception) {
//                Log.e("error", "$e")
//            }
//        }
//
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
        getAdsIsView()
    }

    private fun getAdsIsView() {
        binding.adView.visibility =View.VISIBLE
        loadAds()
        interstitialAd()
        val database = FirebaseDatabase.getInstance().reference.child("isAdsView")

        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.value == null) {
                    return
                }
                val database = dataSnapshot.value

                if (database != null) {
                    if (database as Boolean){

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
    private fun interstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
           this,
            "ca-app-pub-1337577089653332/2717493562",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    this@AboutActivity.interstitialAd = interstitialAd
                    Log.i("TAG", "onAdLoaded")
                    // Toast.makeText(BookViewActivity.this, "onAdLoaded()", Toast.LENGTH_SHORT).show();
                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                this@AboutActivity.interstitialAd = null
                                Log.d("TAG", "The ad was dismissed.")
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                // Called when fullscreen content failed to show.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                this@AboutActivity.interstitialAd = null
                                Log.d("TAG", "The ad failed to show.")
                            }

                            override fun onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                Log.d("TAG", "The ad was shown.")
                            }
                        }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Log.i("TAG", loadAdError.message)
                    interstitialAd = null
                    @SuppressLint("DefaultLocale") val error = String.format(
                        "domain: %s, code: %d, message: %s",
                        loadAdError.domain,
                        loadAdError.code,
                        loadAdError.message
                    )
                    Log.d("Error", error)
                    // Toast.makeText(BookViewActivity.this, "onAdFailedToLoad() with error: " + error, Toast.LENGTH_SHORT).show();
                }
            })
    }

    private fun showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (interstitialAd != null) {
            interstitialAd!!.show(this)
        } else {
            //Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
        }
    }

    override fun onBackPressed() {
        showInterstitial()
        super.onBackPressed()
    }
}