import sys

with open('app/src/main/java/com/example/ui/screens/SystemHealthScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(",\n                strokeCap = StrokeCap.Round", "")

with open('app/src/main/java/com/example/ui/screens/SystemHealthScreen.kt', 'w') as f:
    f.write(content)
