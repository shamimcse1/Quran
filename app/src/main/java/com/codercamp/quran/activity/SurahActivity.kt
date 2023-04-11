package com.codercamp.quran.activity

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.codercamp.quran.adapter.PagerAdapter
import com.codercamp.quran.database.ApplicationData
import com.codercamp.quran.database.LastRead
import com.codercamp.quran.databinding.ActivitySurahBinding
import com.codercamp.quran.fragment.SurahAyat
import com.codercamp.quran.sql.SurahHelper
import com.codercamp.quran.theme.ApplicationTheme
import com.codercamp.quran.utils.ContextUtils
import java.util.*

class SurahActivity : AppCompatActivity() {

    companion object {
        fun launch(context: Context, surahNo: Int, ayat: Int?) {
            context.startActivity(
                Intent(
                    context,
                    SurahActivity::class.java
                ).putExtra("SURAH_NO", surahNo)
                    .putExtra("AYAT", ayat)
            )
        }
    }
    private var surahNo: Int? = null
    private var binding: ActivitySurahBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationTheme(this)
        binding = ActivitySurahBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.back?.setOnClickListener { finish() }

        surahNo = intent.getIntExtra("SURAH_NO", 0)

        binding?.qPager?.adapter = PagerAdapter(
            supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ).apply {
            SurahHelper(this@SurahActivity)
                .readData().reversed().forEach {
                    addFragment(
                        SurahAyat(it.pos-1,
                        intent.getIntExtra("AYAT", 0), it.pos==(surahNo!!+1))
                    )
                    binding?.tabLayout?.newTab()?.setText(it.name)?.let { it1 ->
                        binding?.tabLayout?.addTab(it1)
                    }
                }
        }

        binding?.tabLayout?.tabGravity = TabLayout.GRAVITY_FILL

        binding?.qPager?.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(binding?.tabLayout)
        )

        binding?.tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                /****/
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                /****/
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                binding?.qPager?.currentItem = tab.position
                SurahHelper(this@SurahActivity).readDataAt(114-tab.position)?.let {
                    LastRead(this@SurahActivity).surahName = it.name
                }
                LastRead(this@SurahActivity).surahNo = 113-tab.position
            }
        })

        binding?.qPager?.offscreenPageLimit = 3
        binding?.qPager?.currentItem = SurahHelper(this@SurahActivity)
            .readData().size - surahNo!!-1
    }

    override fun attachBaseContext(newBase: Context?) {
        val localeToSwitchTo = Locale(ApplicationData(newBase!!).language)
        val localeUpdatedContext: ContextWrapper = ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }
}