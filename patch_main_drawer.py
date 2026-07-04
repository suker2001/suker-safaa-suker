import sys

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'r') as f:
    content = f.read()

# Add unreadNotificationCount state
content = content.replace("val activeTab by remember", "val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()\n\n    var activeTab by remember")

# Add NotificationDrawerItem
notification_item = """
                                NavigationDrawerItem(
                                    label = { 
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("الإشعارات", fontWeight = FontWeight.Bold)
                                            if (unreadNotificationCount > 0) {
                                                Badge(containerColor = MaterialTheme.colorScheme.error) { Text(unreadNotificationCount.toString()) }
                                            }
                                        } 
                                    },
                                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                                    selected = activeTab == 5 && selectedSubscriber == null,
                                    onClick = { 
                                        activeTab = 5 
                                        viewModel.selectSubscriber(null)
                                        scope.launch { drawerState.close() }
                                    }
                                )
                                NavigationDrawerItem(
                                    label = { Text("التقارير المالية", fontWeight = FontWeight.Bold) },"""

content = content.replace('NavigationDrawerItem(\n                                    label = { Text("التقارير المالية", fontWeight = FontWeight.Bold) },', notification_item)

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'w') as f:
    f.write(content)
