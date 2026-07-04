import sys

with open('app/src/main/java/com/example/ui/screens/NotificationCenterScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("package com.example.ui.screens", "package com.example.ui.screens\n\nimport androidx.compose.foundation.ExperimentalFoundationApi")
content = content.replace("animateItemPlacement()", "animateItem()")

with open('app/src/main/java/com/example/ui/screens/NotificationCenterScreen.kt', 'w') as f:
    f.write(content)
