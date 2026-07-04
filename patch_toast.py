import sys

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'r') as f:
    content = f.read()

import_statement = "import kotlinx.coroutines.delay"

if import_statement not in content:
    content = content.replace("import kotlinx.coroutines.launch", "import kotlinx.coroutines.launch\nimport kotlinx.coroutines.delay")

# Let's find Toast.makeText
toast_code = """    // Side Notification Flows
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }"""

new_toast_code = """    // Custom Animated Toast State
    var toastMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            toastMessage = message
            delay(3000)
            toastMessage = null
        }
    }"""

content = content.replace(toast_code, new_toast_code)

# Let's insert the custom Toast UI at the bottom of the Box holding the innerPadding
box_end = """                        }
                    }
                }
            }
        }
    }
}"""

custom_toast_ui = """                        }
                        
                        // Custom Toast / Banner
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
                        }
                    }
                }
            }
        }
    }
}"""

content = content.replace(box_end, custom_toast_ui)

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'w') as f:
    f.write(content)

