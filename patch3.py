import sys

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("var activeTab by remember { mutableIntStateOf(0) }", "val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()\n    var activeTab by remember { mutableIntStateOf(0) }")

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'w') as f:
    f.write(content)
