package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.ui.theme.*
import com.example.viewmodel.AppUiState
import kotlinx.coroutines.launch

@Composable
fun SayoriScreen(
    uiState: AppUiState,
    onSendMessage: (String) -> Unit,
    onSimulateVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Auto-scroll on new message
    LaunchedEffect(uiState.chatMessages.size, uiState.isSayoriTyping) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size)
        }
    }

    val quickPrompts = listOf(
        "Ver meus pets",
        "Meus alertas",
        "Dúvida frequente",
        "Temperatura alta é perigoso?",
        "Como funciona a cerca?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // Header da Sayori com Avatar e Status
        SayoriHeader()

        HorizontalDivider(color = KpBorder)

        // Lista de Mensagens
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(uiState.chatMessages, key = { it.id }) { msg ->
                ChatBubbleItem(message = msg)
            }

            // Indicador de "Sayori está digitando..."
            if (uiState.isSayoriTyping) {
                item {
                    SayoriTypingIndicator()
                }
            }
        }

        // Overlay de Gravação de Áudio Ativa
        AnimatedVisibility(visible = uiState.isRecordingAudio) {
            AudioRecordingBar()
        }

        // Botões Rápidos
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                AssistChip(
                    onClick = {
                        onSendMessage(prompt)
                    },
                    label = {
                        Text(
                            text = prompt,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = KpGreenDark
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = KpGreenUltralight
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = KpGreenAccent.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("quick_prompt_${prompt.take(6)}")
                )
            }
        }

        // Barra de Input (Texto e Áudio)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "Pergunte à Sayori sobre a saúde do seu pet...",
                        fontSize = 13.sp,
                        color = KpTextMuted
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("sayori_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KpGreenPrimary,
                    unfocusedBorderColor = KpBorder,
                    focusedContainerColor = KpBackground,
                    unfocusedContainerColor = KpBackground
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                            focusManager.clearFocus()
                        }
                    }
                ),
                maxLines = 3
            )

            // Botão de Áudio
            IconButton(
                onClick = onSimulateVoice,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (uiState.isRecordingAudio) KpRedAlert else KpGreenUltralight)
                    .testTag("sayori_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Falar por áudio",
                    tint = if (uiState.isRecordingAudio) Color.White else KpGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Botão de Enviar
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                        focusManager.clearFocus()
                    }
                },
                enabled = inputText.isNotBlank(),
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) KpGreenPrimary else KpSurfaceVariant)
                    .testTag("sayori_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Enviar mensagem",
                    tint = if (inputText.isNotBlank()) Color.White else KpTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SayoriHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(48.dp)) {
            Image(
                painter = painterResource(id = R.drawable.sayori_avatar),
                contentDescription = "Sayori Assistente Veterinária",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, KpGreenAccent, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(KpGreenAccent)
                    .border(2.dp, Color.White, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Sayori",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = KpTextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(KpGreenUltralight)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "VET IA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = KpGreenDark
                    )
                }
            }
            Text(
                text = "Online • Assistente de Saúde K&P",
                fontSize = 12.sp,
                color = KpGreenPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ChatBubbleItem(message: ChatMessage) {
    val isSayori = message.sender == MessageSender.SAYORI

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSayori) Arrangement.Start else Arrangement.End
    ) {
        if (isSayori) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.dp, KpGreenAccent, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sayori_avatar),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isSayori) Alignment.Start else Alignment.End,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isSayori) 4.dp else 18.dp,
                            bottomEnd = if (isSayori) 18.dp else 4.dp
                        )
                    )
                    .background(if (isSayori) KpGreenUltralight else KpBlueUltralight)
                    .border(
                        1.dp,
                        if (isSayori) KpGreenAccent.copy(alpha = 0.35f) else KpBlueAccent.copy(alpha = 0.35f),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    if (message.isAudio) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = KpBlueData,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Áudio enviado",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KpBlueData
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        color = KpTextPrimary,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = message.time,
                fontSize = 10.sp,
                color = KpTextMuted
            )
        }
    }
}

@Composable
private fun SayoriTypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 40.dp)
    ) {
        val transition = rememberInfiniteTransition(label = "typing")
        val alpha by transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(600), repeatMode = RepeatMode.Reverse),
            label = "typing_alpha"
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(KpGreenUltralight)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(KpGreenPrimary.copy(alpha = alpha)))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(KpGreenPrimary.copy(alpha = alpha * 0.8f)))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(KpGreenPrimary.copy(alpha = alpha * 0.5f)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sayori está consultando os sinais vitais...",
                    fontSize = 11.sp,
                    color = KpGreenDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AudioRecordingBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KpRedLight)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(KpRedAlert)
        )
        Text(
            text = "Gravando áudio para Sayori... Fale agora",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = KpRedDark,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            tint = KpRedAlert,
            modifier = Modifier.size(20.dp)
        )
    }
}
