package com.module.notelycompose.core

import kotlinx.coroutines.flow.Flow

actual class CommonFlow<T>(private val flow: Flow<T>) : Flow<T> by flow

actual fun <T> Flow<T>.toCommonFlow(): CommonFlow<T> = CommonFlow(this)

actual fun <T> CommonFlow<T>.asFlow(): Flow<T> = this