import sys

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'r') as f:
    content = f.read()

# Add unreadNotificationCount parameter to TopAppBarContent call
content = content.replace("""                            TopAppBarContent(
                                activeTab = activeTab,
                                currentUser = currentUser,
                                selectedSubscriber = selectedSubscriber,
                                networkStatus = networkStatus,
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onBackClick = onBack,
                                onLogoutClick = { viewModel.logout() }
                            )""", """                            TopAppBarContent(
                                activeTab = activeTab,
                                currentUser = currentUser,
                                selectedSubscriber = selectedSubscriber,
                                networkStatus = networkStatus,
                                unreadNotificationCount = unreadNotificationCount,
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onBackClick = onBack,
                                onLogoutClick = { viewModel.logout() },
                                onNotificationsClick = { activeTab = 5; viewModel.selectSubscriber(null) }
                            )""")

# Add NotificationCenterScreen routing
notification_route = """                                5 -> NotificationCenterScreen(
                                    viewModel = viewModel
                                )
                                4 -> ReportsScreen("""

content = content.replace('                                4 -> ReportsScreen(', notification_route)

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'w') as f:
    f.write(content)
