package od.twins.radio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import be.rijckaert.tim.animatedvector.FloatingMusicActionButton
import kotlinx.android.synthetic.main.activity_main.*
import od.twins.radio.service.PlayerService

class MainActivity : AppCompatActivity() {
    private var isPlaying = false
    private var mediaController: MediaControllerCompat? = null
    private var callback: MediaControllerCompat.Callback? = null
    private var serviceConnection: ServiceConnection? = null
    private var playerServiceBinder: PlayerService.PlayerServiceBinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPlayer()
        fab_view.setOnClickListener {
            isPlaying = if (!isPlaying) {
                mediaController?.transportControls?.play()
                Toast.makeText(this, "play", Toast.LENGTH_SHORT).show()
                true
            } else {
                mediaController?.transportControls?.stop()
                false
            }
        }
    }

    private fun initPlayer() {
        callback = object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                if (state == null)
                    return
                if (state.state == PlaybackStateCompat.STATE_PLAYING)
                    fab_view.changeMode(FloatingMusicActionButton.Mode.STOP_TO_PLAY)
                else
                    fab_view.changeMode(FloatingMusicActionButton.Mode.PLAY_TO_STOP)
            }
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                playerServiceBinder = service as PlayerService.PlayerServiceBinder
                try {
                    mediaController = MediaControllerCompat(this@MainActivity,
                            playerServiceBinder!!.mediaSessionToken)
                    mediaController?.registerCallback(callback as MediaControllerCompat.Callback)
                    callback?.onPlaybackStateChanged(mediaController!!.playbackState)
                } catch (e: RemoteException) {
                    mediaController = null
                }

            }

            override fun onServiceDisconnected(name: ComponentName) {
                playerServiceBinder = null
                if (mediaController != null) {
                    mediaController?.unregisterCallback(callback as MediaControllerCompat.Callback)
                    mediaController = null
                }
            }
        }
        bindService(Intent(this, PlayerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        playerServiceBinder = null
        mediaController?.unregisterCallback(callback as MediaControllerCompat.Callback)
        mediaController = null
        if (serviceConnection != null)
            unbindService(serviceConnection)
    }
}
