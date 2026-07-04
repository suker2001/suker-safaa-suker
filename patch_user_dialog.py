import sys

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'r') as f:
    content = f.read()

# Protect Developer and Admin roles from being modified by others
content = content.replace(
    'enabled = editingUserAccount!!.username != "admin"',
    'enabled = editingUserAccount!!.username != "admin" && editingUserAccount!!.role != "DEVELOPER" || currentUser?.role == "DEVELOPER"'
)

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'w') as f:
    f.write(content)
print("Patched user dialog")
