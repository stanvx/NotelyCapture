package com.module.notelycompose.core

import kotlinx.coroutines.flow.Flow


 class CommonFlow<T>(private val flow: Flow<T>) : Flow<T> by flow

 fun <T> Flow<T>.toCommonFlow(): CommonFlow<T> = CommonFlow(this)

fun <T> CommonFlow<T>.asFlow(): Flow<T> = this