package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.EngineCapabilities
import com.example.engine.ManagedSpaceInfo
import com.example.ui.components.MonoBrandMark
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoStatusPill
import com.example.ui.theme.MonoTheme

@Composable
fun OnboardingScreen(
    capabilities: EngineCapabilities,
    spaces: List<ManagedSpaceInfo>,
    onInitiateProvisioning: () -> Unit,
    onFinishOnboarding: () -> Unit
) {
    val colors = MonoTheme.colors
    var step by remember { mutableIntStateOf(0) }
    val hasWorkSpace = spaces.any { it.isManagedProfile }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primaryBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 580.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar with step indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MonoBrandMark(size = 28.dp)
                    Text(
                        text = "MONO DUAL-SPACE",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                }
                Text(
                    text = "0${step + 1} / 04",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
            }

            // Middle content
            AnimatedContent(
                targetState = step,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 24.dp),
                label = "onboarding_step"
            ) { currentStep ->
                when (currentStep) {
                    0 -> OnboardingIntroStep()
                    1 -> OnboardingArchitectureStep()
                    2 -> OnboardingConsentStep()
                    else -> OnboardingSetupStep(
                        capabilities = capabilities,
                        hasWorkSpace = hasWorkSpace,
                        onInitiateProvisioning = onInitiateProvisioning
                    )
                }
            }

            // Bottom navigation controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    TextButton(
                        onClick = { step -= 1 },
                        modifier = Modifier.testTag("onboarding_back_button")
                    ) {
                        Text("Back", color = colors.textSecondary)
                    }
                } else {
                    TextButton(
                        onClick = onFinishOnboarding,
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text("Skip Setup", color = colors.textSecondary)
                    }
                }

                if (step < 3) {
                    Button(
                        onClick = { step += 1 },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.onAccent
                        ),
                        modifier = Modifier.testTag("onboarding_next_button")
                    ) {
                        Text("Continue")
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onFinishOnboarding,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.onAccent
                        ),
                        modifier = Modifier.testTag("onboarding_finish_button")
                    ) {
                        Text("Open Dashboard")
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingIntroStep() {
    val colors = MonoTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(
            text = "One device.\nMultiple spaces.",
            style = MaterialTheme.typography.displaySmall,
            color = colors.textPrimary
        )
        Text(
            text = "Mono Dual-Space is an open-source, privacy-first utility for isolating secondary app instances on Android without subscriptions, ads, or accounts.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary
        )
        MonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PillarRow(
                    icon = Icons.Outlined.Shield,
                    title = "100% Free & Open Source",
                    body = "Apache 2.0 licensed. Zero paywalls, zero analytics SDKs, zero network permission."
                )
                PillarRow(
                    icon = Icons.Outlined.Lock,
                    title = "Offline-First Privacy",
                    body = "All clone labels, PIN hashes, and space preferences stay strictly on your device."
                )
            }
        }
    }
}

@Composable
private fun OnboardingArchitectureStep() {
    val colors = MonoTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        MonoStatusPill(text = "ARCHITECTURE", toneColor = colors.accent)
        Text(
            text = "How Android Work Profiles isolate apps",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary
        )
        Text(
            text = "Instead of modifying or repacking APK binaries, Mono Dual-Space uses Android's built-in Managed Work Profile (DevicePolicyManager).",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary
        )
        MonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PillarRow(
                    icon = Icons.Outlined.Layers,
                    title = "OS-Enforced Data Separation",
                    body = "Apps inside the Work Space receive their own isolated Android user storage, credentials, and notifications."
                )
                PillarRow(
                    icon = Icons.Outlined.VerifiedUser,
                    title = "Original Signatures Preserved",
                    body = "Apps keep their cryptographic signatures and Google Play / system integrity intact."
                )
            }
        }
    }
}

@Composable
private fun OnboardingConsentStep() {
    val colors = MonoTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        MonoStatusPill(text = "CONSENT & PERMISSIONS", toneColor = colors.accent)
        Text(
            text = "Explicit control, never silent setup",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary
        )
        Text(
            text = "Mono Dual-Space never provisions profiles or installs applications silently. Every space action requires Android's system consent prompt.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary
        )
        MonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "What Mono Dual-Space requests:",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                Text(
                    text = "• Device Admin / Profile Owner role (only inside the isolated Work Profile if you choose to provision one).\n" +
                        "• Launcher & Package Visibility (<queries>) to list launchable apps.\n" +
                        "• BiometricPrompt (optional, only if you enable local app lock).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun OnboardingSetupStep(
    capabilities: EngineCapabilities,
    hasWorkSpace: Boolean,
    onInitiateProvisioning: () -> Unit
) {
    val colors = MonoTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        MonoStatusPill(
            text = if (hasWorkSpace) "SPACE READY" else if (capabilities.isProvisioningAllowed) "COMPATIBLE" else "CHECK DEVICE",
            toneColor = if (hasWorkSpace || capabilities.isProvisioningAllowed) colors.success else colors.warning
        )
        Text(
            text = "Device Space Compatibility",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary
        )
        MonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (hasWorkSpace || capabilities.isProvisioningAllowed) {
                            Icons.Outlined.CheckCircle
                        } else {
                            Icons.Outlined.WarningAmber
                        },
                        contentDescription = null,
                        tint = if (hasWorkSpace || capabilities.isProvisioningAllowed) colors.success else colors.warning
                    )
                    Text(
                        text = capabilities.statusSummary,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                }

                capabilities.limitationNotes.forEach { note ->
                    Text(
                        text = "• $note",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }

                if (!hasWorkSpace && capabilities.isProvisioningAllowed) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onInitiateProvisioning,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_provision_button")
                    ) {
                        Text(
                            text = "Initiate Work Profile Setup",
                            color = colors.accent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else if (!hasWorkSpace && !capabilities.isProvisioningAllowed) {
                    Text(
                        text = "You can still inspect installed apps, organize dashboard shortcuts, and configure privacy lock—or review setup diagnostics in Spaces.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun PillarRow(
    icon: ImageVector,
    title: String,
    body: String
) {
    val colors = MonoTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}
