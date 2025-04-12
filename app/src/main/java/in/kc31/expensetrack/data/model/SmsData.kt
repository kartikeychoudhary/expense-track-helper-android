package `in`.kc31.expensetrack.data.model

data class SmsData(
    val id: String,
    val sender: String,
    val body: String,
    val timestamp: Long,
    var isSent: Boolean = false
)
