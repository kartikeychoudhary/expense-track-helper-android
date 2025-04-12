package `in`.kc31.expensetrack.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("user") val user: User
)

data class User(
    @SerializedName("firstname") val firstName: String,
    @SerializedName("lastname") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("profilePicURL") val profilePicUrl: String?
)
