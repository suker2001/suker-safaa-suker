import sys

with open('app/src/main/java/com/example/ui/viewmodel/DebtViewModel.kt', 'r') as f:
    content = f.read()

old_func = """    private fun createSystemNotification(title: String, message: String, type: String, priority: String, subId: Int? = null) {
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
    }"""

new_func = """    private fun createSystemNotification(title: String, message: String, type: String, priority: String, subId: Int? = null) {
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
    }"""

content = content.replace(old_func, new_func)

with open('app/src/main/java/com/example/ui/viewmodel/DebtViewModel.kt', 'w') as f:
    f.write(content)
