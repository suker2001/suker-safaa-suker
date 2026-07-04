package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.network.WhatsAppService
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DebtViewModel(
    private val repository: AppRepository,
    private val networkObserver: com.example.data.network.NetworkConnectivityObserver
) : ViewModel() {

    val networkStatus: StateFlow<com.example.data.network.NetworkStatus> = networkObserver.networkStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.network.NetworkStatus.Unavailable)

    // Flow for the sync screen
    val allSyncLogs: StateFlow<List<SyncLog>> = repository.allSyncLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- State Holders ---
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Real-time subscribers list (handles searching)
    val subscribers: StateFlow<List<Subscriber>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allSubscribers
            } else {
                repository.searchSubscribers(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All financial transactions for general logs & stats
    val allTransactions: StateFlow<List<FinancialTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Security activity logs
    val activityLogs: StateFlow<List<ActivityLog>> = repository.allActivityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val whatsappLogs: StateFlow<List<com.example.data.model.PendingWhatsAppMessage>> = repository.getAllWhatsAppMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All system accounts for settings management
    val userAccounts: StateFlow<List<UserAccount>> = repository.allUserAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Notifications State ---
    val allActiveNotifications = repository.allActiveNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount = repository.unreadNotificationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Dynamic Stats Computations ---
    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    // Selected Subscriber & their details
    private val _selectedSubscriberId = MutableStateFlow<Int?>(null)
    val selectedSubscriber: StateFlow<Subscriber?> = _selectedSubscriberId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.observeSubscriber(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedSubscriberTransactions: StateFlow<List<FinancialTransaction>> = _selectedSubscriberId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.observeTransactions(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Message feedback / notification flows
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private val _whatsAppPendingPrompt = MutableSharedFlow<PendingWhatsAppSend>()
    val whatsAppPendingPrompt: SharedFlow<PendingWhatsAppSend> = _whatsAppPendingPrompt.asSharedFlow()

    init {
        viewModelScope.launch {
            // Seed database with defaults
            repository.seedDatabaseIfNeeded()

            // Observe settings
            repository.appSettings.collect { appSettings ->
                if (appSettings != null) {
                    _settings.value = appSettings
                }
            }
        }

        viewModelScope.launch {
            networkStatus.collectLatest { status ->
                if (status == com.example.data.network.NetworkStatus.Available) {
                    syncPendingData()
                }
            }
        }

        // Calculate statistics dynamically whenever subscribers or transactions update
        viewModelScope.launch {
            combine(repository.allSubscribers, repository.allTransactions) { subs, txs ->
                val totalSubscribers = subs.size
                val totalDebts = subs.sumOf { it.totalDebt }
                val totalPaid = subs.sumOf { it.totalPaid }
                val totalRemaining = totalDebts - totalPaid

                // Count subscribers with delayed/overdue debt (e.g., remaining debt > 2000 or suspended with debt)
                val lateDebtsCount = subs.count { it.remainingDebt > 2000.0 || (it.status == "SUSPENDED" && it.remainingDebt > 0.0) }
                val lateDebtsSum = subs.filter { it.remainingDebt > 2000.0 || (it.status == "SUSPENDED" && it.remainingDebt > 0.0) }.sumOf { it.remainingDebt }

                // Group by dates for simple charts
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dailyTransactions = txs.groupBy { sdf.format(Date(it.timestamp)) }
                    .mapValues { (_, dayTxs) ->
                        val dayDebts = dayTxs.filter { it.type == "DEBT" }.sumOf { it.amount }
                        val dayPayments = dayTxs.filter { it.type == "PAYMENT" }.sumOf { it.amount }
                        DayStat(dayDebts, dayPayments)
                    }

                DashboardStats(
                    totalSubscribers = totalSubscribers,
                    totalDebts = totalDebts,
                    totalPaid = totalPaid,
                    totalRemaining = totalRemaining,
                    lateSubscribersCount = lateDebtsCount,
                    lateDebtsSum = lateDebtsSum,
                    dailyStats = dailyTransactions
                )
            }.collect { calculatedStats ->
                _stats.value = calculatedStats
            }
        }
    }

    private suspend fun syncPendingData() {
        val pendingLogs = repository.getPendingSyncLogs()
        if (pendingLogs.isNotEmpty()) {
            repository.logActivity("SYSTEM", "SYSTEM", "بدء مزامنة ${pendingLogs.size} عملية معلقة...")
            pendingLogs.forEach { log ->
                try {
                    // Simulate syncing with cloud
                    kotlinx.coroutines.delay(200) // fake network delay
                    repository.markSyncLogAs(log.id, SyncStatus.SYNCED)
                } catch (e: Exception) {
                    repository.markSyncLogAs(log.id, SyncStatus.FAILED, e.localizedMessage ?: "Unknown Error")
                }
            }
            repository.logActivity("SYSTEM", "SYSTEM", "اكتملت المزامنة بنجاح.")
        }
        
        // Sync pending WhatsApp messages
        val currentSettings = _settings.value
        if (currentSettings.isWhatsAppEnabled && currentSettings.whatsAppApiType != "MANUAL") {
            val pendingMsgs = repository.getPendingWhatsAppMessages()
            pendingMsgs.forEach { msg ->
                val result = WhatsAppService.sendWhatsAppMessage(msg.phone, msg.message, currentSettings)
                result.onSuccess {
                    repository.markWhatsAppMessageSynced(msg.id)
                }
            }
        }
    }

    
    // --- Notifications Management ---
    fun markNotificationAsRead(notification: com.example.data.model.Notification) {
        viewModelScope.launch { repository.updateNotification(notification.copy(isRead = true)) }
    }
    fun deleteNotification(notification: com.example.data.model.Notification) {
        viewModelScope.launch { repository.deleteNotification(notification) }
    }
    fun archiveNotification(notification: com.example.data.model.Notification) {
        viewModelScope.launch { repository.updateNotification(notification.copy(isArchived = true)) }
    }
    fun markAllNotificationsAsRead() {
        viewModelScope.launch { repository.markAllNotificationsAsRead() }
    }
    fun archiveAllNotifications() {
        viewModelScope.launch { repository.archiveAllNotifications() }
    }
    private fun createSystemNotification(title: String, message: String, type: String, priority: String, subId: Int? = null) {
        val currentSettings = _settings.value
        val shouldNotify = when (type) {
            "PAYMENT", "NEW_DEBT" -> currentSettings.notifyPayments
            "DUE_DATE" -> currentSettings.notifyDueDates
            "WHATSAPP" -> currentSettings.notifyWhatsApp
            "SECURITY" -> currentSettings.notifySecurity
            "ACCOUNT_UPDATE" -> currentSettings.notifyEmployeeActivities
            "SYSTEM" -> currentSettings.notifySystem
            else -> true
        }

        if (shouldNotify) {
            viewModelScope.launch {
                repository.addNotification(
                    com.example.data.model.Notification(
                        title = title,
                        message = message,
                        type = type,
                        priority = priority,
                        relatedSubscriberId = subId
                    )
                )
            }
        }
    }

// --- Authentication Actions ---
    fun login(username: String, pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.verifyPinAndLogin(username, pin)
            if (user != null) {
                if (user.isEnabled) {
                    _currentUser.value = user
                    onResult(true)
                } else {
                    android.util.Log.e("Auth", "Login failed: Account disabled for username: $username")
                    _toastMessage.emit("هذا الحساب معطل، يرجى مراجعة الإدارة.")
                    onResult(false)
                }
            } else {
                android.util.Log.e("Auth", "Login failed: Invalid credentials for username: $username")
                _toastMessage.emit("اسم المستخدم أو كلمة المرور غير صحيحة.")
                onResult(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                repository.logActivity(user.username, user.role, "تسجيل خروج من النظام")
            }
            _currentUser.value = null
        }
    }

    // --- Subscriber Operations ---
    fun setSearchQuery(query: String) {
        _searchQuery.value = com.example.utils.FormatHelper.normalizeDigits(query)
    }

    fun selectSubscriber(subscriberId: Int?) {
        _selectedSubscriberId.value = subscriberId
    }

    fun addSubscriber(name: String, phone: String, address: String, notes: String, avatarEmoji: String, status: String, dueDate: Long?) {
        viewModelScope.launch {
            val actor = _currentUser.value?.username ?: "مجهول"
            val role = _currentUser.value?.role ?: "USER"
            repository.addSubscriber(name, phone, address, notes, avatarEmoji, status, dueDate, actor, role)
            createSystemNotification("مشترك جديد", "تم إضافة المشترك $name بنجاح.", "NEW_SUBSCRIBER", "MEDIUM")
            _toastMessage.emit("تم إضافة المشترك بنجاح.")
        }
    }

    fun updateSubscriber(subscriber: Subscriber) {
        viewModelScope.launch {
            val actor = _currentUser.value?.username ?: "مجهول"
            val role = _currentUser.value?.role ?: "USER"
            repository.updateSubscriber(subscriber, actor, role)
            createSystemNotification("تحديث مشترك", "تم تحديث بيانات المشترك ${subscriber.name}.", "ACCOUNT_UPDATE", "LOW", subscriber.id)
            _toastMessage.emit("تم تحديث بيانات المشترك بنجاح.")
        }
    }

    fun deleteSubscriber(subscriber: Subscriber) {
        viewModelScope.launch {
            val actor = _currentUser.value?.username ?: "مجهول"
            val role = _currentUser.value?.role ?: "USER"
            repository.deleteSubscriber(subscriber, actor, role)
            createSystemNotification("حذف مشترك", "تم حذف المشترك ${subscriber.name}.", "ACCOUNT_UPDATE", "HIGH")
            _toastMessage.emit("تم حذف المشترك بنجاح مع كافة حساباته.")
            if (_selectedSubscriberId.value == subscriber.id) {
                _selectedSubscriberId.value = null
            }
        }
    }

    // --- Financial Transaction Operations ---
    private val _triggerWhatsAppSync = MutableSharedFlow<Unit>()
    val triggerWhatsAppSync = _triggerWhatsAppSync.asSharedFlow()

    fun addTransaction(subscriberId: Int, type: String, amount: Double, notes: String) {
        viewModelScope.launch {
            val actor = _currentUser.value?.username ?: "مجهول"
            val role = _currentUser.value?.role ?: "USER"

            val tx = repository.addTransaction(subscriberId, type, amount, notes, actor, role)
            if (tx != null) {
                val notifType = if(type == "DEBT") "NEW_DEBT" else "PAYMENT"
                val notifTitle = if(type == "DEBT") "دين جديد" else "دفعة جديدة"
                val priority = if(type == "DEBT") "HIGH" else "MEDIUM"
                createSystemNotification(notifTitle, "تم تسجيل ${if(type == "DEBT") "دين" else "دفعة"} بقيمة $amount", notifType, priority, subscriberId)
                _toastMessage.emit("تم تسجيل العملية المالية بنجاح.")
                // Trigger background worker
                _triggerWhatsAppSync.emit(Unit)
            } else {
                _toastMessage.emit("فشل تسجيل العملية. المشترك غير موجود.")
            }
        }
    }

    fun updateTransaction(txId: Int, amount: Double, notes: String) {
        viewModelScope.launch {
            val actor = _currentUser.value?.username ?: "مجهول"
            val role = _currentUser.value?.role ?: "USER"
            val success = repository.updateTransaction(txId, amount, notes, actor, role)
            if (success) {
                _toastMessage.emit("تم تعديل العملية المالية وتحديث الحسابات.")
            } else {
                _toastMessage.emit("حدث خطأ أثناء تعديل العملية.")
            }
        }
    }

    fun deleteTransaction(txId: Int) {
        viewModelScope.launch {
            val actor = _currentUser.value?.username ?: "مجهول"
            val role = _currentUser.value?.role ?: "USER"
            val success = repository.deleteTransaction(txId, actor, role)
            if (success) {
                _toastMessage.emit("تم حذف العملية المالية وتعديل الحسابات.")
            } else {
                _toastMessage.emit("فشل في حذف العملية.")
            }
        }
    }

    // --- Settings & User Management ---
    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            val actor = _currentUser.value?.username ?: "مجهول"
            val role = _currentUser.value?.role ?: "USER"
            repository.updateSettings(newSettings, actor, role)
            _toastMessage.emit("تم حفظ الإعدادات وتحديثات واتساب.")
        }
    }

    fun saveUserAccount(account: UserAccount) {
        viewModelScope.launch {
            val actor = _currentUser.value?.username ?: "مجهول"
            val role = _currentUser.value?.role ?: "USER"
            repository.saveUserAccount(account, actor, role)
            _toastMessage.emit("تم حفظ حساب المستخدم.")
        }
    }

    fun deleteUserAccount(account: UserAccount) {
        viewModelScope.launch {
            val actor = _currentUser.value?.username ?: "مجهول"
            val role = _currentUser.value?.role ?: "USER"
            repository.deleteUserAccount(account, actor, role)
            _toastMessage.emit("تم حذف حساب المستخدم.")
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            val actor = _currentUser.value?.username ?: "مجهول"
            val role = _currentUser.value?.role ?: "USER"
            repository.clearActivityLogs(actor, role)
            _toastMessage.emit("تم مسح سجل النشاطات بنجاح.")
        }
    }

    // --- Backup & Restore ---
    fun exportBackup(context: Context, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val json = repository.exportBackupJson()
                onResult(json)
            } catch (e: Exception) {
                _toastMessage.emit("فشل تصدير البيانات: ${e.localizedMessage}")
                onResult(null)
            }
        }
    }

    fun importBackup(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val actor = _currentUser.value?.username ?: "مجهول"
            val role = _currentUser.value?.role ?: "USER"
            val success = repository.importBackupJson(jsonString, actor, role)
            if (success) {
                _toastMessage.emit("تم استيراد قاعدة البيانات بالكامل بنجاح.")
                // Reload settings or session
                val loadedSettings = repository.getAppSettingsOneShot()
                if (loadedSettings != null) {
                    _settings.value = loadedSettings
                }
                onResult(true)
            } else {
                _toastMessage.emit("فشل استيراد النسخة الاحتياطية. يرجى مراجعة صياغة الملف.")
                onResult(false)
            }
        }
    }

    // --- WhatsApp Text Rendering & Automation Core ---


    fun triggerManualWhatsApp(context: Context, phone: String, message: String) {
        val success = WhatsAppService.sendWhatsAppManualIntent(context, phone, message)
        viewModelScope.launch {
            if (success) {
                _toastMessage.emit("تم فتح تطبيق واتساب لإرسال الرسالة.")
            } else {
                _toastMessage.emit("⚠️ تعذر فتح تطبيق واتساب على هذا الجهاز.")
            }
        }
    }
}

// --- Companion Data Structures ---

data class DayStat(
    val debts: Double,
    val payments: Double
)

data class DashboardStats(
    val totalSubscribers: Int = 0,
    val totalDebts: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val lateSubscribersCount: Int = 0,
    val lateDebtsSum: Double = 0.0,
    val dailyStats: Map<String, DayStat> = emptyMap()
)

data class PendingWhatsAppSend(
    val phone: String,
    val message: String,
    val subscriberName: String,
    val isFailedAutoFallback: Boolean = false,
    val errorMessage: String? = null
)

class DebtViewModelFactory(
    private val repository: AppRepository,
    private val networkObserver: com.example.data.network.NetworkConnectivityObserver
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DebtViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DebtViewModel(repository, networkObserver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
