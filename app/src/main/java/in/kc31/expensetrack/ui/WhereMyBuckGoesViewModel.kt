package `in`.kc31.expensetrack.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.kc31.expensetrack.data.api.ApiClient
import `in`.kc31.expensetrack.data.model.AuthRequest
import `in`.kc31.expensetrack.data.model.SmsData
import `in`.kc31.expensetrack.data.preferences.UserPreferences
import `in`.kc31.expensetrack.utils.SmsUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class WhereMyBuckGoesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Loading states for different operations
    private val _isSavingData = MutableStateFlow(false)
    val isSavingData: StateFlow<Boolean> = _isSavingData.asStateFlow()

    private val _isFetchingToken = MutableStateFlow(false)
    val isFetchingToken: StateFlow<Boolean> = _isFetchingToken.asStateFlow()

    private val _isFetchingSenders = MutableStateFlow(false)
    val isFetchingSenders: StateFlow<Boolean> = _isFetchingSenders.asStateFlow()

    private val _isFetchingSms = MutableStateFlow(false)
    val isFetchingSms: StateFlow<Boolean> = _isFetchingSms.asStateFlow()

    private val _isSendingSms = MutableStateFlow<String?>(null) // Stores SMS ID being sent
    val isSendingSms: StateFlow<String?> = _isSendingSms.asStateFlow()

    private val _isHidingSms = MutableStateFlow<String?>(null) // Stores SMS ID being hidden
    val isHidingSms: StateFlow<String?> = _isHidingSms.asStateFlow()

    private val _serverUrl = MutableStateFlow("")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _senderList = MutableStateFlow("")
    val senderList: StateFlow<String> = _senderList.asStateFlow()

    private val _availableSenders = MutableStateFlow<List<String>>(emptyList())
    val availableSenders: StateFlow<List<String>> = _availableSenders.asStateFlow()

    private val _selectedSenders = MutableStateFlow<Set<String>>(emptySet())
    val selectedSenders: StateFlow<Set<String>> = _selectedSenders.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered senders based on search query and with selected senders at the top
    val filteredSenders: StateFlow<List<String>> = combine(
        _availableSenders,
        _searchQuery,
        _selectedSenders
    ) { senders, query, selectedSenders ->
        // First filter by search query if needed
        val filteredList = if (query.isBlank()) {
            senders
        } else {
            senders.filter { it.contains(query, ignoreCase = true) }
        }

        // Then sort to put selected senders at the top
        filteredList.sortedWith(compareByDescending<String> { selectedSenders.contains(it) })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _smsList = MutableStateFlow<List<SmsData>>(emptyList())
    val smsList: StateFlow<List<SmsData>> = _smsList.asStateFlow()

    private val _selectedTimeFilter = MutableStateFlow(TimeFilter.TODAY)
    val selectedTimeFilter: StateFlow<TimeFilter> = _selectedTimeFilter.asStateFlow()

    private lateinit var userPreferences: UserPreferences

    fun initialize(context: Context) {
        userPreferences = UserPreferences(context)

        viewModelScope.launch {
            // Load saved preferences
            _serverUrl.value = userPreferences.serverUrl.first()
            _email.value = userPreferences.email.first()
            _password.value = userPreferences.password.first()
            _senderList.value = userPreferences.senderList.first()

            // Initialize selected senders from the saved sender list
            if (_senderList.value.isNotEmpty()) {
                val savedSenders = _senderList.value.split(",").map { it.trim() }.toSet()
                _selectedSenders.value = savedSenders
            }

            // Fetch SMS messages on initialization
            fetchSmsMessages(context)
        }
    }

    fun updateServerUrl(url: String) {
        _serverUrl.value = url
    }

    fun updateEmail(email: String) {
        _email.value = email
    }

    fun updatePassword(password: String) {
        _password.value = password
    }

    fun updateSenderList(senderList: String) {
        _senderList.value = senderList
    }

    fun updateSelectedSenders(selectedSenders: Set<String>) {
        _selectedSenders.value = selectedSenders
        // Update the comma-separated string for backward compatibility
        _senderList.value = selectedSenders.joinToString(",")
    }

    fun fetchAvailableSenders(context: Context) {
        viewModelScope.launch {
            try {
                _isFetchingSenders.value = true
                _uiState.value = UiState.Loading

                val senders = SmsUtils.fetchUniqueSenders(context)
                _availableSenders.value = senders

                // Initialize selected senders from the current senderList if it's not empty
                if (_senderList.value.isNotEmpty() && _selectedSenders.value.isEmpty()) {
                    val currentSenders = _senderList.value.split(",").map { it.trim() }.toSet()
                    _selectedSenders.value = currentSenders
                }

                _uiState.value = UiState.Success("Found ${senders.size} unique senders")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to fetch senders: ${e.message}")
            } finally {
                _isFetchingSenders.value = false
            }
        }
    }

    fun updateTimeFilter(timeFilter: TimeFilter) {
        _selectedTimeFilter.value = timeFilter
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveUserData() {
        viewModelScope.launch {
            try {
                _isSavingData.value = true
                userPreferences.saveServerUrl(_serverUrl.value)
                userPreferences.saveEmail(_email.value)
                userPreferences.savePassword(_password.value)
                userPreferences.saveSenderList(_senderList.value)
                _uiState.value = UiState.Success("User data saved successfully")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to save user data: ${e.message}")
            } finally {
                _isSavingData.value = false
            }
        }
    }

    fun fetchToken() {
        viewModelScope.launch {
            try {
                _isFetchingToken.value = true
                _uiState.value = UiState.Loading

                val serverUrl = _serverUrl.value
                if (serverUrl.isEmpty()) {
                    _uiState.value = UiState.Error("Server URL cannot be empty")
                    return@launch
                }

                val email = _email.value
                if (email.isEmpty()) {
                    _uiState.value = UiState.Error("Email cannot be empty")
                    return@launch
                }

                val password = _password.value
                if (password.isEmpty()) {
                    _uiState.value = UiState.Error("Password cannot be empty")
                    return@launch
                }

                val apiService = ApiClient.createApiService(serverUrl)
                val authRequest = AuthRequest(email, password)

                val response = apiService.authenticate(authRequest)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        userPreferences.saveAccessToken(authResponse.accessToken)
                        _uiState.value = UiState.Success("Authentication successful")
                    } else {
                        _uiState.value = UiState.Error("Authentication response is empty")
                    }
                } else {
                    _uiState.value = UiState.Error("Authentication failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Authentication failed: ${e.message}")
            } finally {
                _isFetchingToken.value = false
            }
        }
    }

    fun fetchSmsMessages(context: Context) {
        viewModelScope.launch {
            try {
                _isFetchingSms.value = true
                _uiState.value = UiState.Loading

                // Use selected senders if available, otherwise fall back to the comma-separated list
                val senders = if (_selectedSenders.value.isNotEmpty()) {
                    _selectedSenders.value.toList()
                } else {
                    val senderListString = _senderList.value
                    if (senderListString.isEmpty()) {
                        _uiState.value = UiState.Error("Sender list cannot be empty")
                        return@launch
                    }
                    senderListString.split(",").map { it.trim() }
                }
                val sentSmsIds = userPreferences.sentSmsIds.first()

                // Calculate start time based on selected time filter
                val startTime = getStartTimeForFilter(_selectedTimeFilter.value)

                val smsList = SmsUtils.fetchSms(context, senders, startTime, sentSmsIds)

                _smsList.value = smsList
                userPreferences.saveLastFetchTimestamp(Date().time)

                if (smsList.isNotEmpty()) {
                    _uiState.value = UiState.Success("${smsList.size} SMS messages fetched successfully")
                } else {
                    _uiState.value = UiState.Success("No SMS messages found for the selected time period")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to fetch SMS messages: ${e.message}")
            } finally {
                _isFetchingSms.value = false
            }
        }
    }

    private fun getStartTimeForFilter(timeFilter: TimeFilter): Long {
        val calendar = Calendar.getInstance()

        when (timeFilter) {
            TimeFilter.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimeFilter.YESTERDAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
            TimeFilter.LAST_7_DAYS -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.add(Calendar.DAY_OF_YEAR, -7)
            }
            TimeFilter.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimeFilter.LAST_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.add(Calendar.MONTH, -1)
            }
        }

        return calendar.timeInMillis
    }

    fun sendSmsContent(smsData: SmsData) {
        viewModelScope.launch {
            try {
                _isSendingSms.value = smsData.id
                _uiState.value = UiState.Loading

                val serverUrl = _serverUrl.value
                if (serverUrl.isEmpty()) {
                    _uiState.value = UiState.Error("Server URL cannot be empty")
                    return@launch
                }

                val accessToken = userPreferences.accessToken.first()
                if (accessToken.isEmpty()) {
                    _uiState.value = UiState.Error("Access token is not available. Please fetch token first.")
                    return@launch
                }

                val apiService = ApiClient.createApiService(serverUrl)
                val timeStamp = smsData.timestamp;
                val formattedDate = SmsUtils.formatTimestamp(timeStamp);
                val response = apiService.sendSmsContent("Bearer $accessToken", smsData.body + "\n SMS Received at :" + formattedDate)

                if (response.isSuccessful) {
                    // Mark SMS as sent
                    userPreferences.saveSentSmsId(smsData.id)

                    // Remove the sent SMS from the list
                    val updatedList = _smsList.value.toMutableList()
                    updatedList.removeIf { it.id == smsData.id }
                    _smsList.value = updatedList

                    _uiState.value = UiState.Success("SMS content sent successfully")
                } else {
                    _uiState.value = UiState.Error("Failed to send SMS content: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to send SMS content: ${e.message}")
            } finally {
                _isSendingSms.value = null
            }
        }
    }

    fun hideSmsMessage(smsData: SmsData) {
        viewModelScope.launch {
            try {
                _isHidingSms.value = smsData.id

                // Remove the SMS from the list without sending to server
                val updatedList = _smsList.value.toMutableList()
                updatedList.removeIf { it.id == smsData.id }
                _smsList.value = updatedList

                // Mark as hidden in preferences to prevent it from showing up again
                userPreferences.saveSentSmsId(smsData.id)

                _uiState.value = UiState.Success("SMS hidden successfully")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to hide SMS: ${e.message}")
            } finally {
                _isHidingSms.value = null
            }
        }
    }
}

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Error(val message: String) : UiState()
}

enum class TimeFilter {
    TODAY,
    YESTERDAY,
    LAST_7_DAYS,
    THIS_MONTH,
    LAST_MONTH
}
