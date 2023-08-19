package com.codercamp.quran.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.codercamp.quran.R
import com.codercamp.quran.adapter.ParaAyatAdapter
import com.codercamp.quran.adapter.SurahAyatAdapter
import com.codercamp.quran.application.Constant
import com.codercamp.quran.constant.Name
import com.codercamp.quran.constant.Para
import com.codercamp.quran.database.ApplicationData
import com.codercamp.quran.databinding.FragmentParaAyatBinding
import com.codercamp.quran.model.ParaAyat
import com.codercamp.quran.model.Quran
import com.codercamp.quran.sql.QuranHelper
import com.codercamp.quran.sql.SurahHelper
import com.codercamp.quran.utils.KeyboardUtils
import com.facebook.ads.Ad
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class ParaAyat(private val position: Int) : Fragment() {
    private var facebookAdsView: com.facebook.ads.AdView?= null
    private var facebookInterstitialAd: com.facebook.ads.InterstitialAd? = null
    private val TAG: String = ParaAyat::class.java.simpleName
    private var search = ""
    private val data = ArrayList<ParaAyat>()
    private var surahHelper: SurahHelper? = null
    private var quranHelper: QuranHelper? = null
    private var adapterSurah: ParaAyatAdapter? = null
    private var binding: FragmentParaAyatBinding? = null
    private lateinit var smoothScroller: RecyclerView.SmoothScroller
    private val data1 = ArrayList<Quran>()
    private var ayatFollower: BroadcastReceiver? = null
    private lateinit var layoutManager: LinearLayoutManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = FragmentParaAyatBinding.inflate(inflater, container, false)

        initiate()
        AudienceNetworkAds.initialize(activity)

        binding?.searchText?.setOnEditorActionListener(
            TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val key = binding?.searchText?.text.toString()
                search = if (key.isNotEmpty()) {
                    try {
                        filter(key.toInt())
                    } catch (ex: Exception) {
                        Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show()
                    }
                    key
                } else {
                    clearAll()
                    ""
                }
                closeKeyboard(binding?.searchText)
                return@OnEditorActionListener true
            }
            false
        })

        binding?.searchIcon?.setOnClickListener {
            searchIconClick()
        }
        //getAdsIsView()
        binding!!.adView.visibility =View.VISIBLE
        loadAds()
        return binding?.root
    }

    fun loadFacebookBannerAds(){
        facebookAdsView = AdView(activity, "1007569787153234_1007570497153163", AdSize.BANNER_HEIGHT_50)
        binding!!.bannerContainer.visibility = View.VISIBLE
        binding!!.bannerContainer.addView(facebookAdsView)
        facebookAdsView!!.loadAd()

    }

    private fun initiate() {
        layoutManager = LinearLayoutManager(requireContext())

        smoothScroller = object : LinearSmoothScroller(activity) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }

        CoroutineScope(Dispatchers.Default).launch {

            data1.add(Quran(0, 0, 0, "", "",
                "", "", "", "", "")
            )
            data1.addAll(QuranHelper(requireContext()).readSurahNo(position + 1))
            val temp = SurahHelper(requireContext()).readData()[position]
            val t = "${revelation(temp.revelation)}   |   ${
                NumberFormat.getInstance(
                    Locale(ApplicationData(requireContext()).language)
                ).format(temp.verse)}" +
                    "  " + resources.getString(R.string.verses)



            surahHelper = SurahHelper(requireContext())
            quranHelper = QuranHelper(requireContext())
            QuranHelper(requireContext()).readAyatXtoY(
                Para().Position()[position-1].startPos
                , Para().Position()[position-1].endPos
            ).forEach {

                if (it.ayat == 1)
                    data.add(modelExchange(it, true))
                data.add(modelExchange(it, false))
            }
            adapterSurah = ParaAyatAdapter(
                requireContext(), data
            )
            activity?.runOnUiThread {
                binding?.ayatRecycler?.layoutManager = LinearLayoutManager(requireContext())
                binding?.ayatRecycler?.adapter = adapterSurah
            }
        }
    }
    private fun revelation(revelation: String): String {
        return if (revelation == "Meccan")
            resources.getString(R.string.meccan)
        else resources.getString(R.string.medinan)
    }
    private fun clearAll() {
        data.clear()
        adapterSurah?.notifyDataSetChanged()
        CoroutineScope(Dispatchers.Default).launch {
            QuranHelper(requireContext()).readAyatXtoY(
                Para().Position()[position - 1].startPos,
                Para().Position()[position - 1].endPos
            ).forEach {
                if (it.ayat == 1)
                    data.add(modelExchange(it, true))
                data.add(modelExchange(it, false))
            }
            activity?.runOnUiThread {
                adapterSurah?.notifyDataSetChanged()
            }
        }
    }

    private fun searchIconClick() {
        binding?.searchText?.text.toString().let { e->
            search = if (e.isNotEmpty()) {
                try {
                    filter(e.toInt())
                } catch (ex: Exception) {
                    Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show()
                }
                e
            } else {
                data.clear()
                adapterSurah?.notifyDataSetChanged()
                CoroutineScope(Dispatchers.Default).launch {
                    QuranHelper(requireContext()).readAyatXtoY(
                        Para().Position()[position - 1].startPos,
                        Para().Position()[position - 1].endPos
                    ).forEach {
                        if (it.ayat == 1)
                            data.add(modelExchange(it, true))
                        data.add(modelExchange(it, false))
                    }
                    activity?.runOnUiThread {
                        adapterSurah?.notifyDataSetChanged()
                    }
                }
                ""
            }
        }
        closeKeyboard(binding?.searchText)
    }

    private fun filter(pos: Int) {
        data.clear()
        adapterSurah?.notifyDataSetChanged()
        QuranHelper(requireContext()).readAyatXtoY(
            Para().Position()[position-1].startPos
            , Para().Position()[position-1].endPos
        ).forEach {
            if (it.ayat == 1)
                data.add(modelExchange(it, true))
            data.add(modelExchange(it, false))
        }
        adapterSurah?.notifyDataSetChanged()
        if (pos < data.size) {
            binding?.ayatRecycler?.scrollToPosition(pos)
        }
    }

    private fun closeKeyboard(edit: EditText?) {
        edit?.let {
            it.clearFocus()
            KeyboardUtils.hideKeyboard(it)
        }
    }

    private fun modelExchange(temp: Quran, name: Boolean): ParaAyat {
        if (name) {
            val s = surahHelper!!.readDataAt(quranHelper!!.readAyatNo(temp.pos)!!.surah)
            return ParaAyat(
                type = 1,
                pos = temp.pos,
                surah = temp.surah,
                ayat = temp.ayat,
                indopak = temp.indopak,
                utsmani = temp.utsmani,
                jalalayn = temp.jalalayn,
                latin = temp.latin,
                terjemahan = temp.terjemahan,
                englishPro = temp.englishPro,
                englishT = temp.englishT,
                name = s!!.name,
                meaning = Name().data()[temp.surah-1],
                details = "${s.revelation}   |   ${s.verse} VERSES"
            )
        } else {
            return ParaAyat(
                type = 0,
                pos = temp.pos,
                surah = temp.surah,
                ayat = temp.ayat,
                indopak = temp.indopak,
                utsmani = temp.utsmani,
                jalalayn = temp.jalalayn,
                latin = temp.latin,
                terjemahan = temp.terjemahan,
                englishPro = temp.englishPro,
                englishT = temp.englishT,
                name = "",
                meaning = "",
                details = ""
            )
        }
    }

    private fun loadAds() {
        val adRequest = AdRequest.Builder().build()
        binding!!.adView.loadAd(adRequest)

        binding!!.adView.adListener = object : AdListener(){
            override fun onAdFailedToLoad(p0: LoadAdError) {
                loadFacebookBannerAds()
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
        binding!!.adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding!!.adView.resume()
    }
}