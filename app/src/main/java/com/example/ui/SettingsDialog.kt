package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    currentConfig: CompanionConfig,
    apiStatus: ApiConfigStatusInfo = ApiConfigStatusInfo(),
    isCheckingApi: Boolean = false,
    onTestApiConfig: (CompanionConfig) -> Unit = {},
    onPickGalleryPhoto: (Uri, Boolean, (String) -> Unit) -> Unit = { uri, _, cb -> cb(uri.toString()) },
    onSave: (CompanionConfig) -> Unit,
    onClearChat: () -> Unit,
    onDismiss: () -> Unit
) {
    var botName by remember { mutableStateOf(currentConfig.botName) }
    var userName by remember { mutableStateOf(currentConfig.userName) }
    var avatarUri by remember { mutableStateOf(currentConfig.avatarUri) }
    var userAvatarUri by remember { mutableStateOf(currentConfig.userAvatarUri) }
    var selectedPersonality by remember { mutableStateOf(currentConfig.personality) }
    var selectedStyle by remember { mutableStateOf(currentConfig.languageStyle) }
    var mode by remember { mutableStateOf(currentConfig.mode) }
    var filterLevel by remember { mutableStateOf(currentConfig.filterLevel) }
    val initialKeys = remember(currentConfig) {
        val list = currentConfig.getActiveApiKeys().toMutableList()
        if (list.isEmpty()) {
            list.add("")
        }
        list
    }
    var apiKeys by remember { mutableStateOf(initialKeys.toList()) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    // Photo Picker for Bot Profile Photo
    val botPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickGalleryPhoto(uri, false) { savedPath ->
                avatarUri = savedPath
            }
        }
    }

    // Photo Picker for User Profile Photo
    val userPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickGalleryPhoto(uri, true) { savedPath ->
                userAvatarUri = savedPath
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .padding(vertical = 8.dp)
                .testTag("settings_dialog_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with Flat Design Chat Logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_flat_chat_logo),
                            contentDescription = "Logo Chat Flat",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pengaturan Profil & API",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kustomisasi foto profil, karakter & slot API",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Foto Profil & Nama Panggilan
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "1. Foto Profil & Nama Panggilan",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Dual Avatar Preview Row (Bot Profile & User Profile)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Bot Avatar Picker Column
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.BottomEnd,
                                        modifier = Modifier
                                            .clickable {
                                                botPhotoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                            .testTag("change_bot_avatar_box")
                                    ) {
                                        ProfileAvatar(
                                            avatarUri = avatarUri,
                                            fallbackName = botName.ifBlank { "Aria" },
                                            isUser = false,
                                            size = 68.dp
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PhotoCamera,
                                                contentDescription = "Ganti Foto Teman Chat",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Foto ${botName.ifBlank { "Aria" }}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    TextButton(
                                        onClick = {
                                            botPhotoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .testTag("pick_bot_photo_button")
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pilih Galeri", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                // User Avatar Picker Column
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.BottomEnd,
                                        modifier = Modifier
                                            .clickable {
                                                userPhotoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                            .testTag("change_user_avatar_box")
                                    ) {
                                        ProfileAvatar(
                                            avatarUri = userAvatarUri.ifBlank { "preset:initials" },
                                            fallbackName = userName.ifBlank { "Kamu" },
                                            isUser = true,
                                            size = 68.dp
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PhotoCamera,
                                                contentDescription = "Ganti Foto Profilmu",
                                                tint = MaterialTheme.colorScheme.onSecondary,
                                                modifier = Modifier.padding(4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Foto ${userName.ifBlank { "Kamu" }}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    TextButton(
                                        onClick = {
                                            userPhotoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .testTag("pick_user_photo_button")
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pilih Galeri", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Preset Avatar Quick Selector for Bot
                            Text(
                                text = "Pilihan Cepat Gaya Foto Profil:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = avatarUri.isBlank() || avatarUri == "preset:aria",
                                    onClick = { avatarUri = "" },
                                    label = { Text("Default Aria", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("preset_avatar_aria")
                                )
                                FilterChip(
                                    selected = avatarUri == "preset:flat_chat",
                                    onClick = { avatarUri = "preset:flat_chat" },
                                    label = { Text("Logo Flat", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("preset_avatar_flat")
                                )
                                FilterChip(
                                    selected = avatarUri == "preset:initials",
                                    onClick = { avatarUri = "preset:initials" },
                                    label = { Text("Inisial", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("preset_avatar_initials")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = botName,
                                onValueChange = { botName = it },
                                label = { Text("Nama Dia (Misal: Aria, Mimi, Sayang)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("bot_name_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                label = { Text("Nama Kamu (Misal: Lutfi, Mas, Kamu)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("user_name_input")
                            )
                        }
                    }

                    // 2. Kepribadian
                    Column {
                        Text(
                            text = "2. Karakter & Kepribadian",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CompanionPersonality.values().forEach { personality ->
                                val isSelected = selectedPersonality == personality
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPersonality = personality }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedPersonality = personality }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = personality.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = personality.shortDesc,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Gaya Bicara
                    Column {
                        Text(
                            text = "3. Gaya Bahasa",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LanguageStyle.values().forEach { style ->
                                val isSelected = selectedStyle == style
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedStyle = style }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedStyle = style }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = style.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = style.exampleTone,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Mode Hubungan
                    Column {
                        Text(
                            text = "4. Mode Obrolan",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = mode == ChatMode.STANDARD,
                                onClick = { mode = ChatMode.STANDARD },
                                label = { Text("Teman Curhat") },
                                leadingIcon = if (mode == ChatMode.STANDARD) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = mode == ChatMode.MATURE,
                                onClick = { mode = ChatMode.MATURE },
                                label = { Text("Pacar Virtual") },
                                leadingIcon = if (mode == ChatMode.MATURE) {
                                    { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (filterLevel == ContentFilterLevel.OFF) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Proteksi Anti-Blokir & Sensor Chat",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = filterLevel.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    ContentFilterLevel.values().forEach { fl ->
                                        FilterChip(
                                            selected = filterLevel == fl,
                                            onClick = { filterLevel = fl },
                                            label = { Text(fl.displayName, style = MaterialTheme.typography.labelSmall) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Kunci API Gemini (Multi-Slot Bergantian / Round-Robin) + Info Koneksi
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "5. Slot Kunci API & Status Koneksi",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Bergantian otomatis & cek apakah API masih bisa diakses",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Live API Connection Status Banner inside Settings
                            val stateDotColor = when (apiStatus.state) {
                                ApiHealthState.CONNECTED -> Color(0xFF2E7D32)
                                ApiHealthState.CHECKING -> MaterialTheme.colorScheme.primary
                                ApiHealthState.LIMITED_QUOTA -> Color(0xFFEF6C00)
                                ApiHealthState.FALLBACK_READY -> Color(0xFF1565C0)
                                ApiHealthState.DISCONNECTED -> MaterialTheme.colorScheme.error
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, stateDotColor.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("settings_api_status_banner")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(9.dp)
                                                    .clip(CircleShape)
                                                    .background(stateDotColor)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = apiStatus.summaryTitle,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = stateDotColor
                                            )
                                        }
                                        if (apiStatus.latencyMs != null) {
                                            Text(
                                                text = "${apiStatus.latencyMs} ms",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = stateDotColor
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = apiStatus.detailMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Daftar Slot Kunci API
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                apiKeys.forEachIndexed { index, keyVal ->
                                    val slotInfo = apiStatus.slotStatuses.find { it.slotIndex == index + 1 }
                                    Column {
                                        OutlinedTextField(
                                            value = keyVal,
                                            onValueChange = { newVal ->
                                                val updated = apiKeys.toMutableList()
                                                updated[index] = newVal
                                                apiKeys = updated
                                            },
                                            label = {
                                                Text(
                                                    if (index == 0) "Kunci Slot 1 (Utama)"
                                                    else "Kunci Slot ${index + 1}"
                                                )
                                            },
                                            placeholder = { Text("AIzaSy...") },
                                            singleLine = true,
                                            trailingIcon = {
                                                if (apiKeys.size > 1) {
                                                    IconButton(
                                                        onClick = {
                                                            val updated = apiKeys.toMutableList()
                                                            updated.removeAt(index)
                                                            apiKeys = updated
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.DeleteOutline,
                                                            contentDescription = "Hapus Slot",
                                                            tint = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                } else if (keyVal.isNotBlank()) {
                                                    IconButton(
                                                        onClick = {
                                                            val updated = apiKeys.toMutableList()
                                                            updated[0] = ""
                                                            apiKeys = updated
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Clear,
                                                            contentDescription = "Kosongkan Kunci",
                                                            tint = MaterialTheme.colorScheme.outline
                                                        )
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        if (slotInfo != null && keyVal.isNotBlank()) {
                                            Text(
                                                text = if (slotInfo.isConnected) "   ✅ Slot ${index + 1}: ${slotInfo.statusLabel}"
                                                else "   ⚠️ Slot ${index + 1}: ${slotInfo.statusLabel}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (slotInfo.isConnected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Row: Tambah Slot & Cek Koneksi API
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        apiKeys = apiKeys + ""
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tambah Slot (${apiKeys.size})", style = MaterialTheme.typography.labelMedium)
                                }

                                Button(
                                    onClick = {
                                        val cleanKeys = apiKeys.map { it.trim() }.filter { it.isNotBlank() }
                                        val tempConfig = currentConfig.copy(
                                            customApiKey = cleanKeys.firstOrNull() ?: "",
                                            customApiKeys = cleanKeys
                                        )
                                        onTestApiConfig(tempConfig)
                                    },
                                    enabled = !isCheckingApi,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("test_api_in_settings_button")
                                ) {
                                    if (isCheckingApi) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onSecondary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Mengecek...", style = MaterialTheme.typography.labelMedium)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.WifiFind,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Cek Akses API", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Status badge
                            val validKeysCount = apiKeys.count { it.isNotBlank() }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (validKeysCount > 0) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    text = when {
                                        validKeysCount > 1 -> "✅ $validKeysCount Kunci Tersedia — Rotasi Bergantian Aktif"
                                        validKeysCount == 1 -> "✅ 1 Kunci Aktif (Bisa tambah slot lagi untuk bergantian)"
                                        else -> "ℹ️ Menggunakan API bawaan sistem / Mode Mandiri"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (validKeysCount > 0) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // 6. Hapus Riwayat
                    OutlinedButton(
                        onClick = { showClearConfirmation = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_chat_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bersihkan Riwayat Obrolan")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            val cleanKeys = apiKeys.map { it.trim() }.filter { it.isNotBlank() }
                            val updated = currentConfig.copy(
                                botName = botName.trim().ifBlank { "Aria" },
                                userName = userName.trim().ifBlank { "Kamu" },
                                avatarUri = avatarUri,
                                userAvatarUri = userAvatarUri,
                                personality = selectedPersonality,
                                languageStyle = selectedStyle,
                                mode = mode,
                                filterLevel = filterLevel,
                                customApiKey = cleanKeys.firstOrNull() ?: "",
                                customApiKeys = cleanKeys
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_settings_button")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Hapus Semua Obrolan?") },
            text = { Text("Semua pesan yang tersimpan di ponsel ini akan dihapus permanen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearChat()
                        showClearConfirmation = false
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
