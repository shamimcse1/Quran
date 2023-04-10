package com.onik.quran.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.onik.quran.adapter.ParaAyatAdapter
import com.onik.quran.constant.Name
import com.onik.quran.constant.Para
import com.onik.quran.databinding.FragmentParaAyatBinding
import com.onik.quran.model.ParaAyat
import com.onik.quran.model.Quran
import com.onik.quran.sql.QuranHelper
import com.onik.quran.sql.SurahHelper
import com.onik.quran.utils.KeyboardUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ParaAyat(private val position: Int) : Fragment() {

    private var search = ""
    private val data = ArrayList<ParaAyat>()
    private var surahHelper: SurahHelper? = null
    private var quranHelper: QuranHelper? = null
    private var adapterSurah: ParaAyatAdapter? = null
    private var binding: FragmentParaAyatBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = FragmentParaAyatBinding.inflate(inflater, container, false)

        initiate()

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

        return binding?.root
    }

    private fun initiate() {
        CoroutineScope(Dispatchers.Default).launch {
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

    override fun onDetach() {
        super.onDetach()
        binding = null
    }
}