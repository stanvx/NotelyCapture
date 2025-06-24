package com.module.notelycompose.platform

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences


fun dataStore(context: Context): DataStore<Preferences> {
    return createDataStore{
        context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
    }
}