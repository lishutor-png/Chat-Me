package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ApiConfigStatusInfo
import com.example.data.model.ApiHealthState
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMode
import com.example.data.model.CompanionConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val streamingMessage by viewModel.streamingMessage.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val apiStatus by viewModel.apiStatus.collectAsStateWithLifecycle()
    val isCheckingApi by viewModel.isCheckingApi.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(true) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }

    // Filter out empty streaming placeholders so we never render a blank bubble
    val visibleMessages = remember(messages) {
        messages.filter { !(it.isStreaming && it.text.isBlank()) }
    }
    val showTypingIndicator = isGenerating && (streamingMessage == null || streamingMessage?.text.isNullOrBlank())

    // Quick Photo Picker from TopBar avatar
    val quickAvatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.copyGalleryImageToInternalStorage(uri, "bot_avatar") { savedPath ->
                viewModel.updateConfig(config.copy(avatarUri = savedPath))
                Toast.makeText(context, "Foto profil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Auto-scroll to bottom when new messages arrive or typing indicator appears
    LaunchedEffect(visibleMessages.size, showTypingIndicator, streamingMessage?.text?.length) {
        val totalItems = visibleMessages.size + if (showTypingIndicator) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    val apiDotColor = when (apiStatus.state) {
        ApiHealthState.CONNECTED -> Color(0xFF4CAF50)
        ApiHealthState.CHECKING -> Color(0xFF2196F3)
        ApiHealthState.LIMITED_QUOTA -> Color(0xFFFF9800)
        ApiHealthState.FALLBACK_READY -> Color(0xFF03A9F4)
        ApiHealthState.DISCONNECTED -> Color(0xFFE53935)
    }

    // Root Column with proper WindowInsets so Header never pans off-screen and Footer sits cleanly above IME
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Pinned Top Header
        ChatTopHeader(
            config = config,
            isGenerating = isGenerating,
            apiStatus = apiStatus,
            apiDotColor = apiDotColor,
            onAvatarClick = {
                quickAvatarPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onTitleClick = { showSettingsDialog = true },
            onStatusChipClick = { showSecurityDialog = true },
            onSettingsClick = { showSettingsDialog = true }
        )

        // 2. Conversation Messages Area (Resizes smoothly when keyboard opens)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
        ) {
            items(
                items = visibleMessages,
                key = { "${it.id}_${it.timestamp}_${it.isStreaming}" }
            ) { msg ->
                ChatMessageItem(
                    message = msg,
                    botName = config.botName,
                    userName = config.userName,
                    botAvatarUri = config.avatarUri,
                    userAvatarUri = config.userAvatarUri,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Pesan", msg.text))
                        Toast.makeText(context, "Pesan disalin!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (showTypingIndicator) {
                item(key = "typing_indicator_row") {
                    TypingIndicatorRow(
                        botName = config.botName,
                        botAvatarUri = config.avatarUri
                    )
                }
            }
        }

        // 3. Bottom Input & Quick Suggestions Footer (Respects navigation bar & keyboard IME)
        ChatBottomFooter(
            botName = config.botName,
            inputText = inputText,
            isGenerating = isGenerating,
            showSuggestions = showSuggestions && inputText.isEmpty() && !isGenerating,
            onToggleSuggestions = { showSuggestions = !showSuggestions },
            onInputChange = { inputText = it },
            onSelectSuggestion = { suggestion ->
                viewModel.sendMessage(suggestion)
            },
            onSend = {
                val textToSend = inputText.trim()
                if (textToSend.isNotEmpty() && !isGenerating) {
                    inputText = ""
                    viewModel.sendMessage(textToSend)
                }
            }
        )
    }

    // Dialogs
    if (showSettingsDialog) {
        SettingsDialog(
            currentConfig = config,
            apiStatus = apiStatus,
            isCheckingApi = isCheckingApi,
            onTestApiConfig = { tempCfg -> viewModel.verifyApiConnection(tempCfg) },
            onPickGalleryPhoto = { uri, isUser, onSaved ->
                viewModel.copyGalleryImageToInternalStorage(
                    uri = uri,
                    prefix = if (isUser) "user_avatar" else "bot_avatar",
                    onResult = onSaved
                )
            },
            onSave = { viewModel.updateConfig(it) },
            onClearChat = { viewModel.clearHistory() },
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showSecurityDialog) {
        SecurityDialog(
            apiStatus = apiStatus,
            isCheckingApi = isCheckingApi,
            onRecheckApi = { viewModel.verifyApiConnection(config) },
            onOpenSettings = { showSettingsDialog = true },
            onDismiss = { showSecurityDialog = false }
        )
    }
}

@Composable
private fun ChatTopHeader(
    config: CompanionConfig,
    isGenerating: Boolean,
    apiStatus: ApiConfigStatusInfo,
    apiDotColor: Color,
    onAvatarClick: () -> Unit,
    onTitleClick: () -> Unit,
    onStatusChipClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with quick photo picker & pulsing status dot
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .clickable(onClick = onAvatarClick)
                    .testTag("top_bar_avatar_button")
            ) {
                ProfileAvatar(
                    avatarUri = config.avatarUri,
                    fallbackName = config.botName,
                    isUser = false,
                    size = 42.dp
                )
                PulsingStatusDot(color = apiDotColor)
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Name, Mode Pill & Single-line Status Subtitle
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onTitleClick)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = config.botName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (config.mode == ChatMode.MATURE) Color(0xFFFCE4EC) else MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = if (config.mode == ChatMode.MATURE) "Pacar 💕" else "Curhat ☕",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false,
                            color = if (config.mode == ChatMode.MATURE) Color(0xFFC2185B) else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isGenerating) {
                        "Sedang mengetik balasan..."
                    } else {
                        apiStatus.summaryTitle
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isGenerating) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        when (apiStatus.state) {
                            ApiHealthState.CONNECTED -> Color(0xFF2E7D32)
                            ApiHealthState.DISCONNECTED -> MaterialTheme.colorScheme.error
                            ApiHealthState.LIMITED_QUOTA -> Color(0xFFEF6C00)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    },
                    modifier = Modifier.testTag("top_bar_api_status_text")
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Compact API Status Chip
            val badgeLabel = when (apiStatus.state) {
                ApiHealthState.CONNECTED -> "API Aktif"
                ApiHealthState.CHECKING -> "Cek API"
                ApiHealthState.LIMITED_QUOTA -> "Limit API"
                ApiHealthState.FALLBACK_READY -> "Mandiri"
                ApiHealthState.DISCONNECTED -> "API Off"
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = apiDotColor.copy(alpha = 0.14f),
                border = androidx.compose.foundation.BorderStroke(1.dp, apiDotColor.copy(alpha = 0.4f)),
                modifier = Modifier
                    .clickable(onClick = onStatusChipClick)
                    .testTag("e2e_badge_chip")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(apiDotColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Settings Button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("open_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Pengaturan Chat"
                )
            }
        }
    }
}

@Composable
private fun ChatBottomFooter(
    botName: String,
    inputText: String,
    isGenerating: Boolean,
    showSuggestions: Boolean,
    onToggleSuggestions: () -> Unit,
    onInputChange: (String) -> Unit,
    onSelectSuggestion: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(top = 6.dp, bottom = 8.dp)
        ) {
            // Collapsible Quick Curhat Suggestions (Auto-hides while typing so footer stays clean)
            AnimatedVisibility(
                visible = showSuggestions,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                QuickCurhatSuggestions(
                    onSelectSuggestion = onSelectSuggestion
                )
            }

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Toggle Quick Suggestions Icon Button
                IconButton(
                    onClick = onToggleSuggestions,
                    modifier = Modifier
                        .size(42.dp)
                        .padding(bottom = 2.dp)
                ) {
                    Icon(
                        imageVector = if (showSuggestions) Icons.Default.KeyboardArrowDown else Icons.Default.AutoAwesome,
                        contentDescription = "Ide Topik Curhat",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Rounded Chat Input Box
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = {
                        Text(
                            text = "Tulis pesan ke $botName...",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.outline
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSend() }
                    ),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("chat_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                val canSend = inputText.isNotBlank() && !isGenerating
                FilledIconButton(
                    onClick = onSend,
                    enabled = canSend,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .size(46.dp)
                        .padding(bottom = 1.dp)
                        .testTag("send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Kirim Pesan",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileAvatar(
    avatarUri: String,
    fallbackName: String,
    isUser: Boolean,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isUser) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val initials = remember(fallbackName) {
        val clean = fallbackName.trim()
        if (clean.isEmpty()) "A" else clean.take(2).uppercase()
    }

    when {
        avatarUri == "preset:initials" -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(
                        if (isUser) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                    .border(1.5.dp, borderColor, CircleShape)
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        avatarUri == "preset:flat_chat" -> {
            Image(
                painter = painterResource(id = R.drawable.img_flat_chat_logo_1791272084010),
                contentDescription = "Avatar $fallbackName",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(1.5.dp, borderColor, CircleShape)
            )
        }
        avatarUri.isNotBlank() && avatarUri != "preset:aria" -> {
            val imageModel: Any = remember(avatarUri) {
                if (avatarUri.startsWith("/")) {
                    File(avatarUri)
                } else {
                    Uri.parse(avatarUri)
                }
            }
            AsyncImage(
                model = imageModel,
                contentDescription = "Avatar $fallbackName",
                error = painterResource(id = R.drawable.aria_avatar),
                placeholder = painterResource(id = R.drawable.aria_avatar),
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(1.5.dp, borderColor, CircleShape)
            )
        }
        else -> {
            AsyncImage(
                model = R.drawable.aria_avatar,
                contentDescription = "Avatar $fallbackName",
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(1.5.dp, borderColor, CircleShape)
            )
        }
    }
}

@Composable
private fun QuickCurhatSuggestions(
    onSelectSuggestion: (String) -> Unit
) {
    val suggestions = listOf(
        "Lagi capek banget hari ini... 🥺",
        "Kangen kamu, lagi apa sekarang? 💕",
        "Butuh peluk virtual & semangat 🫂",
        "Tadi ada hal yang bikin sedih... 🌧️",
        "Temenin ngobrol malam ini ya? 🌙",
        "Boleh curhat sesuatu nggak? ☕"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(suggestions) { text ->
            SuggestionChip(
                onClick = { onSelectSuggestion(text) },
                label = {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.height(30.dp)
            )
        }
    }
}

private fun formatMessageAnnotatedText(rawText: String, isStreaming: Boolean): AnnotatedString {
    val cleaned = rawText.trim()
    return buildAnnotatedString {
        var i = 0
        while (i < cleaned.length) {
            if (i + 1 < cleaned.length && cleaned[i] == '*' && cleaned[i + 1] == '*') {
                val closeIdx = cleaned.indexOf("**", i + 2)
                if (closeIdx != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(cleaned.substring(i + 2, closeIdx))
                    }
                    i = closeIdx + 2
                    continue
                }
            }
            append(cleaned[i])
            i++
        }
        if (isStreaming) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(" ▍")
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    botName: String,
    userName: String,
    botAvatarUri: String,
    userAvatarUri: String,
    onCopy: () -> Unit
) {
    val isUser = message.sender == "user"
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }
    val formattedText = remember(message.text, message.isStreaming) {
        formatMessageAnnotatedText(message.text, message.isStreaming)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            ProfileAvatar(
                avatarUri = botAvatarUri,
                fallbackName = botName,
                isUser = false,
                size = 32.dp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = if (isUser) {
                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
            } else {
                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
            },
            color = if (isUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            },
            tonalElevation = if (isUser) 0.dp else 1.dp,
            modifier = Modifier
                .widthIn(min = 68.dp, max = 286.dp)
                .testTag(if (isUser) "user_message_bubble" else "assistant_message_bubble")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)
            ) {
                SelectionContainer {
                    Text(
                        text = formattedText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp
                        ),
                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Compact bottom-right metadata row (does NOT force bubble to full width)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = if (isUser) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        }
                    )

                    if (isUser) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Terkirim",
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            modifier = Modifier.size(13.dp)
                        )
                    } else if (message.text.isNotBlank() && !message.isStreaming) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin Pesan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            modifier = Modifier
                                .size(13.dp)
                                .clickable(onClick = onCopy)
                        )
                    }
                }
            }
        }

        if (isUser && userAvatarUri.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            ProfileAvatar(
                avatarUri = userAvatarUri,
                fallbackName = userName,
                isUser = true,
                size = 32.dp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@Composable
private fun TypingIndicatorRow(
    botName: String,
    botAvatarUri: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileAvatar(
            avatarUri = botAvatarUri,
            fallbackName = botName,
            isUser = false,
            size = 30.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BouncingDots()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$botName sedang mengetik...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BouncingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 0, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .scale(dot1Scale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .scale(dot2Scale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .scale(dot3Scale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun PulsingStatusDot(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color)
            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
    )
}
