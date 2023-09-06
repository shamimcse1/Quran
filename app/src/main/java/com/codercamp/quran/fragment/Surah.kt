package com.codercamp.quran.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codercamp.quran.activity.SurahActivity
import com.codercamp.quran.adapter.ItemClickEvent
import com.codercamp.quran.adapter.SurahListAdapter
import com.codercamp.quran.constant.Para
import com.codercamp.quran.databinding.FragmentSurahBinding
import com.codercamp.quran.model.SurahList
import com.codercamp.quran.sql.SurahHelper
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Surah : Fragment(),ItemClickEvent {
    private var facebookAdsView: com.facebook.ads.AdView?= null
    private var facebookInterstitialAd: com.facebook.ads.InterstitialAd? = null
    private val TAG: String = Surah::class.java.simpleName
    private val data = ArrayList<SurahList>()
    private var adapter: SurahListAdapter? = null
    private var binding: FragmentSurahBinding? = null
    var interstitialAd: InterstitialAd? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = FragmentSurahBinding.inflate(inflater, container, false)

        adapter = SurahListAdapter(requireContext(), data,this)

        binding?.surahRecycler?.layoutManager = LinearLayoutManager(requireContext())
        binding?.surahRecycler?.adapter = adapter

        loadData()
        AudienceNetworkAds.initialize(activity)
        MobileAds.initialize(requireContext()) {}
        //getAdsIsView()
        binding!!.adView.visibility =View.VISIBLE
        loadAds()
        interstitialAd()
        return binding?.root
    }
    fun loadFacebookBannerAds(){
        facebookAdsView = AdView(activity, "1007569787153234_1007570497153163", AdSize.BANNER_HEIGHT_50)
        binding!!.bannerContainer.visibility = View.VISIBLE
        binding!!.bannerContainer.addView(facebookAdsView)
        facebookAdsView!!.loadAd()

    }

    private fun showFacebookInterstitialAd() {
        facebookInterstitialAd =
            com.facebook.ads.InterstitialAd(
                requireContext(),
                "1007569787153234_1007570607153152"
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
    private fun loadAds() {
        val adRequest = AdRequest.Builder().build()
        binding!!.adView.loadAd(adRequest)

        binding!!.adView.adListener = object : AdListener(){
            override fun onAdFailedToLoad(p0: LoadAdError) {
                binding!!.adView.visibility = View.GONE
                loadFacebookBannerAds()
                super.onAdFailedToLoad(p0)
            }

            override fun onAdLoaded() {
                binding!!.bannerContainer.visibility = View.GONE
                binding!!.adView.visibility = View.VISIBLE
                super.onAdLoaded()
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

    private fun loadData() {
        CoroutineScope(Dispatchers.Default).launch {
            data.clear()
            data.addAll(SurahHelper(requireContext()).readData())
            activity?.runOnUiThread { adapter?.notifyItemRangeChanged(0, data.size) }
        }
    }

    override fun onDetach() {
        super.onDetach()
        binding = null
    }

    override fun onPause() {
        binding!!.adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding!!.adView.resume()
    }

    override fun onDestroy() {
        binding!!.adView.destroy()
        if (facebookAdsView != null){
            facebookAdsView?.destroy()
        }
        super.onDestroy();
    }

    override fun itemClick(position: Int) {
       // activity?.let { SurahActivity.launch(context = it, position, 0) }
        showInterstitial()

    }
    private fun interstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            requireContext(),
            "ca-app-pub-1337577089653332/2717493562",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    this@Surah.interstitialAd = interstitialAd
                    Log.i("TAG", "onAdLoaded")
                    // Toast.makeText(BookViewActivity.this, "onAdLoaded()", Toast.LENGTH_SHORT).show();
                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                this@Surah.interstitialAd = null
                                Log.d("TAG", "The ad was dismissed.")
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                // Called when fullscreen content failed to show.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                showFacebookInterstitialAd()
                                this@Surah.interstitialAd = null
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
            interstitialAd!!.show(requireActivity())
        } else {
            //Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
        }
    }
}