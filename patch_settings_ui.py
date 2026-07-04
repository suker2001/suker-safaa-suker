import sys

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'r') as f:
    content = f.read()

# Variables mapping
vars_old = """    var isPinEnabled by remember(settings) { mutableStateOf(settings.isPinEnabled) }"""

vars_new = """    var isPinEnabled by remember(settings) { mutableStateOf(settings.isPinEnabled) }
    var notifyPayments by remember(settings) { mutableStateOf(settings.notifyPayments) }
    var notifyDueDates by remember(settings) { mutableStateOf(settings.notifyDueDates) }
    var notifyWhatsApp by remember(settings) { mutableStateOf(settings.notifyWhatsApp) }
    var notifySecurity by remember(settings) { mutableStateOf(settings.notifySecurity) }
    var notifyEmployeeActivities by remember(settings) { mutableStateOf(settings.notifyEmployeeActivities) }
    var notifySystem by remember(settings) { mutableStateOf(settings.notifySystem) }"""

content = content.replace(vars_old, vars_new)

# Card UI
notif_card = """        // Notification Settings UI
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

        // 2. PIN Security Lock Options"""

content = content.replace("// 2. PIN Security Lock Options", notif_card)

# Save logic
save_old = """                onSettingsSave(
                    settings.copy(
                        isWhatsAppEnabled = isWhatsAppEnabled,
                        whatsAppApiType = whatsAppApiType,
                        whatsAppInstanceId = whatsAppInstanceId,
                        whatsAppToken = whatsAppToken,
                        whatsAppSid = whatsAppSid,
                        whatsAppTemplateDebt = whatsAppTemplateDebt,
                        whatsAppTemplatePayment = whatsAppTemplatePayment,
                        isPinEnabled = isPinEnabled
                    )
                )"""

save_new = """                onSettingsSave(
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
                )"""

content = content.replace(save_old, save_new)

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'w') as f:
    f.write(content)

