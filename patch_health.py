import sys

with open('app/src/main/java/com/example/ui/screens/SystemHealthScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.ui.graphics.Stroke", "import androidx.compose.ui.graphics.StrokeCap\nimport androidx.compose.ui.graphics.drawscope.Stroke")

bad_sync = 'HealthMetric("المزامنة السحابية", if (syncLogs.any { it.status == "FAILED" }) HealthStatus.WARNING else HealthStatus.HEALTHY, "${syncLogs.count { it.status == "PENDING" }} قيد الانتظار", Icons.Default.CloudSync, healthPercentage = 95)'
good_sync = 'HealthMetric("المزامنة السحابية", if (syncLogs.any { it.status == com.example.data.model.SyncStatus.FAILED }) HealthStatus.WARNING else HealthStatus.HEALTHY, "${syncLogs.count { it.status == com.example.data.model.SyncStatus.PENDING }} قيد الانتظار", Icons.Default.CloudSync, healthPercentage = 95)'

content = content.replace(bad_sync, good_sync)

with open('app/src/main/java/com/example/ui/screens/SystemHealthScreen.kt', 'w') as f:
    f.write(content)

