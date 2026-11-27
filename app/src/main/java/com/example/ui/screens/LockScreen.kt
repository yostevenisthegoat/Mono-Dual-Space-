package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.MonoBrandMark
import com.example.ui.theme.MonoCodeFontFamily
import com.example.ui.theme.MonoTheme

@Composable
fun LockScreen(
    title: String = "Mono Dual-Space",
    subtitle: String = "Enter your local PIN to unlock spaces",
    errorMessage: String?,
    biometricEnabled: Boolean,
    biometricAvailable: Boolean,
    onSubmitPin: (String) -> Boolean,
    onRequestBiometric: () -> Unit,
    onClearError: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    val colors = MonoTheme.colors
    var enteredPin by remember { mutableStateOf("") }

    LaunchedEffect(biometricEnabled, biometricAvailable) {
        if (biometricEnabled && biometricAvailable) {
            onRequestBiometric()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primaryBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            MonoBrandMark(size = 44.dp)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // PIN dot indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                val maxDots = maxOf(6, enteredPin.length)
                repeat(maxDots) { idx ->
                    val filled = idx < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (filled) colors.accent else colors.secondaryBackground)
                            .border(
                                width = 1.dp,
                                color = if (filled) colors.accent else colors.border,
                                shape = CircleShape
                            )
                    )
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.danger,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("lock_error_text")
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            PinKeypad(
                onDigit = { digit ->
                    if (enteredPin.length < 8) {
                        onClearError()
                        enteredPin += digit
                    }
                },
                onBackspace = {
                    if (enteredPin.isNotEmpty()) {
                        onClearError()
                        enteredPin = enteredPin.dropLast(1)
                    }
                },
                onConfirm = {
                    if (enteredPin.length >= 4) {
                        val ok = onSubmitPin(enteredPin)
                        if (!ok) {
                            enteredPin = ""
                        }
                    }
                },
                canConfirm = enteredPin.length >= 4
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (biometricEnabled && biometricAvailable) {
                    OutlinedButton(
                        onClick = onRequestBiometric,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier.testTag("biometric_unlock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = "Unlock with biometrics",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Use Biometrics")
                    }
                }
                if (onDismiss != null) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = colors.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun PinKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit,
    canConfirm: Boolean
) {
    val colors = MonoTheme.colors
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                row.forEach { digit ->
                    PinKeyButton(
                        label = digit,
                        testTag = "pin_key_$digit",
                        onClick = { onDigit(digit) }
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Surface(
                onClick = onBackspace,
                shape = CircleShape,
                color = colors.secondaryBackground,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .size(72.dp)
                    .testTag("pin_backspace")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                        contentDescription = "Backspace",
                        tint = colors.textSecondary
                    )
                }
            }
            PinKeyButton(
                label = "0",
                testTag = "pin_key_0",
                onClick = { onDigit("0") }
            )
            Surface(
                onClick = onConfirm,
                enabled = canConfirm,
                shape = CircleShape,
                color = if (canConfirm) colors.accent else colors.secondaryBackground,
                border = BorderStroke(1.dp, if (canConfirm) colors.accent else colors.border),
                modifier = Modifier
                    .size(72.dp)
                    .testTag("pin_submit")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Confirm PIN",
                        tint = if (canConfirm) colors.onAccent else colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun PinKeyButton(
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    val colors = MonoTheme.colors
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = colors.cardBackground,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier
            .size(72.dp)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontFamily = MonoCodeFontFamily,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary
            )
        }
    }
}

@Composable
fun SetupPinDialogContent(
    onPinConfirmed: (String) -> Unit,
    onCancel: () -> Unit
) {
    val colors = MonoTheme.colors
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirmStep by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val activePin = if (isConfirmStep) confirmPin else firstPin

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = "PIN setup",
            tint = colors.accent,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = if (isConfirmStep) "Confirm Local PIN" else "Create Local PIN (4–8 digits)",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary
        )
        Text(
            text = "Stored locally using salted PBKDF2-HMAC-SHA256. Never uploaded.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(maxOf(4, activePin.length)) { idx ->
                val filled = idx < activePin.length
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (filled) colors.accent else colors.secondaryBackground)
                        .border(1.dp, colors.border, CircleShape)
                )
            }
        }

        if (error != null) {
            Text(
                text = error!!,
                style = MaterialTheme.typography.bodySmall,
                color = colors.danger
            )
        }

        PinKeypad(
            onDigit = { d ->
                error = null
                if (isConfirmStep) {
                    if (confirmPin.length < 8) confirmPin += d
                } else {
                    if (firstPin.length < 8) firstPin += d
                }
            },
            onBackspace = {
                error = null
                if (isConfirmStep) {
                    if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                } else {
                    if (firstPin.isNotEmpty()) firstPin = firstPin.dropLast(1)
                }
            },
            onConfirm = {
                if (!isConfirmStep) {
                    if (firstPin.length >= 4) {
                        isConfirmStep = true
                    }
                } else {
                    if (confirmPin == firstPin) {
                        onPinConfirmed(firstPin)
                    } else {
                        error = "PINs did not match. Try again."
                        confirmPin = ""
                    }
                }
            },
            canConfirm = activePin.length >= 4
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = colors.textSecondary)
            }
            if (isConfirmStep) {
                Button(
                    onClick = {
                        isConfirmStep = false
                        confirmPin = ""
                        error = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.secondaryBackground,
                        contentColor = colors.textPrimary
                    )
                ) {
                    Text("Reset")
                }
            }
        }
    }
}
