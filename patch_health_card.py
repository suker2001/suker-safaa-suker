import sys

with open('app/src/main/java/com/example/ui/screens/SystemHealthScreen.kt', 'r') as f:
    content = f.read()

old_card = """                Text("${metric.healthPercentage}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = statusColor)
            }
            
            Spacer(modifier = Modifier.height(12.dp))"""

new_card = """                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${metric.healthPercentage}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowOutward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))"""

content = content.replace(old_card, new_card)

with open('app/src/main/java/com/example/ui/screens/SystemHealthScreen.kt', 'w') as f:
    f.write(content)

