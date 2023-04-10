package com.onik.quran.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.onik.quran.databinding.ActivityQuranBinding
import com.onik.minar.database.UserData
import com.onik.quran.theme.ApplicationTheme
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class QuranActivity : AppCompatActivity() {
    private var binding: ActivityQuranBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationTheme(this)
        binding = ActivityQuranBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.quranVector?.clipToOutline = true

        binding?.quranStart?.setOnClickListener {
            startActivity(
                Intent(
                    this, QuranMainActivity::class.java
                )
            )
            try {
                copyDataBaseTwo()
                copyDataBase()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            UserData(this).quranLaunched = true
            finish()
        }
    }

    @Throws(IOException::class)
    private fun copyDataBase() {
        val DB_NAME = "Al_Quran.db"
        val DB_PATH = getDatabasePath(DB_NAME).path
        val myInput = assets.open(DB_NAME)
        val myOutput: OutputStream = FileOutputStream(DB_PATH)
        val buffer = ByteArray(1024)
        var length: Int
        while (myInput.read(buffer).also { length = it } > 0) {
            myOutput.write(buffer, 0, length)
        }
        myOutput.flush()
        myOutput.close()
        myInput.close()
    }

    @Throws(IOException::class)
    private fun copyDataBaseTwo() {
        val DB_NAME = "SurahList.db"
        val DB_PATH = getDatabasePath(DB_NAME).path
        val myInput = assets.open(DB_NAME)
        val myOutput: OutputStream = FileOutputStream(DB_PATH)
        val buffer = ByteArray(1024)
        var length: Int
        while (myInput.read(buffer).also { length = it } > 0) {
            myOutput.write(buffer, 0, length)
        }
        myOutput.flush()
        myOutput.close()
        myInput.close()
    }
}