package com.luigivampa92.ndefemulation.ndef

import java.util.Date

data class ContactNdefData(
    val firstName: String,
    val lastName: String?,
    val phoneNumber: String? = null,
    val email: String? = null,
    val birthday: Date? = null,
    val jobCompany: String? = null,
    val jobTitle: String? = null,
    val siteUrl: String? = null,
    val notes: String? = null,
) : NdefData()
