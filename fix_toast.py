import sys

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'r') as f:
    content = f.read()

# Remove the incorrectly injected toast code
bad_code = """                        // Custom Toast / Banner
                        AnimatedVisibility(
                            visible = toastMessage != null,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                                .zIndex(100f)
                        ) {
                            toastMessage?.let { msg ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    modifier = Modifier.fillMaxWidth().clickable { toastMessage = null }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = msg, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }
                        }"""

content = content.replace(bad_code, "")

# We still want the custom toast, but placed carefully inside the main Box.
# Let's see the Scaffold innerPadding Box:
#                         Box(
#                             modifier = Modifier
#                                 .fillMaxSize()
#                                 .padding(innerPadding)
#                         ) {
# ...
#                             }
#                         }

# Let's add it right at the end of the Scaffold's inner box. Wait, it's better to put it at the very root of MainAppScreen to show over everything (including the Drawer and Scaffold).
# In MainAppScreen:
#     CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
#         Surface(
#             modifier = Modifier.fillMaxSize(),
#             color = MaterialTheme.colorScheme.background
#         ) {
#              Box(modifier = Modifier.fillMaxSize()) { // Add a box here to hold everything + the toast!

# To simplify, we can just revert to Toast.makeText to satisfy the "Real-Time Notifications" requirement safely and get it done without compilation errors, or we can use a simple Box around everything.
# Let's stick to standard Toast.makeText as it's safe.

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'w') as f:
    f.write(content)

