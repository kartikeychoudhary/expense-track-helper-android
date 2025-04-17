package `in`.kc31.expensetrack.ui

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import `in`.kc31.expensetrack.utils.SmsUtils
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import `in`.kc31.expensetrack.R
import `in`.kc31.expensetrack.ui.TimeFilter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@Composable
fun getTimeFilterLabel(timeFilter: TimeFilter): String {
    return when (timeFilter) {
        TimeFilter.TODAY -> stringResource(R.string.today)
        TimeFilter.YESTERDAY -> stringResource(R.string.yesterday)
        TimeFilter.LAST_7_DAYS -> stringResource(R.string.last_7_days)
        TimeFilter.THIS_MONTH -> stringResource(R.string.this_month)
        TimeFilter.LAST_MONTH -> stringResource(R.string.last_month)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WhereMyBuckGoesScreen(
    viewModel: WhereMyBuckGoesViewModel
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val senderList by viewModel.senderList.collectAsState()
    val smsList by viewModel.smsList.collectAsState()
    val availableSenders by viewModel.availableSenders.collectAsState()
    val filteredSenders by viewModel.filteredSenders.collectAsState()
    val selectedSenders by viewModel.selectedSenders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Loading states
    val isSavingData by viewModel.isSavingData.collectAsState()
    val isFetchingToken by viewModel.isFetchingToken.collectAsState()
    val isFetchingSenders by viewModel.isFetchingSenders.collectAsState()
    val isFetchingSms by viewModel.isFetchingSms.collectAsState()
    val isSendingSms by viewModel.isSendingSms.collectAsState()
    val isHidingSms by viewModel.isHidingSms.collectAsState()

    // State for configuration section expansion
    var isConfigExpanded by remember { mutableStateOf(true) }

    // Initialize ViewModel with context
    LaunchedEffect(key1 = true) {
        viewModel.initialize(context)
    }

    // Request SMS permission
    val smsPermissionState = rememberPermissionState(Manifest.permission.READ_SMS)

    LaunchedEffect(key1 = uiState) {
        when (uiState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar((uiState as UiState.Success).message)
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((uiState as UiState.Error).message)
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.wheremybuckgoes_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Configuration Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Configuration header with expand/collapse button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (isConfigExpanded) 8.dp else 0.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.config_title),
                            style = MaterialTheme.typography.titleLarge
                        )

                        IconButton(onClick = { isConfigExpanded = !isConfigExpanded }) {
                            Icon(
                                imageVector = if (isConfigExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isConfigExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand)
                            )
                        }
                    }

                    // Configuration content - only show when expanded
                    if (isConfigExpanded) {
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { viewModel.updateServerUrl(it) },
                            label = { Text(stringResource(R.string.backend_url)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { viewModel.updateEmail(it) },
                            label = { Text(stringResource(R.string.email)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { viewModel.updatePassword(it) },
                            label = { Text(stringResource(R.string.password)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        // Sender selection section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.sender_list),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            // Fetch senders button and selected senders display
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.fetchAvailableSenders(context) },
                                    enabled = !isFetchingSenders
                                ) {
                                    if (isFetchingSenders) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(stringResource(R.string.fetch_senders))
                                }

                                Text(
                                    text = if (selectedSenders.isEmpty()) {
                                        stringResource(R.string.no_senders_selected)
                                    } else {
                                        stringResource(R.string.selected_senders, selectedSenders.size)
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Multi-select dropdown
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                var expanded by remember { mutableStateOf(false) }

                                Button(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.select_senders))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = {
                                        expanded = false
                                        viewModel.updateSearchQuery("") // Clear search when dropdown is closed
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .wrapContentHeight()
                                ) {
                                    // Search box at the top of dropdown
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        OutlinedTextField(
                                            value = searchQuery,
                                            onValueChange = { viewModel.updateSearchQuery(it) },
                                            placeholder = { Text(stringResource(R.string.search_senders)) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            trailingIcon = {
                                                if (searchQuery.isNotEmpty()) {
                                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                                        Icon(
                                                            imageVector = Icons.Default.Clear,
                                                            contentDescription = stringResource(R.string.clear_search)
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }

                                    HorizontalDivider()

                                    if (availableSenders.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("No senders available. Click 'Fetch Senders' first.") },
                                            onClick = { expanded = false }
                                        )
                                    } else if (filteredSenders.isEmpty() && searchQuery.isNotEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.no_search_results)) },
                                            onClick = { viewModel.updateSearchQuery("") }
                                        )
                                    } else {
                                        // List of filtered senders - limit to 10 items at a time
                                        val displaySenders = filteredSenders.take(10)
                                        Column {
                                            displaySenders.forEach { sender ->
                                                val isSelected = selectedSenders.contains(sender)
                                                DropdownMenuItem(
                                                    text = {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                if (isSelected) {
                                                                    // Show a colored dot for selected items
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(8.dp)
                                                                            .background(
                                                                                color = MaterialTheme.colorScheme.primary,
                                                                                shape = CircleShape
                                                                            )
                                                                            .padding(end = 8.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(8.dp))
                                                                }

                                                                // Show the sender text with bold style if selected
                                                                Text(
                                                                    text = sender,
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                                )
                                                            }

                                                            if (isSelected) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = "Selected",
                                                                    tint = MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        val newSelection = selectedSenders.toMutableSet()
                                                        if (isSelected) {
                                                            newSelection.remove(sender)
                                                        } else {
                                                            newSelection.add(sender)
                                                        }
                                                        viewModel.updateSelectedSenders(newSelection)
                                                    }
                                                )
                                            }

                                            // Show a message if there are more items
                                            if (filteredSenders.size > 10) {
                                                HorizontalDivider()
                                                DropdownMenuItem(
                                                    text = { Text("${filteredSenders.size - 10} more items. Refine your search.") },
                                                    onClick = {}
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { viewModel.saveUserData() },
                                enabled = !isSavingData && !isFetchingToken
                            ) {
                                if (isSavingData) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(stringResource(R.string.save_data))
                            }

                            Button(
                                onClick = { viewModel.fetchToken() },
                                enabled = !isFetchingToken && !isSavingData
                            ) {
                                if (isFetchingToken) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(stringResource(R.string.fetch_token))
                            }
                        }
                    }
                }
            }

            // SMS Display Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.sms_messages),
                            style = MaterialTheme.typography.titleLarge
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Time Filter Dropdown
                            Box {
                                var expanded by remember { mutableStateOf(false) }
                                val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsState()

                                Button(
                                    onClick = { expanded = true }
                                ) {
                                    Text(getTimeFilterLabel(selectedTimeFilter))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.today)) },
                                        onClick = {
                                            viewModel.updateTimeFilter(TimeFilter.TODAY)
                                            expanded = false
                                            if (smsPermissionState.status.isGranted) {
                                                viewModel.fetchSmsMessages(context)
                                            } else {
                                                smsPermissionState.launchPermissionRequest()
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.yesterday)) },
                                        onClick = {
                                            viewModel.updateTimeFilter(TimeFilter.YESTERDAY)
                                            expanded = false
                                            if (smsPermissionState.status.isGranted) {
                                                viewModel.fetchSmsMessages(context)
                                            } else {
                                                smsPermissionState.launchPermissionRequest()
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.last_7_days)) },
                                        onClick = {
                                            viewModel.updateTimeFilter(TimeFilter.LAST_7_DAYS)
                                            expanded = false
                                            if (smsPermissionState.status.isGranted) {
                                                viewModel.fetchSmsMessages(context)
                                            } else {
                                                smsPermissionState.launchPermissionRequest()
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.this_month)) },
                                        onClick = {
                                            viewModel.updateTimeFilter(TimeFilter.THIS_MONTH)
                                            expanded = false
                                            if (smsPermissionState.status.isGranted) {
                                                viewModel.fetchSmsMessages(context)
                                            } else {
                                                smsPermissionState.launchPermissionRequest()
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.last_month)) },
                                        onClick = {
                                            viewModel.updateTimeFilter(TimeFilter.LAST_MONTH)
                                            expanded = false
                                            if (smsPermissionState.status.isGranted) {
                                                viewModel.fetchSmsMessages(context)
                                            } else {
                                                smsPermissionState.launchPermissionRequest()
                                            }
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Fetch Button
                            Button(
                                onClick = {
                                    if (smsPermissionState.status.isGranted) {
                                        viewModel.fetchSmsMessages(context)
                                    } else {
                                        smsPermissionState.launchPermissionRequest()
                                    }
                                },
                                enabled = !isFetchingSms
                            ) {
                                if (isFetchingSms) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(stringResource(R.string.fetch_sms))
                            }
                        }
                    }

                    if (uiState is UiState.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isConfigExpanded) 200.dp else 400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (smsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isConfigExpanded) 200.dp else 400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.no_sms_found))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isConfigExpanded) 400.dp else 600.dp)
                        ) {
                            items(smsList) { sms ->
                                SmsItem(
                                    sms = sms,
                                    onSendClick = { viewModel.sendSmsContent(sms) },
                                    onHideClick = { viewModel.hideSmsMessage(sms) },
                                    isSendingSms = isSendingSms,
                                    isHidingSms = isHidingSms
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            // Permission Request Section
            if (!smsPermissionState.status.isGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.permission_required),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = stringResource(R.string.permission_rationale),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Button(
                            onClick = { smsPermissionState.launchPermissionRequest() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(R.string.grant_permission))
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun SmsItem(
    sms: `in`.kc31.expensetrack.data.model.SmsData,
    onSendClick: () -> Unit,
    onHideClick: () -> Unit,
    isSendingSms: String?,
    isHidingSms: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.from, sms.sender),
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Show timestamp
                    Text(
                        text = SmsUtils.formatTimestamp(sms.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
                // Hide button
                TextButton(
                    onClick = onHideClick,
                    enabled = (isHidingSms == null || isHidingSms != sms.id) &&
                             (isSendingSms == null || isSendingSms != sms.id)
                ) {
                    if (isHidingSms == sms.id) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(stringResource(R.string.hide))
                }

                // Send button
                TextButton(
                    onClick = onSendClick,
                    enabled = (isSendingSms == null || isSendingSms != sms.id) &&
                             (isHidingSms == null || isHidingSms != sms.id)
                ) {
                    if (isSendingSms == sms.id) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(stringResource(R.string.send))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = sms.body,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
