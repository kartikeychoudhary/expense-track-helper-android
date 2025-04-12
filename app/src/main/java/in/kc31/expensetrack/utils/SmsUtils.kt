package `in`.kc31.expensetrack.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import `in`.kc31.expensetrack.data.model.SmsData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object SmsUtils {

    // Format timestamp to "dd MMM yyyy HH:mm" format
    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    fun fetchSms(
        context: Context,
        senders: List<String>,
        startTime: Long,
        sentSmsIds: Set<String>
    ): List<SmsData> {
        val smsList = mutableListOf<SmsData>()

        // If no senders are specified, return an empty list
        if (senders.isEmpty()) {
            return smsList
        }

        // Create a selection string for the senders
        val senderSelection = senders.joinToString(" OR ") {
            "${Telephony.Sms.ADDRESS} LIKE ?"
        }

        // Create selection arguments for the senders (adding % for partial matching)
        val selectionArgs = senders.map { "%$it%" }.toTypedArray()

        // Add timestamp condition to the selection
        val timeSelection = "($senderSelection) AND ${Telephony.Sms.DATE} > ?"

        // Add timestamp to selection args
        val timeSelectionArgs = selectionArgs + startTime.toString()

        // Query the SMS inbox
        val cursor: Cursor? = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE
            ),
            timeSelection,
            timeSelectionArgs,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(Telephony.Sms._ID)
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)

                // Skip if this SMS has already been sent
                if (sentSmsIds.contains(id)) {
                    continue
                }

                val address = it.getString(addressIndex)
                val body = it.getString(bodyIndex)
                val date = it.getLong(dateIndex)

                smsList.add(
                    SmsData(
                        id = id,
                        sender = address,
                        body = body,
                        timestamp = date
                    )
                )
            }
        }

        return smsList
    }

    fun fetchUniqueSenders(context: Context): List<String> {
        val senders = mutableSetOf<String>()

        // Query the SMS inbox to get all unique senders
        val cursor: Cursor? = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS),
            null,
            null,
            null
        )

        cursor?.use {
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)

            while (it.moveToNext()) {
                val address = it.getString(addressIndex)
                if (!address.isNullOrBlank()) {
                    senders.add(address)
                }
            }
        }

        return senders.toList().sorted()
    }
}
