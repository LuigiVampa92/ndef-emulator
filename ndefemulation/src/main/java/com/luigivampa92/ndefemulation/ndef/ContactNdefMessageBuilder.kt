package com.luigivampa92.ndefemulation.ndef

import android.annotation.SuppressLint
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.telephony.PhoneNumberUtils
import android.util.Patterns
import com.luigivampa92.ndefemulation.Logger
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat

internal class ContactNdefMessageBuilder : NdefMessageBuilder {

    private companion object {
        private const val MAX_LENGTH_NAME_PART = 32
        private const val MAX_LENGTH_PHONE_NUMBER = 24
        private const val MAX_LENGTH_EMAIL = 48
        private const val MAX_LENGTH_NOTES = 640
        private const val MAX_LENGTH_GENERIC = 64
        private const val MIME_TYPE_TEXT_VCARD = "text/vcard"
        private val NDEF_MIME_TYPE_VCARD = MIME_TYPE_TEXT_VCARD.toByteArray()
    }

    override fun build(ndefData: NdefData): NdefMessage? {
        if (ndefData !is ContactNdefData) {
            return null
        }
        val contactData: ContactNdefData = ndefData
        if (!validateContactData(contactData)) {
            return null
        }
        val ndefRecord = createContactRecord(contactData)
        return NdefMessage(ndefRecord)
    }

    private fun validateContactData(contactData: ContactNdefData): Boolean {
        val firstNameIsValid = contactData.firstName.isNotBlank() && contactData.firstName.length in 1..MAX_LENGTH_NAME_PART
        if (!firstNameIsValid) {
            Logger.d("Unable to create contact card NDEF data - firstName size is invalid (must be not empty and less than $MAX_LENGTH_NAME_PART characters")
            return false
        }
        if (contactData.lastName != null) {
            val lastNameIsValid = contactData.lastName.length in 1..MAX_LENGTH_NAME_PART
            if (!lastNameIsValid) {
                Logger.d("Unable to create contact card NDEF data - lastName size is invalid (must be not empty and less than $MAX_LENGTH_NAME_PART characters")
                return false
            }
        }
        if (contactData.phoneNumber != null) {
            val phoneNumberSizeIsValid = contactData.phoneNumber.length in 1..MAX_LENGTH_PHONE_NUMBER
            if (!phoneNumberSizeIsValid) {
                Logger.d("Unable to create contact card NDEF data - phoneNumber size is invalid (must be not empty and less than $MAX_LENGTH_PHONE_NUMBER characters")
                return false
            }
            val phoneNumberIsValid = PhoneNumberUtils.isGlobalPhoneNumber(contactData.phoneNumber)
            if (!phoneNumberIsValid) {
                Logger.d("Unable to create contact card NDEF data - phoneNumber is invalid")
                return false
            }
        }
        if (contactData.email != null) {
            val emailSizeIsValid = contactData.email.length in 1..MAX_LENGTH_EMAIL
            if (!emailSizeIsValid) {
                Logger.d("Unable to create contact card NDEF data - email size is invalid (must be not empty and less than $MAX_LENGTH_EMAIL characters")
                return false
            }
            val emailIsValid = Patterns.EMAIL_ADDRESS.toRegex().matches(contactData.email)
            if (!emailIsValid) {
                Logger.d("Unable to create contact card NDEF data - email is invalid")
                return false
            }
        }
        // contactData.birthday
        if (contactData.jobCompany != null) {
            val jobCompanyIsValid = contactData.jobCompany.length in 1..MAX_LENGTH_GENERIC
            if (!jobCompanyIsValid) {
                Logger.d("Unable to create contact card NDEF data - jobCompany size is invalid (must be not empty and less than $MAX_LENGTH_GENERIC characters")
                return false
            }
        }
        if (contactData.jobTitle != null) {
            val jobCompanyIsValid = contactData.jobTitle.length in 1..MAX_LENGTH_GENERIC
            if (!jobCompanyIsValid) {
                Logger.d("Unable to create contact card NDEF data - jobTitle size is invalid (must be not empty and less than $MAX_LENGTH_GENERIC characters")
                return false
            }
        }
        if (contactData.siteUrl != null) {
            val jobCompanyIsValid = contactData.siteUrl.length in 1..MAX_LENGTH_GENERIC
            if (!jobCompanyIsValid) {
                Logger.d("Unable to create contact card NDEF data - siteUrl size is invalid (must be not empty and less than $MAX_LENGTH_GENERIC characters")
                return false
            }
        }
        if (contactData.notes != null) {
            val jobCompanyIsValid = contactData.notes.length in 1..MAX_LENGTH_NOTES
            if (!jobCompanyIsValid) {
                Logger.d("Unable to create contact card NDEF data - notes size is invalid (must be not empty and less than $MAX_LENGTH_NOTES characters")
                return false
            }
        }
        return true
    }

    private fun createContactRecord(contactData: ContactNdefData): NdefRecord {
        val contactLanguage = ""
        val languageBytes: ByteArray
        val textBytes: ByteArray
        try {
            languageBytes = contactLanguage.toByteArray(charset("UTF-8"))
            textBytes = buildVcardNdef3Data(contactData).toByteArray(charset("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            throw AssertionError(e)
        }

        val recordPayload = ByteArray(1 + (languageBytes.size and 0x03F) + textBytes.size)
        recordPayload[0] = (languageBytes.size and 0x03F).toByte()
        System.arraycopy(languageBytes, 0, recordPayload, 1, languageBytes.size and 0x03F)
        System.arraycopy(textBytes, 0, recordPayload, 1 + (languageBytes.size and 0x03F), textBytes.size)

        return NdefRecord(NdefRecord.TNF_MIME_MEDIA, NDEF_MIME_TYPE_VCARD, NdefConstants.NDEF_FID_DATA, recordPayload)
    }

    @SuppressLint("SimpleDateFormat")
    private fun buildVcardNdef3Data(contactData: ContactNdefData): String {
        val builder = StringBuilder("BEGIN:VCARD\n")
        builder.append("VERSION:3.0\n")

        val hasFirstName = !contactData.firstName.isBlank()
        val hasLastName = !contactData.lastName.isNullOrBlank()
        if (hasFirstName && hasLastName) {
            builder.append(String.format("N:%s;%s;;;;\n", contactData.lastName?.trim() ?: "", contactData.firstName.trim()))
            builder.append(String.format("FN:%s %s\n", contactData.firstName.trim(), contactData.lastName?.trim() ?: ""))
        } else if (hasFirstName && !hasLastName) {
            builder.append(String.format("N:;%s;;;;\n", contactData.firstName.trim()))
            builder.append(String.format("FN:%s\n", contactData.firstName.trim()))
        } else if (!hasFirstName && hasLastName) {
            builder.append(String.format("N:%s;;;;;\n", contactData.lastName?.trim() ?: ""))
            builder.append(String.format("FN:%s\n", contactData.lastName?.trim() ?: ""))
        }

        if (!contactData.phoneNumber.isNullOrBlank()) {
            builder.append(String.format("TEL:%s\n", contactData.phoneNumber.trim()))
        }
        if (!contactData.email.isNullOrBlank()) {
            builder.append(String.format("EMAIL:%s\n", contactData.email.trim()))
        }
        if (contactData.birthday != null) {
            builder.append(String.format("BDAY:%s\n", SimpleDateFormat("yyyy-MM-dd").format(contactData.birthday)))
        }
        if (!contactData.jobCompany.isNullOrBlank()) {
            builder.append(String.format("ORG:%s\n", contactData.jobCompany.trim()))
        }
        if (!contactData.jobTitle.isNullOrBlank()) {
            builder.append(String.format("TITLE:%s\n", contactData.jobTitle.trim()))
        }
        if (!contactData.siteUrl.isNullOrBlank()) {
            builder.append(String.format("URL:%s\n", contactData.siteUrl.trim()))
        }
        if (!contactData.notes.isNullOrBlank()) {
            builder.append(String.format("NOTE:%s\n", contactData.notes.trim()))
        }
        builder.append("END:VCARD")
        return builder.toString()
    }
}
