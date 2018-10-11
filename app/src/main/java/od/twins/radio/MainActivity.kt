package od.twins.radio

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import be.rijckaert.tim.animatedvector.FloatingMusicActionButton
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var isPlaying = false
    private var player: MediaPlayer = MediaPlayer()
    private var notification: Notification? = null
    private var notificationManager: NotificationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        initPlayer()
        fab_view.setOnClickListener {
            isPlaying = if (!isPlaying) {
                playRadio()
                fab_view.changeMode(FloatingMusicActionButton.Mode.STOP_TO_PLAY)
                if (notification == null)
                    notification = generateNotification(applicationContext)
                notificationManager?.notify(33, notification)
                true
            } else {
                stopRadio()
                fab_view.changeMode(FloatingMusicActionButton.Mode.PLAY_TO_STOP)
                notificationManager?.cancel(33)
                false
            }
        }
    }

    private fun initPlayer() {
        player = MediaPlayer()
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.setDataSource("http://92.60.176.13:8000/radio")
    }

    private fun playRadio() {
        player.prepareAsync()
        player.setOnPreparedListener {
            player.start()
        }
    }

    private fun stopRadio() {
        player.stop()
        player.release()
        initPlayer()
    }

    private fun generateNotification(context: Context): Notification {
        return Notification.Builder(context)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_radio)
                .setAutoCancel(false)
//                .addAction(Notification.Action.Builder(Icon.createWithResource(context,
//                        R.drawable.ic_stop), "stop", getStopIntent(this)).build())
                .addAction(generateAction(R.drawable.stop_icon,"stop", "action_stop"))
                .addAction(generateAction(R.drawable.pause_icon,"pause", "action_pause"))
//                .addAction(Notification.Action.Builder(Icon.createWithResource(context,
//                        R.drawable.ic_play_arrow), "play", null).build())
                .setContentTitle("mAuthor")
                .setContentText("mTitle")
                .setColor(resources.getColor(R.color.colorAccent))
                .setUsesChronometer(true)
                .setContentIntent(PendingIntent.getService(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
                .build()

    }

    private fun generateAction(icon: Int, title: String, intentAction: String): Notification.Action {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.action = intentAction
        val pendingIntent = PendingIntent.getService(applicationContext, 1, intent, 0)
        return Notification.Action.Builder(icon, title, pendingIntent).build()
    }

    private fun getStopIntent(context: Context): PendingIntent? {
        val controlIntent = Intent("od.twins.radio.mediacontrol")
        controlIntent.putExtra("Notification_playback_state", "pause")
        return PendingIntent.getBroadcast(context, 101, controlIntent, 0)
    }
}
