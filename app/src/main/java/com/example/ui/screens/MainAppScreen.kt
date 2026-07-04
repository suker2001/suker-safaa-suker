package com.example.ui.screens
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.geometry.Offset

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.network.WhatsAppService
import com.example.ui.viewmodel.DayStat
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DebtViewModel
import com.example.ui.viewmodel.PendingWhatsAppSend
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainAppScreen(viewModel: DebtViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // UI States
    val currentUser by viewModel.currentUser.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val subscribers by viewModel.subscribers.collectAsState()
    val selectedSubscriber by viewModel.selectedSubscriber.collectAsState()
    val selectedTransactions by viewModel.selectedSubscriberTransactions.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val logs by viewModel.activityLogs.collectAsState()
    val whatsappLogs by viewModel.whatsappLogs.collectAsState()
    val userAccounts by viewModel.userAccounts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()
    var activeTab by remember { mutableIntStateOf(0) }

    // Dialog Toggles
    var showAddSubscriberDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var transactionTypeToAdd by remember { mutableStateOf("DEBT") } // DEBT or PAYMENT
    var showEditSubscriberDialog by remember { mutableStateOf(false) }
    var showWhatsAppPromptDialog by remember { mutableStateOf<PendingWhatsAppSend?>(null) }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.whatsAppPendingPrompt.collectLatest { prompt ->
            showWhatsAppPromptDialog = prompt
        }
    }

    // Force RTL layout globally for professional Arabic alignment
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (settings.isPinEnabled && currentUser == null) {
                // PIN Lock Login Screen
                LoginScreen(
                    userAccounts = userAccounts,
                    onLoginAttempt = { username, pin ->
                        viewModel.login(username, pin) { success ->
                            if (success) {
                                val role = viewModel.currentUser.value?.role
                                activeTab = when(role) {
                                    "DEVELOPER" -> 6
                                    "ADMIN" -> 0
                                    "EMPLOYEE" -> 1
                                    "USER" -> 1
                                    else -> 0
                                }
                            }
                        }
                    }
                )
            } else {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "القائمة الرئيسية",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 24.dp)
                                )
                                NavigationDrawerItem(
                                    label = { Text("الرئيسية", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                                    selected = activeTab == 0 && selectedSubscriber == null,
                                    onClick = { 
                                        activeTab = 0 
                                        viewModel.selectSubscriber(null)
                                        scope.launch { drawerState.close() }
                                    }
                                )
                                NavigationDrawerItem(
                                    label = { Text("المشتركين", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                                    selected = activeTab == 1 && selectedSubscriber == null,
                                    onClick = { 
                                        activeTab = 1 
                                        viewModel.selectSubscriber(null)
                                        scope.launch { drawerState.close() }
                                    }
                                )
                                NavigationDrawerItem(
                                    label = { Text("سجل النشاطات", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                                    selected = activeTab == 2 && selectedSubscriber == null,
                                    onClick = { 
                                        activeTab = 2 
                                        viewModel.selectSubscriber(null)
                                        scope.launch { drawerState.close() }
                                    }
                                )
                                
                                NavigationDrawerItem(
                                    label = { 
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("الإشعارات", fontWeight = FontWeight.Bold)
                                            if (unreadNotificationCount > 0) {
                                                Badge(containerColor = MaterialTheme.colorScheme.error) { Text(unreadNotificationCount.toString()) }
                                            }
                                        } 
                                    },
                                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                                    selected = activeTab == 5 && selectedSubscriber == null,
                                    onClick = { 
                                        activeTab = 5 
                                        viewModel.selectSubscriber(null)
                                        scope.launch { drawerState.close() }
                                    }
                                )
                                NavigationDrawerItem(
                                    label = { Text("التقارير المالية", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                                    selected = activeTab == 4 && selectedSubscriber == null,
                                    onClick = { 
                                        activeTab = 4 
                                        viewModel.selectSubscriber(null)
                                        scope.launch { drawerState.close() }
                                    }
                                )
                                NavigationDrawerItem(
                                    label = { Text("صحة النظام", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.HealthAndSafety, contentDescription = null) },
                                    selected = activeTab == 6 && selectedSubscriber == null,
                                    onClick = { 
                                        activeTab = 6 
                                        viewModel.selectSubscriber(null)
                                        scope.launch { drawerState.close() }
                                    }
                                )
                                NavigationDrawerItem(
                                    label = { Text("الإعدادات", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    selected = activeTab == 3 && selectedSubscriber == null,
                                    onClick = { 
                                        activeTab = 3 
                                        viewModel.selectSubscriber(null)
                                        scope.launch { drawerState.close() }
                                    }
                                )
                            }
                        }
                    }
                ) {
                    // Handle Back Button
                    val isMainPage = activeTab == 0 && selectedSubscriber == null
                    
                    val onBack: () -> Unit = {
                        if (selectedSubscriber != null) {
                            viewModel.selectSubscriber(null)
                        } else if (activeTab != 0) {
                            activeTab = 0
                        }
                    }

                    androidx.activity.compose.BackHandler(enabled = !isMainPage) {
                        onBack()
                    }

                    // Main Application Shell
                    Scaffold(
                        topBar = {
                            TopAppBarContent(
                                activeTab = activeTab,
                                currentUser = currentUser,
                                selectedSubscriber = selectedSubscriber,
                                networkStatus = networkStatus,
                                unreadNotificationCount = unreadNotificationCount,
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onBackClick = onBack,
                                onLogoutClick = { viewModel.logout() },
                                onNotificationsClick = { activeTab = 5; viewModel.selectSubscriber(null) }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                        if (selectedSubscriber != null) {
                            // Detail panel has its own visual stack
                            SubscriberDetailView(
                                subscriber = selectedSubscriber!!,
                                transactions = selectedTransactions,
                                currentUser = currentUser,
                                onBack = { viewModel.selectSubscriber(null) },
                                onAddTransaction = { type ->
                                    transactionTypeToAdd = type
                                    showAddTransactionDialog = true
                                },
                                onDeleteTransaction = { txId ->
                                    viewModel.deleteTransaction(txId)
                                },
                                onEditTransaction = { txId, amount, notes ->
                                    viewModel.updateTransaction(txId, amount, notes)
                                },
                                onEditSubscriber = {
                                    showEditSubscriberDialog = true
                                },
                                onWhatsAppClick = {
                                    val renderedMsg = "مرحباً ${selectedSubscriber!!.name}، إجمالي الرصيد المتبقي عليك هو ${com.example.utils.FormatHelper.formatCurrency(selectedSubscriber!!.remainingDebt)}."
                                    viewModel.triggerManualWhatsApp(context, selectedSubscriber!!.phone, renderedMsg)
                                }
                            )
                        } else {
                            // Standard Tabs
                            when (activeTab) {
                                0 -> DashboardScreen(
                                    stats = stats,
                                    transactions = selectedTransactions,
                                    onNavigateToSubscribers = { activeTab = 1 },
                                    onAddSubscriberClick = { showAddSubscriberDialog = true },
                                    onAddTransactionClick = {
                                        // Usually transaction requires a selected subscriber, 
                                        // so we might need a general selector or just navigate to subscribers
                                        activeTab = 1 
                                    },
                                    onWhatsAppClick = { activeTab = 1 },
                                    onReportsClick = { activeTab = 4 },
                                    onSearchClick = { activeTab = 1 }
                                )
                                5 -> NotificationCenterScreen(
                                    viewModel = viewModel
                                )
                                6 -> SystemHealthScreen(
                                    viewModel = viewModel
                                )
                                4 -> ReportsScreen(
                                    stats = stats,
                                    subscribers = subscribers
                                )
                                1 -> SubscribersScreen(
                                    subscribers = subscribers,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    onSubscriberSelect = { viewModel.selectSubscriber(it.id) },
                                    onAddSubscriberClick = { showAddSubscriberDialog = true }
                                )
                                2 -> AuditLogsScreen(
                                    logs = logs,
                                    whatsappLogs = whatsappLogs,
                                    currentUser = currentUser,
                                    onClearLogs = { viewModel.clearAllLogs() }
                                )
                                3 -> SettingsScreen(
                                    viewModel = viewModel,
                                    settings = settings,
                                    userAccounts = userAccounts,
                                    currentUser = currentUser,
                                    onSettingsSave = { viewModel.updateSettings(it) },
                                    onSaveUser = { viewModel.saveUserAccount(it) },
                                    onDeleteUser = { viewModel.deleteUserAccount(it) },
                                    onExport = {
                                        viewModel.exportBackup(context) { json ->
                                            if (json != null) {
                                                clipboardManager.setText(AnnotatedString(json))
                                                Toast.makeText(context, "تم نسخ نسخة البيانات الاحتياطية بنجاح إلى الحافظة!", Toast.LENGTH_LONG).show()
                                                // Trigger system share sheet
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "application/json"
                                                    putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية - سجل الديون")
                                                    putExtra(Intent.EXTRA_TEXT, json)
                                                }
                                                context.startActivity(Intent.createChooser(intent, "تصدير نسخة احتياطية"))
                                            }
                                        }
                                    },
                                    onImport = { json ->
                                        viewModel.importBackup(json) { success ->
                                            if (success) {
                                                activeTab = 0
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        // --- Dialogue Boxes ---

                        if (showAddSubscriberDialog) {
                            AddSubscriberDialog(
                                onDismiss = { showAddSubscriberDialog = false },
                                onConfirm = { name, phone, address, notes, emoji, status, dueDate ->
                                    viewModel.addSubscriber(name, phone, address, notes, emoji, status, dueDate)
                                    showAddSubscriberDialog = false
                                }
                            )
                        }

                        if (showEditSubscriberDialog && selectedSubscriber != null) {
                            EditSubscriberDialog(
                                subscriber = selectedSubscriber!!,
                                onDismiss = { showEditSubscriberDialog = false },
                                onConfirm = { updatedSub ->
                                    viewModel.updateSubscriber(updatedSub)
                                    showEditSubscriberDialog = false
                                },
                                onDelete = {
                                    viewModel.deleteSubscriber(selectedSubscriber!!)
                                    showEditSubscriberDialog = false
                                }
                            )
                        }

                        if (showAddTransactionDialog && selectedSubscriber != null) {
                            AddTransactionDialog(
                                type = transactionTypeToAdd,
                                subscriberName = selectedSubscriber!!.name,
                                onDismiss = { showAddTransactionDialog = false },
                                onConfirm = { amount, notes ->
                                    viewModel.addTransaction(selectedSubscriber!!.id, transactionTypeToAdd, amount, notes)
                                    showAddTransactionDialog = false
                                }
                            )
                        }

                        if (showWhatsAppPromptDialog != null) {
                            WhatsAppPromptDialog(
                                pendingSend = showWhatsAppPromptDialog!!,
                                onDismiss = { showWhatsAppPromptDialog = null },
                                onConfirm = {
                                    viewModel.triggerManualWhatsApp(
                                        context,
                                        showWhatsAppPromptDialog!!.phone,
                                        showWhatsAppPromptDialog!!.message
                                    )
                                    showWhatsAppPromptDialog = null
                                }
                            )
                        }
                    }
                }
                } // End ModalNavigationDrawer
            }
        }
    }
}

// --- TOP APP BAR ---


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarContent(
    activeTab: Int,
    currentUser: com.example.data.model.UserAccount?,
    selectedSubscriber: com.example.data.model.Subscriber?,
    networkStatus: com.example.data.network.NetworkStatus,
    unreadNotificationCount: Int,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    val title = when {
        selectedSubscriber != null -> selectedSubscriber.name
        activeTab == 0 -> "اللوحة الرئيسية"
        activeTab == 1 -> "المشتركين"
        activeTab == 2 -> "سجل النشاطات"
        activeTab == 3 -> "إعدادات النظام"
        activeTab == 4 -> "التقارير المالية"
        activeTab == 5 -> "مركز الإشعارات"
        activeTab == 6 -> "مركز صحة النظام"
        else -> "وصل"
    }

    val isMainPage = selectedSubscriber == null

    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (isMainPage) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            } else {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (networkStatus == com.example.data.network.NetworkStatus.Unavailable) {
                Icon(
                    Icons.Default.CloudOff,
                    contentDescription = "Offline",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
            if (activeTab != 5) {
                Box(modifier = Modifier.padding(end = 8.dp)) {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    if (unreadNotificationCount > 0) {
                        Badge(
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                            containerColor = MaterialTheme.colorScheme.error
                        ) {
                            Text(unreadNotificationCount.toString())
                        }
                    }
                }
            }
            if (currentUser != null) {
                IconButton(onClick = onLogoutClick) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout")
                }
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        currentUser.fullName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    userAccounts: List<com.example.data.model.UserAccount>,
    onLoginAttempt: (String, String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") } // Used as username
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var hasAttemptedLogin by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Re-enable button/loading if phone number or password changes
    LaunchedEffect(phoneNumber, password) {
        isLoading = false
        hasAttemptedLogin = false
    }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            kotlinx.coroutines.delay(2000)
            isLoading = false
            hasAttemptedLogin = true
            // Show error toast if not navigated away
            Toast.makeText(context, "رقم الهاتف أو كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show()
        }
    }

    val primaryDarkBlue = Color(0xFF0F172A)
    val emeraldGreen = Color(0xFF10B981)
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    val bgColor = if (isDark) Color(0xFF0B1120) else Color(0xFFF1F5F9)
    val cardColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f)
    val textColor = if (isDark) Color.White else primaryDarkBlue
    val textSecondaryColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Validation
    val isPhoneValid = phoneNumber.isNotEmpty()
    val isPasswordValid = password.length >= 4

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        val isDesktop = maxWidth > 800.dp

        // Background shapes
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = emeraldGreen.copy(alpha = 0.15f),
                radius = size.width * 0.4f,
                center = Offset(size.width * 0.1f, size.height * 0.1f)
            )
            drawCircle(
                color = primaryDarkBlue.copy(alpha = if (isDark) 0.3f else 0.05f),
                radius = size.width * 0.5f,
                center = Offset(size.width * 0.8f, size.height * 0.8f)
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Left Panel (Brand Area) - Visible only on Desktop
            if (isDesktop) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(primaryDarkBlue, Color(0xFF1E293B)),
                                start = Offset.Zero,
                                end = Offset(0f, 2000f)
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(48.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = emeraldGreen,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "وصل",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "إدارة ديونك باحتراف",
                            fontSize = 24.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right Panel (Login Card)
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .padding(horizontal = 24.dp)
                            .shadow(
                                elevation = 32.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = primaryDarkBlue.copy(alpha = 0.2f)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp, vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (!isDesktop) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(emeraldGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = emeraldGreen,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            Text(
                                text = "مرحبًا بك، سجّل الدخول لإدارة ديونك باحتراف.",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 32.sp
                            )

                            Spacer(modifier = Modifier.height(48.dp))

                            // Phone Field
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("اسم المستخدم / رقم الهاتف") },
                                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                                
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                isError = hasAttemptedLogin && !isPhoneValid,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = emeraldGreen,
                                    focusedLabelColor = emeraldGreen,
                                    unfocusedBorderColor = if(isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    errorLabelColor = MaterialTheme.colorScheme.error,
                                )
                            )
                            if (hasAttemptedLogin && !isPhoneValid) {
                                Text("يرجى إدخال اسم مستخدم أو رقم هاتف صحيح", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp, start = 16.dp))
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Password Field
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("كلمة المرور") },
                                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                isError = hasAttemptedLogin && !isPasswordValid,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = emeraldGreen,
                                    focusedLabelColor = emeraldGreen,
                                    unfocusedBorderColor = if(isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    errorLabelColor = MaterialTheme.colorScheme.error,
                                )
                            )
                            if (hasAttemptedLogin && !isPasswordValid) {
                                Text("كلمة المرور قصيرة جداً", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).padding(top = 4.dp, start = 16.dp))
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { rememberMe = !rememberMe }
                                        .padding(end = 8.dp)
                                ) {
                                    Checkbox(
                                        checked = rememberMe,
                                        onCheckedChange = { rememberMe = it },
                                        colors = CheckboxDefaults.colors(checkedColor = emeraldGreen)
                                    )
                                    Text("تذكرني", fontSize = 14.sp, color = textSecondaryColor)
                                }
                                TextButton(onClick = { /* TODO Forgot Password */ }) {
                                    Text("نسيت كلمة المرور؟", color = emeraldGreen, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))
                            
                            // Google Sign-In Button
                            Button(
                                onClick = { /* TODO: Implement Google Sign-In */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .border(1.dp, if(isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = textColor
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle, // Placeholder
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("تسجيل الدخول عبر Google", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            // Login Button
                            Button(
                                onClick = {
                                    if (isPhoneValid && isPasswordValid) {
                                        isLoading = true
                                        onLoginAttempt(phoneNumber, password)
                                    } else {
                                        hasAttemptedLogin = true
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        spotColor = emeraldGreen.copy(alpha = 0.5f)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = emeraldGreen,
                                    disabledContainerColor = emeraldGreen.copy(alpha = 0.6f)
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    Text("تسجيل الدخول", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Biometric Button
                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "تسجيل الدخول بالبصمة غير مفعل حالياً", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, if(isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = textColor, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("تسجيل الدخول بالبصمة", color = textColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Footer
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الإصدار 1.0.0", fontSize = 12.sp, color = textSecondaryColor)
                        Text(" • ", fontSize = 12.sp, color = textSecondaryColor)
                        Text("سياسة الخصوصية", fontSize = 12.sp, color = emeraldGreen, modifier = Modifier.clickable { })
                        Text(" • ", fontSize = 12.sp, color = textSecondaryColor)
                        Text("شروط الاستخدام", fontSize = 12.sp, color = emeraldGreen, modifier = Modifier.clickable { })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("© 2026 جميع الحقوق محفوظة", fontSize = 12.sp, color = textSecondaryColor)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// --- TAB 0: DASHBOARD SCREEN ---

@Composable
fun DashboardScreen(
    stats: com.example.ui.viewmodel.DashboardStats,
    transactions: List<FinancialTransaction>,
    onNavigateToSubscribers: () -> Unit,
    onAddSubscriberClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onReportsClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Welcome Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background decorative circle (Clean Minimalism)
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.1f),
                        radius = size.width * 0.4f,
                        center = androidx.compose.ui.geometry.Offset(x = size.width, y = 0f)
                    )
                }
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "لوحة التحكم المالية والملخص",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "متابعة الحسابات، مجموع الديون، التحصيلات اليومية ومستويات الالتزام.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Summary Statistics Cards (2x2 Grid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "إجمالي المشتركين",
                value = stats.totalSubscribers.toString(),
                unit = "مشترك",
                color = MaterialTheme.colorScheme.primary,
                icon = Icons.Default.Group,
                onClick = onNavigateToSubscribers
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "إجمالي الديون",
                value = com.example.utils.FormatHelper.formatCurrency(stats.totalDebts),
                
                color = DebtRed,
                icon = Icons.Default.TrendingUp,
                onClick = {}
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "إجمالي التحصيلات",
                value = com.example.utils.FormatHelper.formatCurrency(stats.totalPaid),
                
                color = PaymentGreen,
                icon = Icons.Default.CheckCircle,
                onClick = {}
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "المبالغ المتبقية",
                value = com.example.utils.FormatHelper.formatCurrency(stats.totalRemaining),
                
                color = MaterialTheme.colorScheme.secondary,
                icon = Icons.Default.AccountBalanceWallet,
                onClick = {}
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "الديون المتأخرة",
                value = com.example.utils.FormatHelper.formatCurrency(stats.lateDebtsSum),
                
                color = AccentOrange,
                icon = Icons.Default.AccessTime,
                onClick = {}
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "المشتركين المتأخرين",
                value = stats.lateSubscribersCount.toString(),
                unit = "مشترك",
                color = DebtRed,
                icon = Icons.Default.Warning,
                onClick = onNavigateToSubscribers
            )
        }

        // Quick Actions Section
        Text(
            text = "إجراءات سريعة",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Grid of Quick Actions
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "إضافة مشترك",
                    icon = Icons.Default.PersonAdd,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onAddSubscriberClick
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "إضافة دين",
                    icon = Icons.Default.AddShoppingCart,
                    color = DebtRed,
                    onClick = onAddTransactionClick
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "تسجيل تسديد",
                    icon = Icons.Default.Payments,
                    color = PaymentGreen,
                    onClick = onAddTransactionClick
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "إرسال واتساب",
                    icon = Icons.Default.Message,
                    color = Color(0xFF25D366), // WhatsApp Green
                    onClick = onWhatsAppClick
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "التقارير",
                    icon = Icons.Default.Assessment,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = onReportsClick
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "البحث",
                    icon = Icons.Default.Search,
                    color = AccentPurple,
                    onClick = onSearchClick
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String = "",
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

// --- TAB 1: SUBSCRIBERS DIRECTORY SCREEN ---

@Composable
fun SubscribersScreen(
    subscribers: List<Subscriber>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSubscriberSelect: (Subscriber) -> Unit,
    onAddSubscriberClick: () -> Unit
) {
    var filterStatus by remember { mutableStateOf("ALL") } // ALL, ACTIVE, SUSPENDED

    val filteredList = remember(subscribers, filterStatus) {
        when (filterStatus) {
            "ACTIVE" -> subscribers.filter { it.status == "ACTIVE" }
            "SUSPENDED" -> subscribers.filter { it.status == "SUSPENDED" }
            else -> subscribers
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("الاسم، الهاتف، الكود، المنطقة، أو المبلغ...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filterStatus == "ALL",
                        onClick = { filterStatus = "ALL" },
                        label = { Text("الكل (${subscribers.size})") }
                    )
                    FilterChip(
                        selected = filterStatus == "ACTIVE",
                        onClick = { filterStatus = "ACTIVE" },
                        label = { Text("النشطين (${subscribers.count { it.status == "ACTIVE" }})") }
                    )
                    FilterChip(
                        selected = filterStatus == "SUSPENDED",
                        onClick = { filterStatus = "SUSPENDED" },
                        label = { Text("الموقوفين (${subscribers.count { it.status == "SUSPENDED" }})") }
                    )
                }
                
                val context = LocalContext.current
                IconButton(onClick = {
                    com.example.utils.ExportHelper.exportAllDebtsCsv(context, subscribers)
                }) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "تصدير كشف الديون",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Subscribers List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لم يتم العثور على مشتركين",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "اضغط على زر (+) في الأسفل لإضافة مشترك جديد",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList) { subscriber ->
                        SubscriberCard(
                            subscriber = subscriber,
                            onClick = { onSubscriberSelect(subscriber) }
                        )
                    }
                }
            }
        }

        // FAB to add subscriber
        FloatingActionButton(
            onClick = onAddSubscriberClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة مشترك", tint = Color.White)
        }
    }
}

@Composable
fun SubscriberCard(
    subscriber: Subscriber,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Emoji Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subscriber.avatarEmoji,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = subscriber.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (subscriber.status == "ACTIVE") SoftPaymentGreen else SoftDebtRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (subscriber.status == "ACTIVE") "نشط" else "موقف",
                            color = if (subscriber.status == "ACTIVE") PaymentGreen else DebtRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = subscriber.uniqueCode,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = subscriber.phone,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Debt balance display
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "المتبقي عليه",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = com.example.utils.FormatHelper.formatCurrency(subscriber.remainingDebt),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (subscriber.remainingDebt > 0) DebtRed else PaymentGreen
                )
                if (subscriber.remainingDebt > 0 && subscriber.nextDueDate != null) {
                    val days = ((subscriber.nextDueDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).coerceAtLeast(0)
                    Text(
                        text = "يستحق بعد $days يوم",
                        fontSize = 10.sp,
                        color = if (days <= 3) DebtRed else AccentOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- SUBSCRIBER LEDGER DETAIL VIEW ---

@Composable
fun SubscriberDetailView(
    subscriber: Subscriber,
    transactions: List<FinancialTransaction>,
    currentUser: UserAccount?,
    onBack: () -> Unit,
    onAddTransaction: (String) -> Unit,
    onDeleteTransaction: (Int) -> Unit,
    onEditTransaction: (Int, Double, String) -> Unit,
    onEditSubscriber: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    val context = LocalContext.current
    var activeTxToEdit by remember { mutableStateOf<FinancialTransaction?>(null) }
    var showEditTxDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper Profile details panel
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = subscriber.avatarEmoji, fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subscriber.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "رمز المشترك: ${subscriber.uniqueCode}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "العنوان: ${subscriber.address.ifBlank { "غير مسجل" }}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // Actions (Export and Edit)
                    Row {
                        IconButton(onClick = {
                            com.example.utils.ExportHelper.exportSubscriberStatementCsv(context, subscriber, transactions)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "تصدير كشف حساب",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(onClick = onEditSubscriber) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل المشترك",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (subscriber.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ملاحظات: ${subscriber.notes}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.onSurface
                                    .copy(alpha = 0.05f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Direct Communication Shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${subscriber.phone}"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اتصال هاتفي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Ledger Statement Balance Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "الحالة المالية للحساب",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("إجمالي الديون القائمة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        Text(com.example.utils.FormatHelper.formatCurrency(subscriber.totalDebt), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DebtRed)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("إجمالي المسدد", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        Text(com.example.utils.FormatHelper.formatCurrency(subscriber.totalPaid), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PaymentGreen)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("المبلغ المتبقي للتحصيل", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        Text(com.example.utils.FormatHelper.formatCurrency(subscriber.remainingDebt), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if (subscriber.remainingDebt > 0) DebtRed else PaymentGreen)
                    }
                }
            }
        }

        // Core Financial Action Buttons (Huge target size)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onAddTransaction("DEBT") },
                colors = ButtonDefaults.buttonColors(containerColor = DebtRed),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة دين", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                onClick = { onAddTransaction("PAYMENT") },
                colors = ButtonDefaults.buttonColors(containerColor = PaymentGreen),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تسجيل تسديد", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onWhatsAppClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Message, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("إرسال واتساب", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                onClick = onEditSubscriber,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تعديل المشترك", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // Log Timeline of past transactions
        Text(
            text = "سجل الحركات المالية المتبادلة",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد عمليات مسجلة لهذا المشترك بعد.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                transactions.forEach { tx ->
                    TransactionItem(
                        tx = tx,
                        currentUser = currentUser,
                        onEdit = {
                            activeTxToEdit = tx
                            showEditTxDialog = true
                        },
                        onDelete = {
                            onDeleteTransaction(tx.id)
                        }
                    )
                }
            }
        }
    }

    // Edit Transaction Dialog
    if (showEditTxDialog && activeTxToEdit != null) {
        EditTransactionDialog(
            tx = activeTxToEdit!!,
            onDismiss = {
                showEditTxDialog = false
                activeTxToEdit = null
            },
            onConfirm = { amount, notes ->
                onEditTransaction(activeTxToEdit!!.id, amount, notes)
                showEditTxDialog = false
                activeTxToEdit = null
            }
        )
    }
}

@Composable
fun TransactionItem(
    tx: FinancialTransaction,
    currentUser: UserAccount?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDebt = tx.type == "DEBT"
    val dateStr = com.example.utils.FormatHelper.formatDateTime(tx.timestamp)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isDebt) SoftDebtRed else SoftPaymentGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDebt) Icons.Default.TrendingUp else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isDebt) DebtRed else PaymentGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = if (isDebt) "إضافة دين" else "تسجيل تسديد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isDebt) DebtRed else PaymentGreen
                    )

                    if (tx.isModified) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("معدل", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = "${if (isDebt) "+" else "-"}${com.example.utils.FormatHelper.formatCurrency(tx.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isDebt) DebtRed else PaymentGreen
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (tx.notes.isNotBlank()) {
                Text(
                    text = tx.notes,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "التاريخ: $dateStr • المدخل: ${tx.createdBy}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                // Permissions Action Panel (Only Admin can modify / delete)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if ((currentUser?.role == "ADMIN" || currentUser?.role == "DEVELOPER")) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        // Display locking info for employee audit
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "التعديل للمدير فقط",
                            tint = Color.LightGray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Display historical modification edits logs if any
            if (tx.isModified && tx.modificationHistory.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "تاريخ التعديلات:\n${tx.modificationHistory.trim()}",
                    fontSize = 8.sp,
                    lineHeight = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                )
            }
        }
    }
}

// --- TAB 2: AUDIT LOGS SCREEN ---

@Composable
fun AuditLogsScreen(
    logs: List<ActivityLog>,
    whatsappLogs: List<com.example.data.model.PendingWhatsAppMessage>,
    currentUser: UserAccount?,
    onClearLogs: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showConfirmClearDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    val filteredLogs = remember(logs, searchQuery) {
        if (searchQuery.isBlank()) logs
        else logs.filter { it.action.contains(searchQuery, ignoreCase = true) || it.username.contains(searchQuery, ignoreCase = true) }
    }

    val filteredWhatsAppLogs = remember(whatsappLogs, searchQuery) {
        if (searchQuery.isBlank()) whatsappLogs
        else whatsappLogs.filter { it.subscriberName.contains(searchQuery, ignoreCase = true) || it.message.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "سجل النشاطات",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Clear Log option only for administrators
            if ((currentUser?.role == "ADMIN" || currentUser?.role == "DEVELOPER") && selectedTabIndex == 0) {
                Button(
                    onClick = { showConfirmClearDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مسح السجل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        androidx.compose.material3.TabRow(selectedTabIndex = selectedTabIndex) {
            androidx.compose.material3.Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("النظام والأمان") }
            )
            androidx.compose.material3.Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("رسائل واتساب") }
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث في السجل...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        if (selectedTabIndex == 0) {
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "سجل النشاطات فارغ.", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredLogs) { log ->
                    val dateStr = com.example.utils.FormatHelper.formatDateTime(log.timestamp)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "المستخدم: ${log.username} (${if (log.role == "ADMIN") "أدمن" else "موظف"})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = dateStr,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = log.action,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } // end LazyColumn
        } // end else for filteredLogs
        } else {
            if (filteredWhatsAppLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "لا توجد رسائل واتساب.", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredWhatsAppLogs) { log ->
                        val dateString = com.example.utils.FormatHelper.formatDateTime(log.timestamp)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = log.subscriberName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    val statusColor = when(log.status) {
                                        com.example.data.model.SyncStatus.SYNCED -> Color(0xFF4CAF50)
                                        com.example.data.model.SyncStatus.FAILED -> Color(0xFFE53935)
                                        com.example.data.model.SyncStatus.PENDING -> Color(0xFFFF9800)
                                    }
                                    val statusText = when(log.status) {
                                        com.example.data.model.SyncStatus.SYNCED -> "تم الإرسال"
                                        com.example.data.model.SyncStatus.FAILED -> "فشل (${log.retryCount})"
                                        com.example.data.model.SyncStatus.PENDING -> "قيد الانتظار (${log.retryCount})"
                                    }
                                    Text(text = statusText, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = log.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "التاريخ: $dateString", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = { Text("تأكيد مسح السجل") },
            text = { Text("هل أنت متأكد من رغبتك في مسح سجل العمليات والنشاطات بالكامل؟ لا يمكن التراجع عن هذه الخطوة.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearLogs()
                        showConfirmClearDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("مسح الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SettingsSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// --- TAB 4: REPORTS SCREEN ---

@Composable
fun ReportsScreen(
    stats: com.example.ui.viewmodel.DashboardStats,
    subscribers: List<Subscriber>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("التقارير المالية المفصلة", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("إحصائيات عامة", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                ReportRow("إجمالي مبلغ الديون", com.example.utils.FormatHelper.formatCurrency(stats.totalDebts), DebtRed)
                ReportRow("إجمالي التحصيلات", com.example.utils.FormatHelper.formatCurrency(stats.totalPaid), PaymentGreen)
                ReportRow("صافي المبالغ المتبقية", com.example.utils.FormatHelper.formatCurrency(stats.totalRemaining), MaterialTheme.colorScheme.primary)
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                ReportRow("عدد المشتركين الكلي", stats.totalSubscribers.toString(), Color.Gray)
                ReportRow("عدد المتأخرين عن السداد", stats.lateSubscribersCount.toString(), AccentOrange)
            }
        }

        Text("أعلى المدينين قيمة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        val topDebtors = subscribers.sortedByDescending { it.remainingDebt }.take(5)
        topDebtors.forEach { sub ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(sub.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("رقم الهاتف: ${sub.phone}", fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(
                        com.example.utils.FormatHelper.formatCurrency(sub.remainingDebt),
                        fontWeight = FontWeight.Bold,
                        color = DebtRed,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ReportRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 13.sp)
    }
}

// --- TAB 3: SETTINGS SCREEN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DebtViewModel,
    settings: AppSettings,
    userAccounts: List<UserAccount>,
    currentUser: UserAccount?,
    onSettingsSave: (AppSettings) -> Unit,
    onSaveUser: (UserAccount) -> Unit,
    onDeleteUser: (UserAccount) -> Unit,
    onExport: () -> Unit,
    onImport: (String) -> Unit
) {
    // Local edits holders
    var isWhatsAppEnabled by remember(settings) { mutableStateOf(settings.isWhatsAppEnabled) }
    var whatsAppApiType by remember(settings) { mutableStateOf(settings.whatsAppApiType) } // MANUAL, ULTRAMSG, TWILIO
    var whatsAppInstanceId by remember(settings) { mutableStateOf(settings.whatsAppInstanceId) }
    var whatsAppToken by remember(settings) { mutableStateOf(settings.whatsAppToken) }
    var whatsAppSid by remember(settings) { mutableStateOf(settings.whatsAppSid) }
    var whatsAppTemplateDebt by remember(settings) { mutableStateOf(settings.whatsAppTemplateDebt) }
    var whatsAppTemplatePayment by remember(settings) { mutableStateOf(settings.whatsAppTemplatePayment) }
    var isPinEnabled by remember(settings) { mutableStateOf(settings.isPinEnabled) }
    var notifyPayments by remember(settings) { mutableStateOf(settings.notifyPayments) }
    var notifyDueDates by remember(settings) { mutableStateOf(settings.notifyDueDates) }
    var notifyWhatsApp by remember(settings) { mutableStateOf(settings.notifyWhatsApp) }
    var notifySecurity by remember(settings) { mutableStateOf(settings.notifySecurity) }
    var notifyEmployeeActivities by remember(settings) { mutableStateOf(settings.notifyEmployeeActivities) }
    var notifySystem by remember(settings) { mutableStateOf(settings.notifySystem) }

    var importTextPaste by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }

    // User management modal state
    var showUserAccountDialog by remember { mutableStateOf(false) }
    var editingUserAccount by remember { mutableStateOf<UserAccount?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "إعدادات التطبيق العامة والربط",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // 1. WhatsApp Configuration Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("إعدادات رسائل واتساب التلقائية", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تفعيل إشعارات واتساب", fontSize = 13.sp)
                    Switch(
                        checked = isWhatsAppEnabled,
                        onCheckedChange = { isWhatsAppEnabled = it }
                    )
                }

                if (isWhatsAppEnabled) {
                    Text("بوابة الإرسال ومزود الخدمة API", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("MANUAL", "تطبيق واتساب اليدوي", Icons.Default.ChatBubbleOutline),
                            Triple("ULTRAMSG", "بوابة UltraMsg التلقائية", Icons.Default.Send),
                            Triple("TWILIO", "بوابة Twilio للأعمال", Icons.Default.Language)
                        ).forEach { (type, label, icon) ->
                            val selected = whatsAppApiType == type
                            OutlinedButton(
                                onClick = { whatsAppApiType = type },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.LightGray),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(label, fontSize = 9.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (whatsAppApiType == "ULTRAMSG") {
                        OutlinedTextField(
                            value = whatsAppInstanceId,
                            onValueChange = { whatsAppInstanceId = it },
                            label = { Text("معرف المثيل الخاص بـ UltraMsg") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = whatsAppToken,
                            onValueChange = { whatsAppToken = it },
                            label = { Text("رمز التوثيق") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else if (whatsAppApiType == "TWILIO") {
                        OutlinedTextField(
                            value = whatsAppSid,
                            onValueChange = { whatsAppSid = it },
                            label = { Text("معرف الحساب Twilio") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = whatsAppToken,
                            onValueChange = { whatsAppToken = it },
                            label = { Text("رمز المرور Twilio") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Template configurations
                    Text("قوالب صياغة الرسائل", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = whatsAppTemplateDebt,
                        onValueChange = { whatsAppTemplateDebt = it },
                        label = { Text("رسالة تسجيل دين جديد") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = whatsAppTemplatePayment,
                        onValueChange = { whatsAppTemplatePayment = it },
                        label = { Text("رسالة تأكيد دفعة مسددة") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    // Placeholders list helper description
                    Text(
                        text = "الكلمات الدلالية المتاحة: \n{name} اسم المشترك | {amount} مبلغ العملية | {total} مجموع الديون | {total_paid} إجمالي المسدد | {remaining} المتبقي | {date} التاريخ | {time} الوقت",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }
            }
        }

                // Notification Settings UI
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("إعدادات الإشعارات والتنبيهات", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                
                SettingsSwitch("تنبيهات الدفعات والديون", notifyPayments) { notifyPayments = it }
                SettingsSwitch("تنبيهات مواعيد الاستحقاق", notifyDueDates) { notifyDueDates = it }
                SettingsSwitch("حالة إرسال واتساب", notifyWhatsApp) { notifyWhatsApp = it }
                SettingsSwitch("تنبيهات أمان الحساب", notifySecurity) { notifySecurity = it }
                SettingsSwitch("نشاطات الموظفين (إضافة/تعديل/حذف)", notifyEmployeeActivities) { notifyEmployeeActivities = it }
                SettingsSwitch("تنبيهات النظام والمزامنة", notifySystem) { notifySystem = it }
            }
        }

        // 2. PIN Security Lock Options
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("إعدادات الأمان وقفل الشاشة", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("طلب رمز PIN عند فتح التطبيق", fontSize = 13.sp)
                    Switch(
                        checked = isPinEnabled,
                        onCheckedChange = { isPinEnabled = it }
                    )
                }
            }
        }

        // Save Settings button
        Button(
            onClick = {
                onSettingsSave(
                    settings.copy(
                        isWhatsAppEnabled = isWhatsAppEnabled,
                        whatsAppApiType = whatsAppApiType,
                        whatsAppInstanceId = whatsAppInstanceId,
                        whatsAppToken = whatsAppToken,
                        whatsAppSid = whatsAppSid,
                        whatsAppTemplateDebt = whatsAppTemplateDebt,
                        whatsAppTemplatePayment = whatsAppTemplatePayment,
                        isPinEnabled = isPinEnabled,
                        notifyPayments = notifyPayments,
                        notifyDueDates = notifyDueDates,
                        notifyWhatsApp = notifyWhatsApp,
                        notifySecurity = notifySecurity,
                        notifyEmployeeActivities = notifyEmployeeActivities,
                        notifySystem = notifySystem
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("حفظ كافة الإعدادات والربط", fontWeight = FontWeight.Bold)
        }

        // 3. User accounts management (Only viewable by admin)
        if ((currentUser?.role == "ADMIN" || currentUser?.role == "DEVELOPER")) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("إدارة حسابات المستخدمين وصلاحياتهم", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(onClick = {
                            editingUserAccount = UserAccount(username = "", fullName = "", role = "EMPLOYEE", pinCode = "")
                            showUserAccountDialog = true
                        }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "إضافة مستخدم", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    userAccounts.forEach { account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(account.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("اسم المستخدم: ${account.username} • الدور: ${if (account.role == "ADMIN") "مدير" else "موظف"}", fontSize = 11.sp, color = Color.Gray)
                                Text("الرمز السري: ${account.pinCode}", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Row {
                                IconButton(onClick = {
                                    editingUserAccount = account
                                    showUserAccountDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                                }
                                if (account.username != "admin") {
                                    IconButton(onClick = { onDeleteUser(account) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Data backup and restore
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("النسخ الاحتياطي ونقل البيانات", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("قم بتصدير قاعدة بيانات التطبيق بالكامل أو استعادتها من ملف نصي بكل سهولة.", fontSize = 11.sp, color = Color.Gray)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onExport,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تصدير نسخة احتياطية", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("استيراد نسخة احتياطية", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. Offline & Sync Status
        var showSyncDialog by remember { mutableStateOf(false) }
        var showConflictDemoDialog by remember { mutableStateOf(false) }
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("المزامنة والعمل بدون إنترنت ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("التطبيق يعمل بشكل كامل عند انقطاع الإنترنت. يتم حفظ العمليات ومزامنتها تلقائياً لاحقاً.", fontSize = 11.sp, color = Color.Gray)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showSyncDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("سجل المزامنة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showConflictDemoDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.Merge, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تجربة تعارض البيانات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        if (showSyncDialog) {
            SyncLogsDialog(
                viewModel = viewModel,
                onDismiss = { showSyncDialog = false }
            )
        }
        if (showConflictDemoDialog) {
            ConflictResolutionDialog(
                onDismiss = { showConflictDemoDialog = false }
            )
        }
    }

    // Add/Edit User Dialog
    if (showUserAccountDialog && editingUserAccount != null) {
        var username by remember { mutableStateOf(editingUserAccount!!.username) }
        var fullName by remember { mutableStateOf(editingUserAccount!!.fullName) }
        var pinCode by remember { mutableStateOf(editingUserAccount!!.pinCode) }
        var role by remember { mutableStateOf(editingUserAccount!!.role) }

        val hasChanges = username != editingUserAccount!!.username || fullName != editingUserAccount!!.fullName || pinCode != editingUserAccount!!.pinCode || role != editingUserAccount!!.role
        var showConfirmExit by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = { if (hasChanges) showConfirmExit = true else showUserAccountDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(if (editingUserAccount!!.id == 0) "إضافة حساب مستخدم جديد" else "تعديل حساب المستخدم", fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { if (hasChanges) showConfirmExit = true else showUserAccountDialog = false }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("الاسم الكامل للمستخدم") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("اسم المستخدم (للأمان والنشاطات)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = editingUserAccount!!.username != "admin" && editingUserAccount!!.role != "DEVELOPER" || currentUser?.role == "DEVELOPER"
                        )

                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { if (it.length <= 4) pinCode = it },
                            label = { Text("الرمز السري للقفل (4 أرقام)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true
                        )

                        Text("صلاحيات المستخدم في النظام", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = role == "EMPLOYEE",
                                        onClick = { role = "EMPLOYEE" },
                                        enabled = editingUserAccount!!.username != "admin" && editingUserAccount!!.role != "DEVELOPER" || currentUser?.role == "DEVELOPER"
                                    )
                                    Text("موظف مالي (صلاحيات محدودة)")
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = role == "ADMIN",
                                        onClick = { role = "ADMIN" },
                                        enabled = editingUserAccount!!.username != "admin" && editingUserAccount!!.role != "DEVELOPER" || currentUser?.role == "DEVELOPER"
                                    )
                                    Text("مدير نظام (كامل الصلاحيات)")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            onClick = {
                                if (username.isNotBlank() && fullName.isNotBlank() && pinCode.length == 4) {
                                    onSaveUser(
                                        editingUserAccount!!.copy(
                                            username = username,
                                            fullName = fullName,
                                            pinCode = pinCode,
                                            role = role
                                        )
                                    )
                                    showUserAccountDialog = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حفظ بيانات الحساب", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showConfirmExit) {
            ConfirmExitDialog(
                onDismiss = { showConfirmExit = false },
                onConfirmSave = {
                    if (username.isNotBlank() && fullName.isNotBlank() && pinCode.length == 4) {
                        onSaveUser(editingUserAccount!!.copy(username = username, fullName = fullName, pinCode = pinCode, role = role))
                        showUserAccountDialog = false
                    } else {
                        showConfirmExit = false
                    }
                },
                onConfirmDiscard = {
                    showUserAccountDialog = false
                    showConfirmExit = false
                }
            )
        }
    }

    // Import Dialog
    if (showImportDialog) {
        Dialog(onDismissRequest = { showImportDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("استيراد نسخة احتياطية من نص البيانات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("تنبيه: ستؤدي هذه العملية إلى استبدال كافة البيانات الحالية في التطبيق ببيانات النسخة المستوردة.", fontSize = 11.sp, color = Color.Red)

                    OutlinedTextField(
                        value = importTextPaste,
                        onValueChange = { importTextPaste = it },
                        placeholder = { Text("الصق هنا نص البيانات الذي قمت بنسخه مسبقاً...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        minLines = 5
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showImportDialog = false }) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                if (importTextPaste.isNotBlank()) {
                                    onImport(importTextPaste)
                                    showImportDialog = false
                                    importTextPaste = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("استعادة واستبدال الآن", color = Color.White)
                        }
                        

                    }
                }
            }
        }
    }
}

// --- ADD/EDIT SUBSCRIBER DIALOGUE ---

@Composable
fun ConfirmExitDialog(
    onDismiss: () -> Unit,
    onConfirmSave: () -> Unit,
    onConfirmDiscard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تنبيه: بيانات غير محفوظة", fontWeight = FontWeight.Bold) },
        text = { Text("لقد قمت بتعديل بعض البيانات ولم يتم حفظها بعد. كيف تود المتابعة؟") },
        confirmButton = {
            Button(
                onClick = onConfirmSave,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("حفظ ثم الرجوع")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onConfirmDiscard) {
                    Text("رجوع بدون حفظ", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("إلغاء")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriberDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String, notes: String, emoji: String, status: String, dueDate: Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("👤") }
    var isActive by remember { mutableStateOf(true) }
    var daysUntilDue by remember { mutableStateOf("") }

    val hasChanges = name.isNotBlank() || phone.isNotBlank() || address.isNotBlank() || notes.isNotBlank() || daysUntilDue.isNotBlank()
    var showConfirmExit by remember { mutableStateOf(false) }

    val emojis = listOf("👤", "🏬", "🏢", "🏪", "💼", "👨", "👩", "🤝", "📦", "🚗")

    Dialog(
        onDismissRequest = { if (hasChanges) showConfirmExit = true else onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("إضافة مشترك جديد", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { if (hasChanges) showConfirmExit = true else onDismiss() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الكامل للمشترك") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف (الواتساب)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("العنوان السكني / التجاري") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = daysUntilDue,
                        onValueChange = { daysUntilDue = it },
                        label = { Text("عدد الأيام حتى موعد السداد (اختياري)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات إضافية") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Text("اختر أيقونة المشترك الرسمية", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        emojis.forEach { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray, CircleShape)
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("حالة المشترك (نشط حالياً)", fontSize = 13.sp)
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            if (name.isNotBlank() && phone.isNotBlank()) {
                                val days = daysUntilDue.toLongOrNull()
                                val dueDate = if (days != null && days > 0) {
                                    System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000)
                                } else null
                                onConfirm(
                                    name,
                                    phone,
                                    address,
                                    notes,
                                    selectedEmoji,
                                    if (isActive) "ACTIVE" else "SUSPENDED",
                                    dueDate
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إضافة وحفظ المشترك", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showConfirmExit) {
        ConfirmExitDialog(
            onDismiss = { showConfirmExit = false },
            onConfirmSave = {
                if (name.isNotBlank() && phone.isNotBlank()) {
                    val days = daysUntilDue.toLongOrNull()
                    val dueDate = if (days != null && days > 0) {
                        System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000)
                    } else null
                    onConfirm(name, phone, address, notes, selectedEmoji, if (isActive) "ACTIVE" else "SUSPENDED", dueDate)
                } else {
                    showConfirmExit = false
                }
            },
            onConfirmDiscard = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubscriberDialog(
    subscriber: Subscriber,
    onDismiss: () -> Unit,
    onConfirm: (Subscriber) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(subscriber.name) }
    var phone by remember { mutableStateOf(subscriber.phone) }
    var address by remember { mutableStateOf(subscriber.address) }
    var notes by remember { mutableStateOf(subscriber.notes) }
    var selectedEmoji by remember { mutableStateOf(subscriber.avatarEmoji) }
    var isActive by remember { mutableStateOf(subscriber.status == "ACTIVE") }
    
    // Initial calculate days remaining
    val initialDays = subscriber.nextDueDate?.let { 
        ((it - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).coerceAtLeast(0).toString() 
    } ?: ""
    var daysUntilDue by remember { mutableStateOf(initialDays) }
    
    val hasChanges = name != subscriber.name || phone != subscriber.phone || address != subscriber.address || notes != subscriber.notes || selectedEmoji != subscriber.avatarEmoji || isActive != (subscriber.status == "ACTIVE") || daysUntilDue != initialDays
    var showConfirmExit by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    val emojis = listOf("👤", "🏬", "🏢", "🏪", "💼", "👨", "👩", "🤝", "📦", "🚗")

    Dialog(
        onDismissRequest = { if (hasChanges) showConfirmExit = true else onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("تعديل بيانات المشترك", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { if (hasChanges) showConfirmExit = true else onDismiss() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الكامل للمشترك") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف (الواتساب)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("العنوان") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = daysUntilDue,
                        onValueChange = { daysUntilDue = it },
                        label = { Text("عدد الأيام حتى موعد السداد (اختياري)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Text("اختر الأيقونة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        emojis.forEach { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray, CircleShape)
                                    .clickable { selectedEmoji = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("حالة المشترك (نشط)", fontSize = 13.sp)
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f).height(56.dp),
                            onClick = { showConfirmDelete = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف المشترك")
                        }

                        Button(
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            onClick = {
                                if (name.isNotBlank() && phone.isNotBlank()) {
                                    val days = daysUntilDue.toLongOrNull()
                                    val dueDate = if (days != null && days >= 0) {
                                        System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000)
                                    } else null
                                    onConfirm(
                                        subscriber.copy(
                                            name = name,
                                            phone = phone,
                                            address = address,
                                            notes = notes,
                                            avatarEmoji = selectedEmoji,
                                            status = if (isActive) "ACTIVE" else "SUSPENDED",
                                            nextDueDate = dueDate
                                        )
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حفظ التعديلات")
                        }
                    }
                }
            }
        }
    }

    if (showConfirmExit) {
        ConfirmExitDialog(
            onDismiss = { showConfirmExit = false },
            onConfirmSave = {
                if (name.isNotBlank() && phone.isNotBlank()) {
                    val days = daysUntilDue.toLongOrNull()
                    val dueDate = if (days != null && days >= 0) {
                        System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000)
                    } else null
                    onConfirm(
                        subscriber.copy(
                            name = name,
                            phone = phone,
                            address = address,
                            notes = notes,
                            avatarEmoji = selectedEmoji,
                            status = if (isActive) "ACTIVE" else "SUSPENDED",
                            nextDueDate = dueDate
                        )
                    )
                } else {
                    showConfirmExit = false
                }
            },
            onConfirmDiscard = onDismiss
        )
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("تأكيد حذف المشترك") },
            text = { Text("هل أنت متأكد تماماً من رغبتك في حذف المشترك وحذف سجل معاملاته بالكامل؟ لا يمكن التراجع عن هذا القرار.") },
            confirmButton = {
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                    Text("نعم، احذف بالكامل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

// --- ADD TRANSACTION DIALOGUE ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    type: String, // DEBT or PAYMENT
    subscriberName: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, notes: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val title = if (type == "DEBT") "تسجيل مبلغ دين جديد" else "تسجيل دفعة تسديد مستلمة"
    val label = if (type == "DEBT") "مبلغ الدين المضاف (د.ع)" else "مبلغ الدفعة المستلمة (د.ع)"

    val hasChanges = amount.isNotBlank() || notes.isNotBlank()
    var showConfirmExit by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (hasChanges) showConfirmExit = true else onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(title, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { if (hasChanges) showConfirmExit = true else onDismiss() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = (if (type == "DEBT") DebtRed else PaymentGreen).copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (type == "DEBT") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (type == "DEBT") DebtRed else PaymentGreen
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("المشترك المستهدف: $subscriberName", fontWeight = FontWeight.Medium)
                        }
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات وتفاصيل هذه العملية...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                onConfirm(amt, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == "DEBT") DebtRed else PaymentGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تسجيل وحفظ البيانات", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showConfirmExit) {
        ConfirmExitDialog(
            onDismiss = { showConfirmExit = false },
            onConfirmSave = {
                val amt = amount.toDoubleOrNull()
                if (amt != null && amt > 0) {
                    onConfirm(amt, notes)
                } else {
                    showConfirmExit = false
                }
            },
            onConfirmDiscard = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    tx: FinancialTransaction,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, notes: String) -> Unit
) {
    var amount by remember { mutableStateOf(tx.amount.toString()) }
    var notes by remember { mutableStateOf(tx.notes) }

    val isDebt = tx.type == "DEBT"
    val title = if (isDebt) "تعديل عملية الدين" else "تعديل دفعة التسديد"

    val hasChanges = amount != tx.amount.toString() || notes != tx.notes
    var showConfirmExit by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (hasChanges) showConfirmExit = true else onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(title, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { if (hasChanges) showConfirmExit = true else onDismiss() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    ) {
                        Text(
                            "تنبيه: سيتم تحديث وتعديل رصيد المشترك وحساباته فور حفظ هذا التعديل.",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("المبلغ (د.ع)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("الملاحظات والتبرير...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (amt != null && amt > 0) {
                                onConfirm(amt, notes)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ التعديلات الجديدة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showConfirmExit) {
        ConfirmExitDialog(
            onDismiss = { showConfirmExit = false },
            onConfirmSave = {
                val amt = amount.toDoubleOrNull()
                if (amt != null && amt > 0) {
                    onConfirm(amt, notes)
                } else {
                    showConfirmExit = false
                }
            },
            onConfirmDiscard = onDismiss
        )
    }
}

// --- WHATSAPP PROMPT MANUAL DIALOGUE ---

@Composable
fun WhatsAppPromptDialog(
    pendingSend: PendingWhatsAppSend,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = null,
                        tint = PaymentGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "مراسلة وتأكيد الحركة للمشترك",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                if (pendingSend.isFailedAutoFallback) {
                    Text(
                        text = "⚠️ تنبيه: فشل الإرسال التلقائي عبر بوابة الـ API: \n(${pendingSend.errorMessage})\nتم تفعيل خيار الإرسال اليدوي كخيار بديل لضمان وصول الإشعار.",
                        color = DebtRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 15.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftDebtRed, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                } else {
                    Text(
                        text = "تم تكوين الإرسال اليدوي. يرجى الضغط على زر الإرسال لفتح تطبيق واتساب مع رسالة التأكيد جاهزة للإرسال مباشرة.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                Text(
                    text = "المشترك: ${pendingSend.subscriberName}\nالهاتف: ${pendingSend.phone}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Text("معاينة نص الرسالة:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                Text(
                    text = pendingSend.message,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("تخطي الإشعار")
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = PaymentGreen)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إرسال عبر واتساب الآن", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- SYNC & OFFLINE FEATURE MOCK UI ---

@Composable
fun SyncLogsDialog(
    viewModel: DebtViewModel,
    onDismiss: () -> Unit
) {
    val syncLogs by viewModel.allSyncLogs.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().height(400.dp).padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("سجل المزامنة ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    if (syncLogs.isEmpty()) {
                        item {
                            Text("لا توجد عمليات مسجلة.", color = Color.Gray)
                        }
                    }
                    items(syncLogs) { log ->
                        val statusColor = when (log.status) {
                            com.example.data.model.SyncStatus.SYNCED -> PaymentGreen
                            com.example.data.model.SyncStatus.FAILED -> MaterialTheme.colorScheme.error
                            else -> AccentOrange
                        }
                        val statusText = when (log.status) {
                            com.example.data.model.SyncStatus.SYNCED -> "متزامن"
                            com.example.data.model.SyncStatus.FAILED -> "فشل"
                            else -> "معلق"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(log.operationType, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("المعرف: ${log.entityId}", fontSize = 10.sp, color = Color.Gray)
                            }
                            Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("إغلاق")
                }
            }
        }
    }
}

@Composable
fun ConflictResolutionDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "⚠️ اكتشاف تعارض في البيانات",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "تم تعديل بيانات نفس المشترك (خالد محمد) من جهازين مختلفين في وضع عدم الاتصال.",
                    fontSize = 12.sp
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Local Data
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("النسخة المحلية", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("الدين: 450 د.ع", fontSize = 11.sp)
                            Text("الهاتف: 0555", fontSize = 11.sp)
                        }
                    }
                    // Remote Data
                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("نسخة السحابة", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AccentOrange)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("الدين: 600 د.ع", fontSize = 11.sp)
                            Text("الهاتف: 0500", fontSize = 11.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Button(onClick = onDismiss) { Text("دمج تلقائي") }
                }
            }
        }
    }
}