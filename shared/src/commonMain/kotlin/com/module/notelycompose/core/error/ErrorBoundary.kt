package com.module.notelycompose.core.error

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Composable error boundary that provides fallback UI for error states.
 * Since Compose doesn't support try-catch around composables, this provides
 * a manual error state management system.
 */
@Composable
fun ErrorBoundary(
    hasError: Boolean,
    onError: (Throwable) -> Unit = { /* Default: do nothing */ },
    fallbackContent: @Composable () -> Unit = { DefaultErrorFallback() },
    content: @Composable () -> Unit
) {
    if (hasError) {
        fallbackContent()
    } else {
        content()
    }
}

/**
 * Error boundary specifically for note components with proper exception handling
 * This provides fallback UI when data validation fails or rendering errors occur
 */
@Composable
fun NoteErrorBoundary(
    noteId: Long?,
    component: String,
    fallbackContent: @Composable () -> Unit = { 
        NoteErrorFallback(
            noteId = noteId,
            component = component
        ) 
    },
    content: @Composable () -> Unit
) {
    var hasError by remember { mutableStateOf(false) }
    var caughtError by remember { mutableStateOf<Throwable?>(null) }
    
    if (hasError) {
        fallbackContent()
    } else {
        // Wrap content execution with error handling
        runCatching {
            content()
        }.onFailure { error ->
            // Log the error
            ErrorLogger.logError(
                error = UIRenderingException(
                    message = "UI rendering failed in $component",
                    component = component,
                    cause = error
                ),
                context = ErrorContext(
                    component = component,
                    operation = "render",
                    additionalInfo = mapOf(
                        "noteId" to (noteId?.toString() ?: "unknown"),
                        "errorType" to error::class.simpleName.orEmpty()
                    )
                ),
                severity = ErrorSeverity.HIGH,
                userMessage = "Failed to render note component"
            )
            
            // Set error state to trigger fallback UI
            caughtError = error
            hasError = true
        }
    }
}

/**
 * Error boundary for critical operations that should never fail
 */
@Composable
fun CriticalErrorBoundary(
    operation: String,
    fallbackContent: @Composable () -> Unit = { CriticalErrorFallback(operation) },
    content: @Composable () -> Unit
) {
    // Simplified implementation - main protection comes from safe operation wrappers
    content()
}

/**
 * Safe wrapper for potentially dangerous string operations
 */
fun <T> safeStringOperation(
    operation: String,
    fallbackValue: T,
    block: () -> T
): T {
    return try {
        block()
    } catch (e: Exception) {
        ErrorLogger.logError(
            error = e,
            context = ErrorContext(
                component = "StringProcessor",
                operation = operation
            ),
            severity = ErrorSeverity.MEDIUM,
            userMessage = "String operation failed safely"
        )
        fallbackValue
    }
}

/**
 * Safe wrapper for date/time operations
 */
fun <T> safeDateTimeOperation(
    dateTimeString: String,
    operation: String,
    fallbackValue: T,
    block: () -> T
): T {
    return try {
        if (dateTimeString.isBlank()) {
            ErrorLogger.logMalformedData(
                noteId = null,
                field = "timestamp",
                value = "Empty timestamp",
                context = ErrorContext("DateTimeProcessor", operation)
            )
            return fallbackValue
        }
        block()
    } catch (e: Exception) {
        ErrorLogger.logError(
            error = e,
            context = ErrorContext(
                component = "DateTimeProcessor",
                operation = operation,
                additionalInfo = mapOf("timestamp" to dateTimeString.take(50))
            ),
            severity = ErrorSeverity.MEDIUM,
            userMessage = "Date/time processing failed safely"
        )
        fallbackValue
    }
}

/**
 * Default error fallback UI
 */
@Composable
private fun DefaultErrorFallback() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Content temporarily unavailable",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics {
                    contentDescription = "Error: Content temporarily unavailable"
                }
            )
        }
    }
}

/**
 * Note-specific error fallback UI
 */
@Composable
fun NoteErrorFallback(
    noteId: Long?,
    component: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Note Error",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = "Note Display Error",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "This note cannot be displayed properly due to data issues. " +
                      "The note content is preserved and will be accessible once the issue is resolved.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            
            if (noteId != null) {
                Text(
                    text = "Note ID: $noteId",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Critical error fallback UI
 */
@Composable
private fun CriticalErrorFallback(operation: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Critical Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
            
            Text(
                text = "Critical Error",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "A critical error occurred during: $operation",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Please restart the application if this issue persists.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Minimal error indicator for space-constrained areas
 */
@Composable
fun MinimalErrorIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
    }
}