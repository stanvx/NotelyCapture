package com.module.notelycompose.notes.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.module.notelycompose.core.security.AiSettingsUiState
import com.module.notelycompose.notes.presentation.settings.AISettingsIntent
import com.module.notelycompose.notes.presentation.settings.AISettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * AI Settings screen for secure OpenAI API key management.
 * Follows Material 3 design principles with comprehensive security features.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AISettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentApiKey by viewModel.currentApiKey.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    LaunchedEffect(Unit) {
        viewModel.onProcessIntent(AISettingsIntent.LoadSettings)
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "AI Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Privacy Information Card
            PrivacyInfoCard()
            
            // API Key Configuration Section
            ApiKeyConfigurationSection(
                uiState = uiState,
                currentApiKey = currentApiKey,
                onApiKeyChange = { viewModel.onProcessIntent(AISettingsIntent.UpdateApiKey(it)) },
                onToggleVisibility = { viewModel.onProcessIntent(AISettingsIntent.ToggleApiKeyVisibility) },
                onSaveApiKey = { 
                    keyboardController?.hide()
                    viewModel.onProcessIntent(AISettingsIntent.SaveApiKey)
                },
                onRemoveApiKey = { viewModel.onProcessIntent(AISettingsIntent.RemoveApiKey) },
                onClearErrors = { viewModel.onProcessIntent(AISettingsIntent.ClearErrors) }
            )
            
            // Status Section
            if (uiState.hasApiKey) {
                ApiKeyStatusCard(uiState = uiState)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PrivacyInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Privacy & Security",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                text = "Your OpenAI API key is encrypted and stored securely on your device using Android Keystore. " +
                      "It's never transmitted to our servers and is only used for direct communication with OpenAI's services.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Justify
            )
            
            Text(
                text = "• API keys are encrypted using AES-256 encryption\n" +
                      "• Keys are stored in Android's secure hardware-backed Keystore\n" +
                      "• No API key data leaves your device\n" +
                      "• You can remove your key at any time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ApiKeyConfigurationSection(
    uiState: AiSettingsUiState,
    currentApiKey: String,
    onApiKeyChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onSaveApiKey: () -> Unit,
    onRemoveApiKey: () -> Unit,
    onClearErrors: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "OpenAI API Key",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        
        // API Key Input Field
        OutlinedTextField(
            value = currentApiKey,
            onValueChange = { 
                onClearErrors()
                onApiKeyChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter your OpenAI API key") },
            placeholder = { Text("sk-...") },
            visualTransformation = if (uiState.showApiKey) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                Row {
                    // Visibility toggle
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            imageVector = if (uiState.showApiKey) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (uiState.showApiKey) {
                                "Hide API key"
                            } else {
                                "Show API key"
                            }
                        )
                    }
                }
            },
            isError = uiState.validationError != null,
            supportingText = {
                uiState.validationError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onSaveApiKey() }
            ),
            singleLine = true
        )
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Save Button
            Button(
                onClick = onSaveApiKey,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isSaving && currentApiKey.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (uiState.hasApiKey) "Update" else "Save")
            }
            
            // Remove Button (only show if API key exists)
            if (uiState.hasApiKey) {
                OutlinedButton(
                    onClick = onRemoveApiKey,
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remove")
                }
            }
        }
        
        // Save Error Display
        uiState.saveError?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ApiKeyStatusCard(
    uiState: AiSettingsUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (uiState.hasApiKey) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
                
                Text(
                    text = if (uiState.hasApiKey) {
                        "API key configured and ready"
                    } else {
                        "No API key configured"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}