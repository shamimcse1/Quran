package com.codercamp.quran.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codercamp.quran.R
import com.codercamp.quran.adapter.ItemClickEvent
import com.codercamp.quran.adapter.ParaListAdapter
import com.codercamp.quran.constant.Para
import com.codercamp.quran.databinding.FragmentParaBinding
import com.facebook.ads.Ad
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.MobileAds


class Para : Fragment(), ItemClickEvent {
    private var facebookAdsView: com.facebook.ads.AdView? = null
    private var facebookInterstitialAd: com.facebook.ads.InterstitialAd? = null
    private val TAG: String = Para::class.java.simpleName
    private var binding: FragmentParaBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentParaBinding.inflate(inflater, container, false)

        binding?.paraRecycler?.layoutManager = LinearLayoutManager(requireContext())
        binding?.paraRecycler?.adapter = ParaListAdapter(
            requireContext(), Para().Position(), this
        )
        AudienceNetworkAds.initialize(activity)
        MobileAds.initialize(requireContext()) {}
        //getAdsIsView()

        loadFacebookBannerAds()
        return binding?.root
    }

    private fun loadFacebookBannerAds() {
        facebookAdsView = AdView(
            activity,
            resources.getString(R.string.facebook_banner_ad_unit_id),
            AdSize.BANNER_HEIGHT_50
        )

        binding!!.adView.addView(facebookAdsView)
        facebookAdsView!!.loadAd()

    }

    private fun showFacebookInterstitialAd() {
        facebookInterstitialAd =
            com.facebook.ads.InterstitialAd(
                requireContext(),
                resources.getString(R.string.facebook_interstitial_id),
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

    override fun onDetach() {
        super.onDetach()
        binding = null
    }

    override fun onPause() {

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {

        super.onDestroy()
    }


    override fun itemClick(position: Int) {
        showFacebookInterstitialAd()
    }
}