import sys

with open('app/src/main/java/com/example/ui/screens/SystemHealthScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.ui.graphics.StrokeCap\n", "")
content = content.replace("import androidx.compose.ui.graphics.drawscope.Stroke", "import androidx.compose.ui.graphics.drawscope.Stroke\nimport androidx.compose.ui.graphics.StrokeCap")

with open('app/src/main/java/com/example/ui/screens/SystemHealthScreen.kt', 'w') as f:
    f.write(content)
