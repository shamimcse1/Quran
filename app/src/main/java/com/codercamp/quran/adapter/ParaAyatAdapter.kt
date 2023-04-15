package com.codercamp.quran.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.codercamp.quran.R
import com.codercamp.quran.database.ApplicationData
import com.codercamp.quran.model.ParaAyat
import com.codercamp.quran.model.Quran
import com.codercamp.quran.process.AudioProcess
import com.codercamp.quran.sql.QuranHelper
import com.codercamp.quran.sql.SurahHelper
import java.text.NumberFormat
import java.util.*

class ParaAyatAdapter(val context: Context, val data: ArrayList<ParaAyat>)
    : RecyclerView.Adapter<ParaAyatAdapter.ViewHolder>() {
    private var reading = -1
    companion object {
        const val BISMILLAH = 1
        const val DEFAULT = 0
    }

    private val quran = QuranHelper(context)

    inner class ViewHolder(view: View,  val type: Int): RecyclerView.ViewHolder(view) {
        var name: TextView? = null
        var details: TextView? = null
        var meaning: TextView? = null

        var pron: TextView? = null
        var play: ImageView? = null
        var share: ImageView? = null
        var arabic: TextView? = null
        var ayatNo: TextView? = null
        var surahName: TextView? = null
        var bookmark: ImageView? = null
        var translation: TextView? = null

        init {
            if (type == DEFAULT) {
                pron = view.findViewById(R.id.pron)
                play = view.findViewById(R.id.play)
                share = view.findViewById(R.id.share)
                arabic = view.findViewById(R.id.arabic)
                ayatNo = view.findViewById(R.id.ayat_no)
                bookmark = view.findViewById(R.id.bookmark)
                surahName = view.findViewById(R.id.surah_name)
                translation = view.findViewById(R.id.translation)

                play!!.clipToOutline = true
                share!!.clipToOutline = true
                bookmark!!.clipToOutline = true
            } else {
                name = view.findViewById(R.id.name)
                details = view.findViewById(R.id.details)
                meaning = view.findViewById(R.id.meaning)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ParaAyatAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context)
                .inflate(
                    if (viewType == BISMILLAH)
                        R.layout.layout_ayat_header
                    else R.layout.layout_ayat,
                    parent, false
                ), viewType
        )
    }

    override fun onBindViewHolder(holder: ParaAyatAdapter.ViewHolder, position: Int) {
            data[position].let {
                if (data[position].type == DEFAULT) {
                    holder.run {
                        ayatNo?.text = numberFormat.format(it.ayat)
                        arabic?.text = it.indopak
                        pron?.text = it.latin
//            holder.translation?.text = data[position].englishT
//                    translation?.text = it.terjemahan
                        surahName?.text = SurahHelper(context).readDataAt(it.surah)?.name
                        translation?.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Html.fromHtml(it.terjemahan, Html.FROM_HTML_MODE_COMPACT)
                        } else Html.fromHtml(it.terjemahan)

                        maintainClicks(play,share, bookmark, it)
                    }
                } else {
                    holder.name?.text = it.name
                    holder.details?.text = it.details
                    holder.meaning?.text = it.meaning
                }
            }
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].type
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun modelExchange(temp: ParaAyat): Quran {
        return Quran(
            pos = temp.pos,
            surah = temp.surah,
            ayat = temp.ayat,
            indopak = temp.indopak,
            utsmani = temp.utsmani,
            jalalayn = temp.jalalayn,
            latin = temp.latin,
            terjemahan = temp.terjemahan,
            englishPro = temp.englishPro,
            englishT = temp.englishT
        )
    }

    private fun maintainClicks(play: ImageView?,share: ImageView?, bookmark: ImageView?, it: ParaAyat) {
        share?.setOnClickListener { v ->
            var text = "Surah-> ${SurahHelper(context).readDataAt(it.surah)!!.name}," +
                    " Ayat-> ${it.ayat}\n\n"
            text += if (ApplicationData(context).arabic) it.utsmani else it.indopak
            text += "\n\nঅর্থ :  "+when(ApplicationData(context).translation) {
                ApplicationData.TAISIRUL -> it.terjemahan
                ApplicationData.MUHIUDDIN -> it.jalalayn
                ApplicationData.ENGLISH -> it.englishT
                else -> ""
            }
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, text)
            sendIntent.type = "text/plain"
            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)
        }

        bookmark?.setImageDrawable(
            ResourcesCompat.getDrawable(
                context.resources,
                if (quran.readAyatNo(it.pos)!!.englishPro == "T")
                    R.drawable.ic_baseline_bookmark_24
                else R.drawable.ic_baseline_bookmark_border_24,
                null
            )
        )

        bookmark?.setOnClickListener { _ ->
            if (quran.readAyatNo(it.pos)!!.englishPro == "T") {
                quran.insertData(modelExchange(it), "F")
            } else quran.insertData(modelExchange(it), "T")
            bookmark.setImageDrawable(
                ResourcesCompat.getDrawable(
                    context.resources,
                    if (quran.readAyatNo(it.pos)!!.englishPro == "T")
                        R.drawable.ic_baseline_bookmark_24
                    else R.drawable.ic_baseline_bookmark_border_24,
                    null
                )
            )
        }
        play?.setOnClickListener { _->
            AudioProcess(context as Activity).play(it.surah, it.ayat)
        }

    }

    private val numberFormat: NumberFormat =
        NumberFormat.getInstance(Locale(ApplicationData(context).language))
}