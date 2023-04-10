package com.onik.quran.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.onik.quran.`interface`.Bookmark
import com.onik.quran.adapter.BookmarkAdapter
import com.onik.quran.databinding.FragmentBookmarkBinding
import com.onik.quran.model.Quran
import com.onik.quran.sql.QuranHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Bookmark : Fragment() {

    private val data = ArrayList<Quran>()
    private var adapter: BookmarkAdapter? = null
    private var binding: FragmentBookmarkBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        binding = FragmentBookmarkBinding.inflate(inflater, container, false)

        adapter = BookmarkAdapter(requireContext(), data
            , object : Bookmark {
                override fun removed(pos: Int) {
                    data.removeAt(pos)
                    adapter?.notifyItemRemoved(pos)
                    adapter?.notifyItemRangeChanged(pos, data.size)
                    binding?.noBookmark?.visibility =
                        if (data.size > 0) View.GONE
                        else View.VISIBLE
                }
            })
        binding?.ayatRecycler?.layoutManager = LinearLayoutManager(requireContext())
        binding?.ayatRecycler?.adapter = adapter

        return binding?.root
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
    }
}