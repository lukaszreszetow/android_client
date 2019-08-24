package com.example.androidclient

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Base64
import android.util.Log
import android.widget.Toast
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URISyntaxException


class MainActivity : AppCompatActivity() {

    lateinit var socket: Socket
    var wyemitowanaWiadomosc: String = ""
    var czasKomunikacji: Long = 0
    var startConnection: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectToServer.setOnClickListener {
            connectToServer()
        }

        sendImage.setOnClickListener {
            sendMessage(R.drawable.gwiazdy, "obraz")
        }

        sendSound.setOnClickListener {
            sendMessage(R.raw.krotki_dzwiek, "dzwiek")
        }

        sendPdf.setOnClickListener {
            sendMessage(R.raw.sredni_plik_pdf, "pdf")
        }
    }

    private fun connectToServer() {
        try {
            val ip = IPaddress.text.toString()
            socket = IO.socket("http://$ip:1337/")
            socket.on(Socket.EVENT_CONNECT, polaczono)
            socket.on("obraz", nowaWiadomoscZSerwera)
            socket.on("dzwiek", nowaWiadomoscZSerwera)
            socket.on("pdf", nowaWiadomoscZSerwera)
            startConnection = SystemClock.elapsedRealtime()
            socket.connect()
        } catch (e: URISyntaxException) {
            Toast.makeText(this, "Wyjątek zwiazany ze skladnia URI", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage(plikDoWyslania: Int, naglowek: String) {
        val start = SystemClock.elapsedRealtime()
        val tablicaBitow = resources.openRawResource(+plikDoWyslania).readBytes()
        val wiadomosc = Base64.encodeToString(tablicaBitow, Base64.DEFAULT)
        wyemitowanaWiadomosc = wiadomosc
        Log.d("AndroidClient", "Czas konwersji : ${SystemClock.elapsedRealtime() - start}")
        czasKomunikacji = SystemClock.elapsedRealtime()
        socket.emit(naglowek, wiadomosc)
    }

    private val polaczono: Emitter.Listener = Emitter.Listener {
        Log.d("AndroidClient", "Czas nawiazywania polaczenia: ${SystemClock.elapsedRealtime() - startConnection}")
    }

    private val nowaWiadomoscZSerwera: Emitter.Listener = Emitter.Listener {
        runOnUiThread {
            Log.d("AndroidClient", "Komunikacja trwala : ${SystemClock.elapsedRealtime() - czasKomunikacji}")
            val wiadomoscZSerwera = it[0] as String
            Log.d("AndroidClient", "pliki sa identyczne? = ${wyemitowanaWiadomosc == wiadomoscZSerwera}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
        socket.off("message", nowaWiadomoscZSerwera)
    }
}
