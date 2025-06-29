package com.module.notelycompose.core

import com.module.notelycompose.platform.isDebugMode

inline fun debugPrintln(message: () -> Any?) {
    // Turn flag to true to test on iOS before proper implementation
    if (isDebugMode()) {
        println(message())
    }
}
