import sys

with open('app/src/main/java/com/example/data/model/Entities.kt', 'r') as f:
    content = f.read()

old_settings = """    val whatsAppTemplatePayment: String = "مرحباً {name}\\n\\nتم استلام دفعة جديدة:\\n\\nالمبلغ المسدد: {amount}\\nالمبلغ المتبقي: {remaining}\\n\\nتاريخ العملية: {date}\\n\\nشكراً لتعاونكم.",
    val isDarkMode: Boolean = false
)"""

new_settings = """    val whatsAppTemplatePayment: String = "مرحباً {name}\\n\\nتم استلام دفعة جديدة:\\n\\nالمبلغ المسدد: {amount}\\nالمبلغ المتبقي: {remaining}\\n\\nتاريخ العملية: {date}\\n\\nشكراً لتعاونكم.",
    val isDarkMode: Boolean = false,
    
    // Notification Settings
    val notifyPayments: Boolean = true,
    val notifyDueDates: Boolean = true,
    val notifyWhatsApp: Boolean = true,
    val notifySecurity: Boolean = true,
    val notifyEmployeeActivities: Boolean = true,
    val notifySystem: Boolean = true,
    val pushNotificationsEnabled: Boolean = true
)"""

content = content.replace(old_settings, new_settings)

with open('app/src/main/java/com/example/data/model/Entities.kt', 'w') as f:
    f.write(content)
