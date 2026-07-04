import sys
import re

with open('app/src/main/java/com/example/ui/viewmodel/DebtViewModel.kt', 'r') as f:
    content = f.read()

old_login = """    fun login(username: String, pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repository.verifyPinAndLogin(username, pin)
            if (user != null) {
                _currentUser.value = user
                onResult(true)
            } else {
                _toastMessage.emit("رمز المرور PIN خاطئ أو الحساب معطل.")
                onResult(false)
            }
        }
    }"""

new_login = """    fun login(username: String, pin: String, onResult: (Boolean) -> Unit) {
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
    }"""

if old_login in content:
    content = content.replace(old_login, new_login)
    with open('app/src/main/java/com/example/ui/viewmodel/DebtViewModel.kt', 'w') as f:
        f.write(content)
    print("Patched DebtViewModel login")
else:
    print("Could not find old login block")
