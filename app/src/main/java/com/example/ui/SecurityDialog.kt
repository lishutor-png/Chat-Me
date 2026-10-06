package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.ApiConfigStatusInfo
import com.example.data.model.ApiHealthState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SecurityDialog(
    apiStatus: ApiConfigStatusInfo = ApiConfigStatusInfo(),
    isCheckingApi: Boolean = false,
    onRecheckApi: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val checkedTimeText = remember(apiStatus.lastCheckedTimestamp) {
        if (apiStatus.lastCheckedTimestamp > 0L) {
            timeFormat.format(Date(apiStatus.lastCheckedTimestamp))
        } else {
            "Baru saja"
        }
    }

    val statusColor = when (apiStatus.state) {
        ApiHealthState.CONNECTED -> Color(0xFF2E7D32)
        ApiHealthState.CHECKING -> MaterialTheme.colorScheme.primary
        ApiHealthState.LIMITED_QUOTA -> Color(0xFFEF6C00)
        ApiHealthState.FALLBACK_READY -> Color(0xFF1565C0)
        ApiHealthState.DISCONNECTED -> MaterialTheme.colorScheme.error
    }

    val statusBgColor = when (apiStatus.state) {
        ApiHealthState.CONNECTED -> Color(0xFFE8F5E9)
        ApiHealthState.CHECKING -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ApiHealthState.LIMITED_QUOTA -> Color(0xFFFFF3E0)
        ApiHealthState.FALLBACK_READY -> Color(0xFFE3F2FD)
        ApiHealthState.DISCONNECTED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("security_dialog_card")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Flat Design Chat Logo Header
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_flat_chat_logo_1791272084010),
                        contentDescription = "Logo Chat Flat Design",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Status API Config & Keamanan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Informasi koneksi API real-time dan perlindungan privasi obrolanmu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Live API Config Status Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusBgColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_config_status_panel")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = apiStatus.summaryTitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                            if (apiStatus.latencyMs != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = statusColor.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${apiStatus.latencyMs} ms",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = apiStatus.detailMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Per-slot breakdown if available
                        if (apiStatus.slotStatuses.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = statusColor.copy(alpha = 0.2f)
                            )
                            Text(
                                text = "Detail Akses Slot API Key:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            apiStatus.slotStatuses.forEach { slot ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Slot ${slot.slotIndex} (${slot.maskedKey})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (slot.isConnected) "✅ Bisa Diakses (${slot.latencyMs}ms)" else "⚠️ ${slot.statusLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (slot.isConnected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dicek: $checkedTimeText",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = onRecheckApi,
                                    enabled = !isCheckingApi,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("recheck_api_button")
                                ) {
                                    if (isCheckingApi) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Mengecek...", style = MaterialTheme.typography.labelSmall)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Cek Ulang API",
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cek Akses API", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SecurityFeatureRow(
                    icon = Icons.Default.SyncAlt,
                    title = "Multi-Key Auto-Failover & Round-Robin",
                    description = "Jika salah satu slot API key mencapai batas kuota, sistem otomatis beralih ke slot berikutnya tanpa memutus obrolan."
                )

                Spacer(modifier = Modifier.height(10.dp))

                SecurityFeatureRow(
                    icon = Icons.Default.Lock,
                    title = "Enkripsi Lokal AES-256 GCM",
                    description = "Setiap pesan dan konfigurasi disimpan secara aman di perangkat ini."
                )

                Spacer(modifier = Modifier.height(10.dp))

                SecurityFeatureRow(
                    icon = Icons.Default.Shield,
                    title = "Zona Curhat Bebas Sensor & Anti-Ban",
                    description = "Tetap aman merespon obrolan emosional maupun kata-kata sensitif tanpa takut akun diblokir."
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenSettings()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Atur API Key")
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("close_security_dialog_button")
                    ) {
                        Text("Tutup")
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(22.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
