import sys

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'r') as f:
    content = f.read()

old_login_route = """                    onLoginAttempt = { username, pin ->
                        viewModel.login(username, pin) { success ->
                            if (success) {
                                activeTab = 0
                            }
                        }
                    }"""

new_login_route = """                    onLoginAttempt = { username, pin ->
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
                    }"""

if old_login_route in content:
    content = content.replace(old_login_route, new_login_route)
    with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'w') as f:
        f.write(content)
    print("Patched login routing")
else:
    print("Could not find old_login_route")
