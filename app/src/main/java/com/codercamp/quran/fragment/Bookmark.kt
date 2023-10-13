package com.codercamp.quran.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codercamp.quran.R
import com.codercamp.quran.adapter.BookmarkAdapter
import com.codercamp.quran.adapter.ItemClickEvent
import com.codercamp.quran.databinding.FragmentBookmarkBinding
import com.codercamp.quran.`interface`.Bookmark
import com.codercamp.quran.model.Quran
import com.codercamp.quran.sql.QuranHelper
import com.facebook.ads.Ad
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Bookmark : Fragment(), ItemClickEvent {

    private val data = ArrayList<Quran>()
    private var adapter: BookmarkAdapter? = null
    private var binding: FragmentBookmarkBinding? = null
    var interstitialAd: InterstitialAd? = null
    private var facebookAdsView: com.facebook.ads.AdView?= null
    private var facebookInterstitialAd: com.facebook.ads.InterstitialAd? = null
    private val TAG: String = Bookmark::class.java.simpleName
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookmarkBinding.inflate(inflater, container, false)

        adapter = BookmarkAdapter(requireContext(), data, object : Bookmark {
            override fun removed(pos: Int) {
                data.removeAt(pos)
                adapter?.notifyItemRemoved(pos)
                adapter?.notifyItemRangeChanged(pos, data.size)
                binding?.noBookmark?.visibility =
                    if (data.size > 0) View.GONE
                    else View.VISIBLE
            }
        }, this)
        binding?.ayatRecycler?.layoutManager = LinearLayoutManager(requireContext())
        binding?.ayatRecycler?.adapter = adapter
        //getAdsIsView()
        binding!!.adView.visibility = View.VISIBLE
        AudienceNetworkAds.initialize(activity)
        loadAds()
        interstitialAd()
        return binding?.root
    }

    fun loadFacebookBannerAds(){
        facebookAdsView = AdView(activity, resources.getString(R.string.facebook_banner), AdSize.BANNER_HEIGHT_50)
        binding!!.bannerContainer.visibility = View.VISIBLE
        binding!!.bannerContainer.addView(facebookAdsView)
        facebookAdsView!!.loadAd()

    }

    private fun showFacebookInterstitialAd() {
        facebookInterstitialAd =
            com.facebook.ads.InterstitialAd(
                requireContext(),
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

    private fun loadAds() {
        val adRequest = AdRequest.Builder().build()
        binding!!.adView.loadAd(adRequest)

        binding!!.adView.adListener = object : AdListener() {
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

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Default).launch {
            data.clear()
            data.addAll(QuranHelper(requireContext()).readBookmark())
            activity?.runOnUiThread {
                binding?.noBookmark?.visibility =
                    if (data.size > 0) View.GONE
                    else View.VISIBLE
                adapter?.notifyDataSetChanged()
            }
        }
        binding!!.adView.resume()
    }

    override fun onPause() {
        binding!!.adView.pause()
        super.onPause()
    }


    override fun onDestroy() {
        binding!!.adView.destroy()
        if (facebookAdsView != null){
            facebookAdsView!!.destroy()
        }
        super.onDestroy()
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
                    this@Bookmark.interstitialAd = interstitialAd
                    Log.i("TAG", "onAdLoaded")
                    // Toast.makeText(BookViewActivity.this, "onAdLoaded()", Toast.LENGTH_SHORT).show();
                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                this@Bookmark.interstitialAd = null
                                Log.d("TAG", "The ad was dismissed.")
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                // Called when fullscreen content failed to show.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                showFacebookInterstitialAd()
                                this@Bookmark.interstitialAd = null
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

    override fun itemClick(position: Int) {
        showInterstitial()
    }
}