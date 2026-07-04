package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.DebtViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

// Add classes and data models for Health Status
enum class HealthStatus {
    HEALTHY, WARNING, CRITICAL, OFFLINE
}

data class HealthMetric(
    val title: String,
    val status: HealthStatus,
    val value: String,
    val icon: ImageVector,
    val lastCheck: String = "الآن",
    val healthPercentage: Int = 100
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemHealthScreen(
    viewModel: DebtViewModel
) {
    val networkStatus by viewModel.networkStatus.collectAsState()
    val syncLogs by viewModel.allSyncLogs.collectAsState()
    val whatsappLogs by viewModel.whatsappLogs.collectAsState()
    
    val isNetworkAvailable = networkStatus == com.example.data.network.NetworkStatus.Available
    
    var refreshing by remember { mutableStateOf(false) }
    var cpuUsage by remember { mutableFloatStateOf(0.12f) }
    var memoryUsage by remember { mutableFloatStateOf(0.45f) }
    
    LaunchedEffect(refreshing) {
        while(true) {
            delay(3000)
            cpuUsage = (0.1f + Random.nextFloat() * 0.3f).coerceIn(0f, 1f)
            memoryUsage = (0.4f + Random.nextFloat() * 0.1f).coerceIn(0f, 1f)
        }
    }

    val metrics = listOf(
        HealthMetric("حالة التطبيق", HealthStatus.HEALTHY, "متصل", Icons.Default.CheckCircle, healthPercentage = 100),
        HealthMetric("قاعدة البيانات", HealthStatus.HEALTHY, "24ms", Icons.Default.Storage, healthPercentage = 98),
        HealthMetric("الاتصال بالإنترنت", if (isNetworkAvailable) HealthStatus.HEALTHY else HealthStatus.OFFLINE, if (isNetworkAvailable) "متصل" else "غير متصل", Icons.Default.Wifi, healthPercentage = if (isNetworkAvailable) 100 else 0),
        HealthMetric("المزامنة السحابية", if (syncLogs.any { it.status == com.example.data.model.SyncStatus.FAILED }) HealthStatus.WARNING else HealthStatus.HEALTHY, "${syncLogs.count { it.status == com.example.data.model.SyncStatus.PENDING }} قيد الانتظار", Icons.Default.CloudSync, healthPercentage = 95),
        HealthMetric("خدمة واتساب", if (whatsappLogs.isNotEmpty()) HealthStatus.WARNING else HealthStatus.HEALTHY, "${whatsappLogs.size} رسالة معلقة", Icons.Default.Chat, healthPercentage = 90),
        HealthMetric("نظام الإشعارات", HealthStatus.HEALTHY, "نشط", Icons.Default.NotificationsActive, healthPercentage = 100),
        HealthMetric("خدمة التوثيق", HealthStatus.HEALTHY, "مستقر", Icons.Default.Security, healthPercentage = 100),
        HealthMetric("مساحة التخزين", HealthStatus.HEALTHY, "1.2 GB", Icons.Default.SdStorage, healthPercentage = 85),
        HealthMetric("خدمة النسخ الاحتياطي", HealthStatus.HEALTHY, "تمت الجدولة", Icons.Default.Backup, healthPercentage = 100),
        HealthMetric("حالة الأمان", HealthStatus.HEALTHY, "لا توجد تهديدات", Icons.Default.Shield, healthPercentage = 100),
        HealthMetric("خدمات API", HealthStatus.HEALTHY, "120ms", Icons.Default.Api, healthPercentage = 96),
        HealthMetric("المهام الخلفية", HealthStatus.HEALTHY, "2 قيد التشغيل", Icons.Default.SettingsSuggest, healthPercentage = 100)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مركز صحة النظام", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { 
                        refreshing = true 
                        // Simulate refresh
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Overview Header
            item {
                SystemOverviewCard(
                    cpuUsage = cpuUsage,
                    memoryUsage = memoryUsage
                )
            }

            // System Monitoring Grid
            item {
                SectionHeader("مراقبة النظام الشاملة", Icons.Default.MonitorHeart)
            }
            item {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 1000.dp),
                    userScrollEnabled = false
                ) {
                    items(metrics) { metric ->
                        HealthMetricCard(metric)
                    }
                }
            }

            // Performance & Analytics
            item {
                SectionHeader("الأداء والتحليلات", Icons.Default.Insights)
            }
            item {
                PerformanceCharts(cpuUsage, memoryUsage)
            }

            // Database & Storage
            item {
                SectionHeader("قاعدة البيانات والتخزين", Icons.Default.DataUsage)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DatabaseHealthCard(modifier = Modifier.weight(1f))
                    StorageHealthCard(modifier = Modifier.weight(1f))
                }
            }

            // Security & Errors
            item {
                SectionHeader("الأمان والأخطاء", Icons.Default.Security)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SecurityStatusCard(modifier = Modifier.weight(1f))
                    ErrorCenterCard(modifier = Modifier.weight(1f))
                }
            }

            // Maintenance Tools
            item {
                SectionHeader("أدوات الصيانة والإصلاح", Icons.Default.Build)
            }
            item {
                MaintenanceToolsCard(viewModel)
            }
        }
    }
}

@Composable
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
        ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("الحالة العامة للنظام", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ممتاز", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("وقت التشغيل: 14 يوم، 5 ساعات", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressMetric("المعالج", cpuUsage, MaterialTheme.colorScheme.primary)
                CircularProgressMetric("الذاكرة", memoryUsage, MaterialTheme.colorScheme.tertiary)
            }
        }
        }
    }
}

@Composable
fun CircularProgressMetric(label: String, progress: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = color.copy(alpha = 0.2f),
                strokeWidth = 6.dp
            )
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = color,
                strokeWidth = 6.dp
            )
            Text("${(progress * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun HealthMetricCard(metric: HealthMetric) {
    val statusColor = when (metric.status) {
        HealthStatus.HEALTHY -> Color(0xFF4CAF50)
        HealthStatus.WARNING -> Color(0xFFFFC107)
        HealthStatus.CRITICAL -> MaterialTheme.colorScheme.error
        HealthStatus.OFFLINE -> Color.Gray
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(metric.icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp))
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${metric.healthPercentage}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = statusColor)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowOutward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(metric.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(metric.value, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        when(metric.status) {
                            HealthStatus.HEALTHY -> "سليم"
                            HealthStatus.WARNING -> "تحذير"
                            HealthStatus.CRITICAL -> "حرج"
                            HealthStatus.OFFLINE -> "غير متصل"
                        },
                        fontSize = 10.sp,
                        color = statusColor
                    )
                }
                Text("آخر فحص: ${metric.lastCheck}", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun DatabaseHealthCard(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("صحة قواعد البيانات", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow("الاتصال", "مستقر", Color(0xFF4CAF50))
            InfoRow("وقت الاستجابة", "14ms", MaterialTheme.colorScheme.onSurface)
            InfoRow("حجم البيانات", "45 MB", MaterialTheme.colorScheme.onSurface)
            InfoRow("الاستعلامات النشطة", "12", MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun StorageHealthCard(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SdStorage, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("مراقبة التخزين", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow("المساحة الكلية", "64 GB", MaterialTheme.colorScheme.onSurface)
            InfoRow("المساحة المستخدمة", "1.2 GB", MaterialTheme.colorScheme.onSurface)
            InfoRow("المساحة المتاحة", "62.8 GB", Color(0xFF4CAF50))
            
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { 1.2f / 64f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun SecurityStatusCard(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF4CAF50))
                Spacer(modifier = Modifier.width(8.dp))
                Text("حالة الأمان", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow("محاولات الدخول", "14 ناجحة", Color(0xFF4CAF50))
            InfoRow("محاولات فاشلة", "0", Color(0xFF4CAF50))
            InfoRow("الحسابات المحظورة", "0", MaterialTheme.colorScheme.onSurface)
            InfoRow("حالة التشفير", "نشط", Color(0xFF4CAF50))
        }
    }
}

@Composable
fun ErrorCenterCard(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BugReport, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("مركز الأخطاء", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            InfoRow("أخطاء حديثة", "لا يوجد", Color(0xFF4CAF50))
            InfoRow("سجلات التحذير", "2 تحذير", Color(0xFFFFC107))
            InfoRow("أخطاء حرجة", "0", Color(0xFF4CAF50))
            InfoRow("تقارير الأعطال", "0", Color(0xFF4CAF50))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun PerformanceCharts(cpuUsage: Float, memoryUsage: Float) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("اتجاهات الأداء (آخر ساعة)", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            val primaryColor = MaterialTheme.colorScheme.primary
            val chartPoints = remember { List(20) { Random.nextFloat() * 40f + 10f } }
            
            Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                val width = size.width
                val height = size.height
                
                val path = Path()
                val stepX = width / (chartPoints.size - 1)
                val maxPoint = 60f
                
                chartPoints.forEachIndexed { index, point ->
                    val x = index * stepX
                    val y = height - (point / maxPoint * height)
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        val prevX = (index - 1) * stepX
                        val prevY = height - (chartPoints[index - 1] / maxPoint * height)
                        // Smooth bezier curve
                        path.cubicTo(
                            prevX + stepX / 2f, prevY,
                            x - stepX / 2f, y,
                            x, y
                        )
                    }
                }
                
                // Draw gradient under path
                val gradientPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                
                drawPath(
                    path = gradientPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
                
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx())
                )
                
                // Draw current point
                val lastY = height - (chartPoints.last() / maxPoint * height)
                drawCircle(
                    color = primaryColor,
                    radius = 6.dp.toPx(),
                    center = Offset(width, lastY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(width, lastY)
                )
            }
        }
    }
}

@Composable
fun MaintenanceToolsCard(viewModel: DebtViewModel) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val tools = listOf(
                Pair("فحص الصحة الشامل", Icons.Default.HealthAndSafety),
                Pair("تحسين قاعدة البيانات", Icons.Default.Speed),
                Pair("مسح الذاكرة المؤقتة", Icons.Default.CleaningServices),
                Pair("إعادة محاولة المزامنة", Icons.Default.SyncProblem),
                Pair("إعادة تشغيل الخدمات", Icons.Default.RestartAlt),
                Pair("إنشاء نسخة احتياطية", Icons.Default.Backup)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 400.dp),
                userScrollEnabled = false
            ) {
                items(tools) { (label, icon) ->
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
                }
            }
        }
    }
}
