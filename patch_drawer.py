import sys
import re

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'r') as f:
    content = f.read()

# First replace Activity Logs drawer item
old_activity = """                                NavigationDrawerItem(
                                    label = { Text("سجل النشاطات", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                                    selected = activeTab == 2 && selectedSubscriber == null,
                                    onClick = {
                                         activeTab = 2
                                         viewModel.selectSubscriber(null)
                                        scope.launch { drawerState.close() }
                                    }
                                )"""
new_activity = """                                if (currentUser?.role == "ADMIN" || currentUser?.role == "DEVELOPER") {
                                    NavigationDrawerItem(
                                        label = { Text("سجل النشاطات", fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                                        selected = activeTab == 2 && selectedSubscriber == null,
                                        onClick = {
                                             activeTab = 2
                                             viewModel.selectSubscriber(null)
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                }"""
content = content.replace(old_activity, new_activity)

old_reports = """                                NavigationDrawerItem(
                                    label = { Text("التقارير المالية", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                                    selected = activeTab == 4 && selectedSubscriber == null,
                                    onClick = {
                                         activeTab = 4
                                         viewModel.selectSubscriber(null)
                                        scope.launch { drawerState.close() }
                                    }
                                )"""
new_reports = """                                if (currentUser?.role == "ADMIN" || currentUser?.role == "DEVELOPER") {
                                    NavigationDrawerItem(
                                        label = { Text("التقارير المالية", fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                                        selected = activeTab == 4 && selectedSubscriber == null,
                                        onClick = {
                                             activeTab = 4
                                             viewModel.selectSubscriber(null)
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                }"""
content = content.replace(old_reports, new_reports)

old_health = """                                NavigationDrawerItem(
                                    label = { Text("صحة النظام", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.HealthAndSafety, contentDescription = null) },
                                    selected = activeTab == 6 && selectedSubscriber == null,
                                    onClick = {
                                         activeTab = 6
                                         viewModel.selectSubscriber(null)
                                        scope.launch { drawerState.close() }
                                    }
                                )"""
new_health = """                                if (currentUser?.role == "DEVELOPER" || currentUser?.role == "ADMIN") {
                                    NavigationDrawerItem(
                                        label = { Text("صحة النظام", fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.Default.HealthAndSafety, contentDescription = null) },
                                        selected = activeTab == 6 && selectedSubscriber == null,
                                        onClick = {
                                             activeTab = 6
                                             viewModel.selectSubscriber(null)
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                }"""
content = content.replace(old_health, new_health)

old_settings = """                                NavigationDrawerItem(
                                    label = { Text("الإعدادات", fontWeight = FontWeight.Bold) },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    selected = activeTab == 3 && selectedSubscriber == null,
                                    onClick = {
                                         activeTab = 3
                                         viewModel.selectSubscriber(null)
                                        scope.launch { drawerState.close() }
                                    }
                                )"""
new_settings = """                                if (currentUser?.role == "ADMIN" || currentUser?.role == "DEVELOPER") {
                                    NavigationDrawerItem(
                                        label = { Text("الإعدادات", fontWeight = FontWeight.Bold) },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                        selected = activeTab == 3 && selectedSubscriber == null,
                                        onClick = {
                                             activeTab = 3
                                             viewModel.selectSubscriber(null)
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                }"""
content = content.replace(old_settings, new_settings)

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'w') as f:
    f.write(content)
print("Patched drawer items")
