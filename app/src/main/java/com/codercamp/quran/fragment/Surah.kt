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
import com.codercamp.quran.adapter.SurahListAdapter
import com.codercamp.quran.databinding.FragmentSurahBinding
import com.codercamp.quran.model.SurahList
import com.codercamp.quran.sql.SurahHelper
import com.facebook.ads.Ad
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.facebook.ads.*;
class Surah : Fragment(), ItemClickEvent {
    private var facebookAdsView: AdView? = null
    private var facebookInterstitialAd: com.facebook.ads.InterstitialAd? = null
    private val TAG: String = Surah::class.java.simpleName
    private val data = ArrayList<SurahList>()
    private var adapter: SurahListAdapter? = null
    private var binding: FragmentSurahBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSurahBinding.inflate(inflater, container, false)

        adapter = SurahListAdapter(requireContext(), data, this)

        binding?.surahRecycler?.layoutManager = LinearLayoutManager(requireContext())
        binding?.surahRecycler?.adapter = adapter

        loadData()
        AudienceNetworkAds.initialize(activity)
        MobileAds.initialize(requireContext()) {}
        //getAdsIsView()
        binding!!.adView.visibility = View.VISIBLE
        loadFacebookBannerAds()
        return binding?.root
    }

    private fun loadFacebookBannerAds() {
        facebookAdsView = AdView(
            activity, resources.getString(R.string.facebook_banner_ad_unit_id),
            AdSize.BANNER_HEIGHT_50
        )
        binding!!.adView.addView(facebookAdsView)
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

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {

        super.onDestroy();
    }

    override fun itemClick(position: Int) {
        // activity?.let { SurahActivity.launch(context = it, position, 0) }
        showFacebookInterstitialAd()
        Log.i("TAG", "onAdLoaded")
    }

}