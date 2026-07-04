package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Notification
import com.example.ui.viewmodel.DebtViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationCenterScreen(
    viewModel: DebtViewModel
) {
    val allNotifications by viewModel.allActiveNotifications.collectAsState()
    
    var selectedCategory by remember { mutableStateOf("الكل") }
    val categories = listOf("الكل", "غير مقروء", "الديون والدفعات", "المنظومة والأمان")

    val filteredNotifications = remember(allNotifications, selectedCategory) {
        when (selectedCategory) {
            "غير مقروء" -> allNotifications.filter { !it.isRead }
            "الديون والدفعات" -> allNotifications.filter { it.type in listOf("PAYMENT", "NEW_DEBT", "DUE_DATE") }
            "المنظومة والأمان" -> allNotifications.filter { it.type in listOf("SYSTEM", "SECURITY", "ACCOUNT_UPDATE") }
            else -> allNotifications
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الإشعارات التنبيهية",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تعيين كمقروء")
                }
                IconButton(onClick = { viewModel.archiveAllNotifications() }) {
                    Icon(Icons.Default.Archive, contentDescription = "أرشفة الكل", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Category Filter Chips
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            edgePadding = 16.dp,
            divider = {},
            indicator = {},
            containerColor = Color.Transparent,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            categories.forEachIndexed { index, category ->
                val isSelected = selectedCategory == category
                Tab(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    ChipItem(text = category, isSelected = isSelected)
                }
            }
        }

        if (filteredNotifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.NotificationsOff, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("لا توجد إشعارات حالياً", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Grouping by Date
                val grouped = filteredNotifications.groupBy { 
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = it.timestamp
                    val today = Calendar.getInstance()
                    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                    
                    when {
                        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "اليوم"
                        cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "أمس"
                        else -> "أقدم"
                    }
                }

                grouped.forEach { (dateGroup, notes) ->
                    item {
                        Text(
                            text = dateGroup,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }

                    items(notes, key = { it.id }) { notification ->
                        NotificationItem(
                            notification = notification,
                            onRead = { viewModel.markNotificationAsRead(it) },
                            onArchive = { viewModel.archiveNotification(it) },
                            onDelete = { viewModel.deleteNotification(it) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChipItem(text: String, isSelected: Boolean) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onRead: (Notification) -> Unit,
    onArchive: (Notification) -> Unit,
    onDelete: (Notification) -> Unit,
    modifier: Modifier = Modifier
) {
    val iconAndColor = getNotificationStyle(notification.type, notification.priority)
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeString = sdf.format(Date(notification.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRead(notification) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconAndColor.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconAndColor.icon,
                    contentDescription = null,
                    tint = iconAndColor.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeString,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (!notification.isRead) {
                        TextButton(onClick = { onRead(notification) }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                            Text("مقروء", fontSize = 12.sp)
                        }
                    }
                    IconButton(onClick = { onArchive(notification) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Archive, contentDescription = "أرشفة", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { onDelete(notification) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

data class NotificationStyle(val icon: ImageVector, val color: Color)

@Composable
fun getNotificationStyle(type: String, priority: String): NotificationStyle {
    val color = when (priority) {
        "CRITICAL" -> MaterialTheme.colorScheme.error
        "HIGH" -> Color(0xFFF57C00) // Orange
        "MEDIUM" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }

    val icon = when (type) {
        "PAYMENT" -> Icons.Default.Payments
        "NEW_DEBT" -> Icons.Default.AccountBalanceWallet
        "DUE_DATE" -> Icons.Default.EventBusy
        "NEW_SUBSCRIBER" -> Icons.Default.PersonAdd
        "ACCOUNT_UPDATE" -> Icons.Default.ManageAccounts
        "SYSTEM" -> Icons.Default.SettingsSystemDaydream
        "SECURITY" -> Icons.Default.Security
        "WHATSAPP" -> Icons.Default.ChatBubbleOutline
        else -> Icons.Default.Notifications
    }

    return NotificationStyle(icon, color)
}
