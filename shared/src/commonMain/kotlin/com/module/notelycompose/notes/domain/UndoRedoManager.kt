package com.module.notelycompose.notes.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages undo/redo functionality for rich text editing operations using the Command pattern.
 * 
 * Features:
 * - Thread-safe command execution and history management
 * - Intelligent command merging for performance optimization
 * - Configurable history size limits
 * - Real-time state updates for UI integration
 * - Memory-efficient history pruning
 * - Batch operation support for complex edits
 * 
 * @param maxHistorySize Maximum number of commands to keep in history (default: 100)
 * @param autoMergeEnabled Whether to automatically merge compatible commands (default: true)
 */
class UndoRedoManager(
    private val maxHistorySize: Int = 100,
    private val autoMergeEnabled: Boolean = true
) {
    
    private val undoStack = ArrayDeque<TextEditCommand>()
    private val redoStack = ArrayDeque<TextEditCommand>()
    
    private val mutex = Mutex()
    
    // State flows for UI integration
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()
    
    private val _undoDescription = MutableStateFlow<String?>(null)
    val undoDescription: StateFlow<String?> = _undoDescription.asStateFlow()
    
    private val _redoDescription = MutableStateFlow<String?>(null)
    val redoDescription: StateFlow<String?> = _redoDescription.asStateFlow()
    
    private val _historySize = MutableStateFlow(0)
    val historySize: StateFlow<Int> = _historySize.asStateFlow()
    
    /**
     * Executes a command and adds it to the undo history.
     * Automatically attempts to merge with the previous command if enabled and possible.
     * 
     * @param command The command to execute
     * @throws Exception if command execution fails
     */
    suspend fun executeCommand(command: TextEditCommand) {
        mutex.withLock {
            // Try to merge with the last command if auto-merge is enabled
            if (autoMergeEnabled && undoStack.isNotEmpty()) {
                val lastCommand = undoStack.last()
                if (lastCommand.canMergeWith(command)) {
                    val mergedCommand = lastCommand.mergeWith(command)
                    if (mergedCommand != null) {
                        // Remove the last command and execute the merged one
                        undoStack.removeLast()
                        command.execute()
                        undoStack.addLast(mergedCommand)
                        clearRedoHistory()
                        updateStateFlows()
                        return
                    }
                }
            }
            
            // Execute the command
            command.execute()
            
            // Add to undo stack
            undoStack.addLast(command)
            
            // Clear redo stack since we have a new command
            clearRedoHistory()
            
            // Maintain history size limit
            pruneHistoryIfNeeded()
            
            // Update state flows
            updateStateFlows()
        }
    }
    
    /**
     * Executes multiple commands as a batch operation.
     * This creates a single composite command that can be undone/redone as one unit.
     * 
     * @param commands List of commands to execute as a batch
     */
    suspend fun executeBatchCommands(commands: List<TextEditCommand>) {
        if (commands.isEmpty()) return
        
        if (commands.size == 1) {
            executeCommand(commands.first())
            return
        }
        
        val compositeCommand = CompositeCommand(commands)
        executeCommand(compositeCommand)
    }
    
    /**
     * Undoes the last command in the history.
     * 
     * @return true if an operation was undone, false if no operations to undo
     */
    suspend fun undo(): Boolean {
        return mutex.withLock {
            val command = undoStack.removeLastOrNull() ?: return false
            
            try {
                command.undo()
                redoStack.addLast(command)
                updateStateFlows()
                true
            } catch (e: Exception) {
                // If undo fails, restore the command to the undo stack
                undoStack.addLast(command)
                updateStateFlows()
                throw e
            }
        }
    }
    
    /**
     * Redoes the last undone command.
     * 
     * @return true if an operation was redone, false if no operations to redo
     */
    suspend fun redo(): Boolean {
        return mutex.withLock {
            val command = redoStack.removeLastOrNull() ?: return false
            
            try {
                command.execute()
                undoStack.addLast(command)
                updateStateFlows()
                true
            } catch (e: Exception) {
                // If redo fails, restore the command to the redo stack
                redoStack.addLast(command)
                updateStateFlows()
                throw e
            }
        }
    }
    
    /**
     * Clears all undo and redo history.
     * This is useful when starting a new document or after a save operation.
     */
    suspend fun clearHistory() {
        mutex.withLock {
            undoStack.clear()
            redoStack.clear()
            updateStateFlows()
        }
    }
    
    /**
     * Gets the complete undo history for debugging or UI display.
     * Returns a list of command descriptions in chronological order.
     */
    suspend fun getUndoHistory(): List<String> {
        return mutex.withLock {
            undoStack.map { it.getDescription() }
        }
    }
    
    /**
     * Gets the complete redo history for debugging or UI display.
     * Returns a list of command descriptions in chronological order.
     */
    suspend fun getRedoHistory(): List<String> {
        return mutex.withLock {
            redoStack.map { it.getDescription() }
        }
    }
    
    /**
     * Checks if there are any unsaved changes in the history.
     * This can be used to prompt users before closing a document.
     * 
     * @param lastSavedHistorySize The history size at the time of last save
     * @return true if there are unsaved changes
     */
    suspend fun hasUnsavedChanges(lastSavedHistorySize: Int): Boolean {
        return mutex.withLock {
            undoStack.size != lastSavedHistorySize
        }
    }
    
    /**
     * Creates a checkpoint in the history that can be used to track save states.
     * Returns the current history size which can be used with hasUnsavedChanges.
     */
    suspend fun createCheckpoint(): Int {
        return mutex.withLock {
            undoStack.size
        }
    }
    
    /**
     * Gets detailed statistics about the command history for debugging and analytics.
     */
    suspend fun getHistoryStatistics(): HistoryStatistics {
        return mutex.withLock {
            val commandTypes = undoStack.groupBy { it::class.simpleName }
                .mapValues { it.value.size }
            
            val totalMemoryEstimate = undoStack.sumOf { estimateCommandMemoryUsage(it) }
            
            HistoryStatistics(
                undoStackSize = undoStack.size,
                redoStackSize = redoStack.size,
                maxHistorySize = maxHistorySize,
                commandTypeDistribution = commandTypes,
                estimatedMemoryUsage = totalMemoryEstimate,
                autoMergeEnabled = autoMergeEnabled
            )
        }
    }
    
    private fun clearRedoHistory() {
        redoStack.clear()
    }
    
    private fun pruneHistoryIfNeeded() {
        while (undoStack.size > maxHistorySize) {
            undoStack.removeFirst()
        }
    }
    
    private fun updateStateFlows() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
        _undoDescription.value = undoStack.lastOrNull()?.getDescription()
        _redoDescription.value = redoStack.lastOrNull()?.getDescription()
        _historySize.value = undoStack.size
    }
    
    private fun estimateCommandMemoryUsage(command: TextEditCommand): Long {
        // Rough estimation of memory usage for analytics
        return when (command) {
            is InsertTextCommand -> 64L + command.toString().length * 2 // Rough string size
            is DeleteTextCommand -> 64L + command.toString().length * 2
            is FormatCommand -> 128L // Formatting metadata
            is ListCommand -> 96L // List metadata
            is CompositeCommand -> 128L + command.toString().length * 2
            else -> 64L // Base object overhead
        }
    }
}

/**
 * Statistics about the command history for debugging and analytics.
 */
data class HistoryStatistics(
    val undoStackSize: Int,
    val redoStackSize: Int,
    val maxHistorySize: Int,
    val commandTypeDistribution: Map<String?, Int>,
    val estimatedMemoryUsage: Long,
    val autoMergeEnabled: Boolean
)

/**
 * Builder class for creating UndoRedoManager with custom configuration.
 */
class UndoRedoManagerBuilder {
    private var maxHistorySize: Int = 100
    private var autoMergeEnabled: Boolean = true
    
    fun maxHistorySize(size: Int): UndoRedoManagerBuilder {
        require(size > 0) { "History size must be positive" }
        this.maxHistorySize = size
        return this
    }
    
    fun autoMergeEnabled(enabled: Boolean): UndoRedoManagerBuilder {
        this.autoMergeEnabled = enabled
        return this
    }
    
    fun build(): UndoRedoManager {
        return UndoRedoManager(
            maxHistorySize = maxHistorySize,
            autoMergeEnabled = autoMergeEnabled
        )
    }
}

/**
 * Factory function for creating UndoRedoManager with default settings.
 */
fun createUndoRedoManager(): UndoRedoManager = UndoRedoManager()

/**
 * Factory function for creating UndoRedoManager with custom settings.
 */
fun createUndoRedoManager(
    configure: UndoRedoManagerBuilder.() -> Unit
): UndoRedoManager {
    return UndoRedoManagerBuilder().apply(configure).build()
}

/**
 * Extension functions for convenient command creation and execution.
 */
suspend fun UndoRedoManager.executeFormatCommand(
    command: FormatCommand
) = executeCommand(command)

suspend fun UndoRedoManager.executeInsertCommand(
    command: InsertTextCommand
) = executeCommand(command)

suspend fun UndoRedoManager.executeDeleteCommand(
    command: DeleteTextCommand
) = executeCommand(command)

suspend fun UndoRedoManager.executeListCommand(
    command: ListCommand
) = executeCommand(command)

/**
 * Utility for tracking undo/redo performance metrics.
 */
class UndoRedoMetrics {
    private var undoCount = 0
    private var redoCount = 0
    private var commandCount = 0
    private var mergeCount = 0
    
    fun recordUndo() { undoCount++ }
    fun recordRedo() { redoCount++ }
    fun recordCommand() { commandCount++ }
    fun recordMerge() { mergeCount++ }
    
    fun getMetrics(): Map<String, Int> = mapOf(
        "undoCount" to undoCount,
        "redoCount" to redoCount,
        "commandCount" to commandCount,
        "mergeCount" to mergeCount
    )
    
    fun reset() {
        undoCount = 0
        redoCount = 0
        commandCount = 0
        mergeCount = 0
    }
}