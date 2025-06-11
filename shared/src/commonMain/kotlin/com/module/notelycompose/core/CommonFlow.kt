package com.module.notelycompose.core

import kotlinx.coroutines.flow.Flow

expect class CommonFlow<T>

expect fun <T> Flow<T>.toCommonFlow(): CommonFlow<T>

expect fun <T> CommonFlow<T>.asFlow(): Flow<T>