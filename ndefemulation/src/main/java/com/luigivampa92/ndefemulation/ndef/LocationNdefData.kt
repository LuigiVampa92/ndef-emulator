package com.luigivampa92.ndefemulation.ndef

import android.location.Location

data class LocationNdefData (
    val latitude: Double,
    val longitude: Double,
) : NdefData() {
    constructor(location: Location) : this(location.latitude, location.longitude)
}
