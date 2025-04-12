package `in`.kc31.expensetrack.data.api

import `in`.kc31.expensetrack.data.model.AuthRequest
import `in`.kc31.expensetrack.data.model.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/v1/auth/authenticate")
    suspend fun authenticate(@Body authRequest: AuthRequest): Response<AuthResponse>

    @POST("api/v1/genAi")
    suspend fun sendSmsContent(
        @Header("Authorization") authToken: String,
        @Body smsBody: String
    ): Response<Unit>
}
