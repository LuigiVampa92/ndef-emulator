package com.luigivampa92.ndefemulation.ndef

import java.net.URI

data class UriNdefData(
    val uri: String
) : NdefData() {
    constructor(uri: URI) : this(uri.toString())
}