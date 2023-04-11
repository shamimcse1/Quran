package com.codercamp.quran.notification

import android.app.*
import android.content.Context
import android.content.Context.MEDIA_SESSION_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.RemoteException
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.codercamp.quran.R
import com.codercamp.quran.application.Constant.Companion.PAUSE
import com.codercamp.quran.enum.PlaybackStatus

class MediaNotification(val activity: Context) {

    companion object {
        const val CHANNEL = "AUDIO"
        const val NOTIFICATION_ID = 101
    }

    private var mediaSessionManager: MediaSessionManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var transportControls: MediaControllerCompat.TransportControls? = null

    fun buildNotification(playbackStatus: PlaybackStatus) {

        createNotificationChannels()
        initMediaSession()

        var notificationAction = R.drawable.ic_baseline_call_24 //needs to be initialized

        var playPauseAction: PendingIntent? = null

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus === PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_baseline_call_24
            //create the pause action
            playPauseAction = playbackAction(1)
        } else if (playbackStatus === PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_baseline_call_24
            //create the play action
            playPauseAction = playbackAction(0)
        }
        val largeIcon = BitmapFactory.decodeResource(
            activity.resources,
            R.drawable.ic_baseline_call_24
        ) //replace with your own image

        // Create a new Notification
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(activity, CHANNEL)
                .setShowWhen(false) // Set the Notification style
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle() // Attach our MediaSession token
                        .setMediaSession(mediaSession!!.sessionToken) // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2)
                ) // Set the Notification color
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_baseline_call_24) // Set Notification content information
                .setContentText("Text")
                .setContentTitle("Title")
                .setOngoing(true)
                .setContentInfo("Info") // Add playback actions
                .addAction(R.drawable.ic_baseline_call_24, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", playPauseAction)
                .addAction(R.drawable.ic_baseline_call_24, "next", playbackAction(2))

        (activity.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(
            NOTIFICATION_ID,
            notificationBuilder.build()
        )
    }

    @Throws(RemoteException::class)
    private fun initMediaSession() {
        if (mediaSessionManager != null) return  //mediaSessionManager exists
        mediaSessionManager = activity.getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        // Create a new MediaSession
        mediaSession = MediaSessionCompat(activity, "AudioPlayer")
        //Get MediaSessions transport controls
        transportControls = mediaSession?.controller?.transportControls
        //set MediaSession -> ready to receive media commands
        mediaSession?.isActive = true
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession?.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        // Attach Callback to receive MediaSession updates
        mediaSession?.setCallback(object : MediaSessionCompat.Callback() {
            // Implement callbacks
            override fun onPlay() {
                super.onPlay()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onPause() {
                super.onPause()
                buildNotification(PlaybackStatus.PAUSED)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                buildNotification(PlaybackStatus.PLAYING)
            }

            override fun onStop() {
                super.onStop()
            }

            override fun onSeekTo(position: Long) {
                super.onSeekTo(position)
            }
        })
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(
                CHANNEL,
                "Channel 1",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel1.description = "This is Channel 1"

            val manager = activity.getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel1)
        }
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {

        val pause = Intent(PAUSE)

        return PendingIntent.getBroadcast(
            activity, actionNumber, pause, PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}