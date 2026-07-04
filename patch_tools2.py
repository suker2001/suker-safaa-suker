import sys

with open('app/src/main/java/com/example/ui/screens/SystemHealthScreen.kt', 'r') as f:
    content = f.read()

import_toast = "import android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext\n"
if "import android.widget.Toast" not in content:
    content = content.replace("import androidx.compose.runtime.*", import_toast + "import androidx.compose.runtime.*")

old_tool = """                items(tools) { (label, icon) ->
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

new_tool = """                items(tools) { (label, icon) ->
                    val context = LocalContext.current
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "تم تشغيل: $label بنجاح", Toast.LENGTH_SHORT).show()
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

content = content.replace(old_tool, new_tool)

with open('app/src/main/java/com/example/ui/screens/SystemHealthScreen.kt', 'w') as f:
    f.write(content)

