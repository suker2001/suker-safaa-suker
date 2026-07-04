import sys

with open('app/src/main/java/com/example/ui/screens/SystemHealthScreen.kt', 'r') as f:
    content = f.read()

old_tool = """                items(tools) { (label, icon) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { /* Simulate Action */ }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }"""

new_tool = """                items(tools) { (label, icon) ->
                    val scope = rememberCoroutineScope()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clickable {
                            scope.launch {
                                // We simulate the action
                                kotlinx.coroutines.delay(500)
                                viewModel.toastMessage.emit("تم تشغيل: $label بنجاح")
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }"""

# add error emit method to viewModel? It's a shared flow, it's public. Wait, is it MutableSharedFlow? No, it's a SharedFlow.
# Let's check DebtViewModel.kt toastMessage.
