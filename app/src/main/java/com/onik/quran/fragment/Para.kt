package com.onik.quran.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.onik.quran.adapter.ParaListAdapter
import com.onik.quran.constant.Para
import com.onik.quran.databinding.FragmentParaBinding

class Para : Fragment() {

    private var binding: FragmentParaBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = FragmentParaBinding.inflate(inflater, container, false)

        binding?.paraRecycler?.layoutManager = LinearLayoutManager(requireContext())
        binding?.paraRecycler?.adapter = ParaListAdapter(
            requireContext(), Para().Position()
        )
        MobileAds.initialize(requireContext()) {}
        loadAds()
        return binding?.root
    }
    private fun loadAds() {
        val adRequest = AdRequest.Builder().build()
        binding!!.adView.loadAd(adRequest)

        binding!!.adView.adListener = object : AdListener(){
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
    override fun onDetach() {
        super.onDetach()
        binding = null
    }

    override fun onPause() {
        if (binding!!.adView!=null) {
            binding!!.adView.pause();
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (binding!!.adView != null) {
            binding!!.adView.resume();
        }
    }

    override fun onDestroy() {
        if (binding!!.adView != null) {
            binding!!.adView.destroy();
        }
        super.onDestroy();
    }
}