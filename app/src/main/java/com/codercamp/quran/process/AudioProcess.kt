package com.codercamp.quran.process

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import com.codercamp.quran.R
import com.codercamp.quran.application.Constant.Companion.PLAY
import com.codercamp.quran.services.AudioService
import com.codercamp.quran.sql.QuranHelper
import com.codercamp.quran.sql.SurahHelper
import com.codercamp.quran.uiClass.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@SuppressLint("SetTextI18n")
class AudioProcess(val activity: Activity) {

    private var surah = 0
    private var current = 0
    private var downloads = 0
    private var error = false
    private var dialog: Dialog? = null
    private val playList  = ArrayList<String>()
    private val requiredDownload = ArrayList<Int>()

    fun play(sur: Int, ayat: Int) {

        surah = sur-1
        current = ayat-1

        val directory = activity.getExternalFilesDir("Audio")?.path

        CoroutineScope(Dispatchers.IO).launch {
            QuranHelper(activity).readSurahNo(sur).forEach {
                val location = "$directory/${it.pos + 1}.mp3"
                if (!File(location).exists())
                    requiredDownload.add(it.pos + 1)
                playList.add(location)
            }

            if (requiredDownload.isEmpty()) startPlaying()
            else {
                downloads = requiredDownload.size
                activity.runOnUiThread {
                    dialog = Dialog(activity)
                    dialog?.setContentView(R.layout.dialog_download)
                    dialog?.setCancelable(false)
                    dialog?.findViewById<TextView>(R.id.title)
                        ?.text = "Surah: " + SurahHelper(activity).readDataAt(sur)?.name
                    dialog?.findViewById<TextView>(R.id.download_total)
                        ?.text = "0/$downloads"
                    dialog?.findViewById<ProgressBar>(R.id.download_progress)
                        ?.max = downloads
                    dialog?.show()
                }
                downloadAudio(requiredDownload[0])
            }
        }
    }

    private fun downloadAudio(id: Int) {
        requiredDownload.remove(id)
        activity.runOnUiThread {
            dialog?.findViewById<ProgressBar>(R.id.download_progress)
                ?.progress = downloads - requiredDownload.size
            dialog?.findViewById<TextView>(R.id.download_total)
                ?.text = "${downloads - requiredDownload.size}/$downloads"
        }
        val name = "http://cdn.islamic.network/quran/audio/64/ar.alafasy/$id.mp3"
        val urlConnection = URL(name).openConnection() as HttpURLConnection
        try {
            val directory = activity.getExternalFilesDir("Audio")?.path
            val file = File(directory, "$id.mp3")
            if (file.exists()) file.delete()
            val fos = FileOutputStream(file, true)
            fos.write(urlConnection.inputStream.readBytes())
            fos.close()
            if (requiredDownload.isEmpty()) {
                startPlaying()
                dialog?.dismiss()
            }
            else downloadAudio(requiredDownload[0])
        } catch (e: Exception) {
            if (!error) {
                requiredDownload.add(0, id)
                downloadAudio(requiredDownload[0])
            } else {
                dialog?.dismiss()
                CustomToast(activity).show(e.toString(), CustomToast.TOAST_NEGATIVE)
            }
            Log.e("Error", e.toString())
        } finally {
            urlConnection.disconnect()
        }
    }

    private fun serviceRunning(): Boolean {
        val manager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (AudioService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun startPlaying() {
        if (serviceRunning()) {
            activity.sendBroadcast(
                Intent(PLAY)
                    .putExtra("SURAH", surah)
                    .putExtra("CURRENT", current)
                    .putExtra("LIST", playList)
            )
        } else {
            activity.startService(
                Intent(activity, AudioService::class.java)
                    .putExtra("SURAH", surah)
                    .putExtra("CURRENT", current)
                    .putExtra("LIST", playList)
            )
        }
    }
}