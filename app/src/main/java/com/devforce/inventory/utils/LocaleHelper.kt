package com.devforce.inventory.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleSwitcher {
    fun updateBaseContextLocale(context: Context, lng: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(lng)
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(
                    lng
                )
            )
        }
    }

    fun getCurrentLanguage(context: Context): String {
        val config = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            config.locales[0].language
        } else {
            @Suppress("DEPRECATION")
            config.locale.language
        }
    }
}