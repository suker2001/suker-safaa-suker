import sys
import re

with open('app/src/main/java/com/example/data/repository/AppRepository.kt', 'r') as f:
    content = f.read()

seed_func_old = """        // 2. Seed Users
        val users = userAccountDao.getAllUserAccounts().firstOrNull()
        if (users.isNullOrEmpty()) {
            userAccountDao.insertUserAccount(
                UserAccount(
                    username = "admin",
                    fullName = "المدير العام (أدمن)",
                    role = "ADMIN",
                    pinCode = "1111",
                    isEnabled = true
                )
            )
            userAccountDao.insertUserAccount(
                UserAccount(
                    username = "employee",
                    fullName = "الموظف المالي",
                    role = "EMPLOYEE",
                    pinCode = "2222",
                    isEnabled = true
                )
            )
            logActivity("system", "SYSTEM", "تم تهيئة قاعدة البيانات وإضافة حسابات مستخدمين افتراضية")
        }"""

seed_func_new = """        // 2. Seed Users
        val users = userAccountDao.getAllUserAccounts().firstOrNull() ?: emptyList()
        if (users.isEmpty()) {
            userAccountDao.insertUserAccount(
                UserAccount(
                    username = "admin",
                    fullName = "المدير العام (أدمن)",
                    role = "ADMIN",
                    pinCode = "1111",
                    isEnabled = true
                )
            )
            userAccountDao.insertUserAccount(
                UserAccount(
                    username = "employee",
                    fullName = "الموظف المالي",
                    role = "EMPLOYEE",
                    pinCode = "2222",
                    isEnabled = true
                )
            )
            logActivity("system", "SYSTEM", "تم تهيئة قاعدة البيانات وإضافة حسابات مستخدمين افتراضية")
        }
        
        // Ensure DEVELOPER account always exists
        val currentUsers = userAccountDao.getAllUserAccounts().firstOrNull() ?: emptyList()
        val devExists = currentUsers.any { it.role == "DEVELOPER" }
        if (!devExists) {
            userAccountDao.insertUserAccount(
                UserAccount(
                    username = "developer",
                    fullName = "المطور",
                    role = "DEVELOPER",
                    pinCode = "0000",
                    isEnabled = true
                )
            )
            logActivity("system", "SYSTEM", "تم إنشاء حساب المطور التلقائي")
        }"""

if seed_func_old in content:
    content = content.replace(seed_func_old, seed_func_new)
    with open('app/src/main/java/com/example/data/repository/AppRepository.kt', 'w') as f:
        f.write(content)
    print("Patched repository successfully.")
else:
    print("Could not find seed_func_old in AppRepository.kt")
