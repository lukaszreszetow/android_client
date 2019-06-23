package com.example.androidclient

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URISyntaxException


class MainActivity : AppCompatActivity() {

    lateinit var socket: com.github.nkzawa.socketio.client.Socket
    var emitted: String = ""
    var connectionTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            socket = IO.socket("http://10.0.2.2:1337/")
            socket.connect()
            socket.on("image", onNewImage)
            socket.on("sound", onNewSound)
            socket.on("pdf", onNewPdf)
            Toast.makeText(this, "I think it worked", Toast.LENGTH_SHORT).show()
        } catch (e: URISyntaxException) {
            Toast.makeText(this, "Uri syntax exception", Toast.LENGTH_SHORT).show()
        }

        sendImage.setOnClickListener {
            sendMessage(R.drawable.stars, "image")
        }

        sendSound.setOnClickListener {
            sendMessage(R.raw.small_sound, "sound")
        }

        sendPdf.setOnClickListener {
            sendMessage(R.raw.medium_pdf_file, "pdf")
        }
    }

    private fun sendMessage(resourceToSend: Int, event: String) {
        var message = ""
        val thread = Thread(Runnable {
            val start = SystemClock.elapsedRealtime()
            val array = resources.openRawResource(+resourceToSend).readBytes()
            message = Base64.encodeToString(array, Base64.DEFAULT)
            emitted = message
            Log.d("Tag", "Converting time was : ${SystemClock.elapsedRealtime() - start}")
        })
        thread.start()
        thread.join()
        connectionTime = SystemClock.elapsedRealtime()
        socket.emit(event, message)
    }

    private val onNewImage: Emitter.Listener = Emitter.Listener {
        runOnUiThread {
            Log.d("Tag", "Connection time was : ${SystemClock.elapsedRealtime() - connectionTime}")
            val value = it[0] as String
            Log.d("Tag", "Image EMITED SAME = ${emitted == value}")
//            val decoded: ByteArray = Base64.decode(value, Base64.DEFAULT)
//            val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
//            imageView.setImageBitmap(bitmap)
        }
    }

    private val onNewSound: Emitter.Listener = Emitter.Listener {
        runOnUiThread {
            Log.d("Tag", "Connection time was : ${SystemClock.elapsedRealtime() - connectionTime}")
            val value = it[0] as String
            Log.d("Tag", "Sound EMITED SAME = ${emitted == value}")
//            val decoded: ByteArray = Base64.decode(value, Base64.DEFAULT)
//            val path = File("$cacheDir/musicfile.3gp")
//            val fos = FileOutputStream(path)
//            fos.write(decoded)
//            fos.close()
//            val mediaPlayer = MediaPlayer()
//            mediaPlayer.setDataSource("$cacheDir/musicfile.3gp")
//
//            mediaPlayer.prepare()
//            mediaPlayer.start()
        }
    }

    private val onNewPdf: Emitter.Listener = Emitter.Listener {
        runOnUiThread {
            Log.d("Tag", "Connection time was : ${SystemClock.elapsedRealtime() - connectionTime}")
            val value = it[0] as String
            Log.d("Tag", "pdf EMITED SAME = ${emitted == value}")
//            val decoded: ByteArray = Base64.decode(value, Base64.DEFAULT)
//            val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
//            imageView.setImageBitmap(bitmap)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
        socket.off("message", onNewImage)
    }
}
