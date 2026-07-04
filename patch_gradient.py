import sys

with open('app/src/main/java/com/example/ui/screens/SystemHealthScreen.kt', 'r') as f:
    content = f.read()

old_overview = """@Composable
fun SystemOverviewCard(cpuUsage: Float, memoryUsage: Float) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {"""

new_overview = """@Composable
fun SystemOverviewCard(cpuUsage: Float, memoryUsage: Float) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
        ) {"""

content = content.replace(old_overview, new_overview)

old_row = """        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {"""

new_row = """        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {"""

content = content.replace(old_row, new_row)

old_close = """        }
    }
}

@Composable
fun CircularProgressMetric"""

new_close = """        }
        }
    }
}

@Composable
fun CircularProgressMetric"""

content = content.replace(old_close, new_close)

with open('app/src/main/java/com/example/ui/screens/SystemHealthScreen.kt', 'w') as f:
    f.write(content)

