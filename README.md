<hr>
<h5> [Readme in english 🇬🇧] <a href="./README_RU.md"> [Описание на русском 🇷🇺] </a> </h5>
<hr>

# NDEF emulator

- [Description](#description)
- [Usage](#usage)
- [Supported message types](#supported-message-types)
- [Code examples](#code-examples)
- [Demo](#demo)
- [Support](#support)
- [Feedback](#feedback)
- [License](#license)

## Description

Tiny and effective library for Android that allows you to emulate NDEF formatted messages via Android device NFC antenna, using the Android host card emulation API. 

The library is written in Kotlin and has no extra dependencies. 

This library emulates the "NFC Forum Type 4" tag that contains NDEF formatted message.
That type of tags meets all the technical requirements and fits all the restrictions to be emulated via the Android HCE interface.
It is also natively supported in most of the software products that can read NDEF formatted messages via NFC and it is also natively supported on the both mobile operating systems - Android and iOS.

This repository contains the library itself (**ndefemulation**) and the demo application (**demo**).

## Usage

To use the library in your project simply add it to your dependencies in `build.gradle` build script:
```
implementation 'com.luigivampa92:ndefemulation-android:1.0.0'
```

All actions are performed through the `NdefEmulation` class.
This class contains property `currentEmulatedNdefData` (or `getCurrentEmulatedNdefData()`/`setCurrentEmulatedNdefData()` methods if you use Java) that is used to set the data that must be emulated.
You will need the `Context` object to set the current emulated NDEF message or to disable the emulation.
The class has the constructor that accepts the `Context`.
You can create and use the `NdefEmulation` object in any Android component (Activity, Service, etc) or by providing the `Context` object via the DI.

The library interface is very simple and basically fits in just a single line of code.

To enable the emulation, create the required NDEF message object and set it to the `currentEmulatedNdefData` property of `NdefEmulation`:
```
NdefEmulation(context).currentEmulatedNdefData = UriNdefData("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
```

To disable the emulation of NDEF messages simply set `null` to the `currentEmulatedNdefData` property in `NdefEmulation`:
```
NdefEmulation(context).currentEmulatedNdefData = null
```

## Supported message types

| Type              | Class               | Android | iOS | Comment                                                                                                                                                                                                                                                                                           |
|-------------------|---------------------|---------|-----|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Normal URI        | UriNdefData         | ✅       | ✅   | The standard URI. The most useful thing and something that you will probably use in 95% of cases. Any normal URIs and URLs can be used here. The transferred message can be used to trigger IPC, open apps, performing actions etc. Perfectly and natively works on both Android and iOS          |
| Location GEO-URI  | LocationNdefData    | ✅       | 🤷  | Geolocation point. Works natively on Android, however to properly receive this kind of message on iOS, you must have an application that supports the `geo://` URI scheme installed (like "MapsMe", for instance)                                                                                 |
| Raw Text          | TextNdefData        | ❌       | ❌   | Simple raw text. While it is the simplest type of message it can not be interpreted out-of-the-box by neither Android nor iOS without the special software that will expect some specific content in it. The OS will read this message, but will not parse it or do anything else                 |
| WiFi Connection   | WifiNetworkNdefData | ✅       | ❌   | Wifi access point connection data - name and password. Only OPEN (no password) and WPA/WPA2-PSK (standard password) types of wifi are supported. The EAP connections with user certificates is not supported. Works natively only on Android, iOS does not react to this type of messages         |
| Phonebook Contact | ContactNdefData     | ✅       | ❌   | Phonebook contact data in the VCard format. Works natively only on Android, iOS does not react to this type of messages                                                                                                                                                                           |
| Another Raw NDEF  | NdefRecordData      | 🤷      | 🤷  | Any NdefRecord class from Android framework can be emulated. For example, you can receive NDEF record by intent in your application and then set it to be emulated without even parsing it. Very useful but whether it will be received or not on the reader side depends on the message contents |

- ✅ - Means that the mentioned OS can natively read and parse that type of message. You do not need any specific third-party applications on the device that will read the emulated tag. After the message will be transferred to the reading device, you will see some notification of some action will be performed right away 
- ❌ - Means that the mentioned OS can natively read but will not parse that type of message, and will not perform any actions after it. Some third party applications may have the implementations that will handle this type of message but not the OS itself
- 🤷 - Means that "it depends", the message will be read but its further handing depends on extra circumstances

## Code examples

Some code examples of emulating the messages of various types
<br>

Send the Youtube video by the HTTP URL:
```
NdefEmulation(this).currentEmulatedNdefData = UriNdefData("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
```

Send the Youtube video by the deeplink URI:
```
NdefEmulation(this).currentEmulatedNdefData = UriNdefData("vnd.youtube://www.youtube.com/watch?v=dQw4w9WgXcQ")
```

Open the WhatsApp dialog by a phone number:
```
NdefEmulation(this).currentEmulatedNdefData = UriNdefData("https://wa.me/79123456789")
```

Open the Telegram dialog by a phone number:
```
NdefEmulation(this).currentEmulatedNdefData = UriNdefData("tg://msg?to=+79123456789")
```

Send some raw text:
```
NdefEmulation(this).currentEmulatedNdefData = TextNdefData("Whiskey Tango Foxtrot")
```

Send the geolocation point:
```
NdefEmulation(this).currentEmulatedNdefData = LocationNdefData(12.345678, 78.654321)
```

Send the WiFi access point name and password:
```
NdefEmulation(this).currentEmulatedNdefData = WifiNetworkNdefData("YourWifiName", WifiNetworkNdefDataProtectionType.PASSWORD, "YourWifiPassword")
```

Send the phonebook contact data:
```
NdefEmulation(this).currentEmulatedNdefData = ContactNdefData("John", "Doe", "+12345678900")
```

## Demo

The demo applications can be found and downloaded in Releases.  

You can download the demo apps for the latest library version [here](https://github.com/LuigiVampa92/ndef-emulator/releases/tag/release-1.0.0).

There are versions for the [smartphones](https://github.com/LuigiVampa92/ndef-emulator/releases/download/release-1.0.0/ndef_emulation_demo_phone_release_1.0.0.apk) and for the [watches](https://github.com/LuigiVampa92/ndef-emulator/releases/download/release-1.0.0/ndef_emulation_demo_watch_release_1.0.0.apk)

You can also build the demo applications yourself from the sources:
```
./gradlew clean assemblePhoneRelease
./gradlew clean assembleWatchRelease
```

## Support

* Star this GitHub repository ⭐

## Feedback

You can provide your feedback, suggest ideas for the new features or simply ask the questions here:
- [Email](mailto:luigivampa92@gmail.com) ✉️
- [Telegram](https://t.me/luigivampa92) 💬

## License

Please see the [LICENSE](LICENSE.md) for details.
