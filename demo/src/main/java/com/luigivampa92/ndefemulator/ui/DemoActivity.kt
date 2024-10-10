package com.luigivampa92.ndefemulator.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.luigivampa92.ndefemulation.NdefEmulation
import com.luigivampa92.ndefemulation.ndef.ContactNdefData
import com.luigivampa92.ndefemulation.ndef.LocationNdefData
import com.luigivampa92.ndefemulation.ndef.TextNdefData
import com.luigivampa92.ndefemulation.ndef.UriNdefData
import com.luigivampa92.ndefemulation.ndef.WifiNetworkNdefData
import com.luigivampa92.ndefemulation.ndef.WifiNetworkNdefDataProtectionType
import com.luigivampa92.ndefemulator.R
import java.util.Date

class DemoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val ndefEmulation = NdefEmulation(this)

        val buttonClear: Button = findViewById(R.id.button_clear)

        val buttonUrlYoutube: Button = findViewById(R.id.button_url_youtube)
        val buttonUriYoutube: Button = findViewById(R.id.button_uri_youtube)
        val buttonUrlWhatsApp: Button = findViewById(R.id.button_url_whatsapp)
        val buttonUrlTelegram: Button = findViewById(R.id.button_uri_telegram)

        val buttonText: Button = findViewById(R.id.button_text)
        val buttonGeo: Button = findViewById(R.id.button_geo)
        val buttonWifi: Button = findViewById(R.id.button_wifi)
        val buttonContact: Button = findViewById(R.id.button_contact)

        buttonClear.setOnClickListener {
            ndefEmulation.currentEmulatedNdefData = null
            toast("Clear data")
        }

        buttonUrlYoutube.setOnClickListener {
            ndefEmulation.currentEmulatedNdefData = UriNdefData("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            toast("Youtube URL")
        }
        buttonUriYoutube.setOnClickListener {
            ndefEmulation.currentEmulatedNdefData = UriNdefData("vnd.youtube://www.youtube.com/watch?v=dQw4w9WgXcQ")
            toast("Youtube URI")
        }
        buttonUrlWhatsApp.setOnClickListener {
            ndefEmulation.currentEmulatedNdefData = UriNdefData("https://wa.me/79123456789")
            toast("WhatsApp")
        }
        buttonUrlTelegram.setOnClickListener {
            ndefEmulation.currentEmulatedNdefData = UriNdefData("tg://msg?to=+79123456789")
            toast("Telegram")
        }

        buttonText.setOnClickListener {
            ndefEmulation.currentEmulatedNdefData = TextNdefData("Whiskey Tango Foxtrot")
            toast("Text")
        }
        buttonGeo.setOnClickListener {
            ndefEmulation.currentEmulatedNdefData = LocationNdefData(59.940825, 30.410988)
            toast("Location")
        }
        buttonWifi.setOnClickListener {
            ndefEmulation.currentEmulatedNdefData = WifiNetworkNdefData("TestWifi", WifiNetworkNdefDataProtectionType.PASSWORD, "TestWifiPassword")
            toast("WiFi")
        }
        buttonContact.setOnClickListener {
            ndefEmulation.currentEmulatedNdefData = ContactNdefData(
                "Thomas",
                "Anderson",
                "+13125550690",
                "tanderson@metacortex.com",
                Date(53617109910L),
                "MetaCortex",
                "Software Engineer",
                "https://hackertyper.net",
                "Wake up",
            )
            toast("Contact")
        }

    }

    private fun toast(message: String?) {
        if (!message.isNullOrBlank()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}