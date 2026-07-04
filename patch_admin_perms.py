import sys

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('currentUser?.role == "ADMIN"', '(currentUser?.role == "ADMIN" || currentUser?.role == "DEVELOPER")')

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'w') as f:
    f.write(content)
print("Patched admin perms")
