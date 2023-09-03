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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Bookmark : Fragment(), ItemClickEvent {

    private val data = ArrayList<Quran>()
    private var adapter: BookmarkAdapter? = null
    private var binding: FragmentBookmarkBinding? = null

    private var facebookAdsView: AdView? = null
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

        AudienceNetworkAds.initialize(activity)
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


    @SuppressLint("NotifyDataSetChanged")
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

    }

    override fun onPause() {
        super.onPause()
    }


    override fun onDestroy() {
        if (facebookAdsView != null) {
            facebookAdsView!!.destroy()
        }
        super.onDestroy()
    }


    override fun itemClick(position: Int) {
        showFacebookInterstitialAd()
    }
}