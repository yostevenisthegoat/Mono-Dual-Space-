package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.monoDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mono_dual_space_prefs"
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val AUTO_LOCK_SECONDS = intPreferencesKey("auto_lock_seconds")
        val LOCK_ON_LAUNCH = booleanPreferencesKey("lock_on_launch")
        val HIDE_IN_RECENTS = booleanPreferencesKey("hide_in_recents")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COMPACT_DENSITY = booleanPreferencesKey("compact_density")
        val DEFAULT_SORT_ORDER = stringPreferencesKey("default_sort_order")
        val SHOW_SYSTEM_APPS = booleanPreferencesKey("show_system_apps")
    }

    val preferences: Flow<SecurityPreferencesState> = context.monoDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val timeoutSeconds = prefs[Keys.AUTO_LOCK_SECONDS] ?: AutoLockTimeout.IMMEDIATE.seconds
            val timeout = AutoLockTimeout.entries.firstOrNull { it.seconds == timeoutSeconds }
                ?: AutoLockTimeout.IMMEDIATE
            val theme = runCatching {
                ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name)
            }.getOrDefault(ThemeMode.DARK)
            val sortOrder = runCatching {
                AppSortOrder.valueOf(prefs[Keys.DEFAULT_SORT_ORDER] ?: AppSortOrder.NAME_ASC.name)
            }.getOrDefault(AppSortOrder.NAME_ASC)

            SecurityPreferencesState(
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                isPinEnabled = prefs[Keys.PIN_ENABLED] ?: false,
                pinHash = prefs[Keys.PIN_HASH] ?: "",
                pinSalt = prefs[Keys.PIN_SALT] ?: "",
                isBiometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
                autoLockTimeout = timeout,
                lockOnLaunch = prefs[Keys.LOCK_ON_LAUNCH] ?: true,
                hideInRecents = prefs[Keys.HIDE_IN_RECENTS] ?: false,
                themeMode = theme,
                compactDensity = prefs[Keys.COMPACT_DENSITY] ?: false,
                defaultSortOrder = sortOrder,
                showSystemAppsByDefault = prefs[Keys.SHOW_SYSTEM_APPS] ?: false
            )
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.monoDataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun enablePin(pin: String) {
        val hashed = PinCrypto.createHash(pin)
        context.monoDataStore.edit { prefs ->
            prefs[Keys.PIN_ENABLED] = true
            prefs[Keys.PIN_HASH] = hashed.hashBase64
            prefs[Keys.PIN_SALT] = hashed.saltBase64
        }
    }

    suspend fun disablePin() {
        context.monoDataStore.edit { prefs ->
            prefs[Keys.PIN_ENABLED] = false
            prefs[Keys.PIN_HASH] = ""
            prefs[Keys.PIN_SALT] = ""
            prefs[Keys.BIOMETRIC_ENABLED] = false
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.monoDataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setAutoLockTimeout(timeout: AutoLockTimeout) {
        context.monoDataStore.edit { it[Keys.AUTO_LOCK_SECONDS] = timeout.seconds }
    }

    suspend fun setLockOnLaunch(enabled: Boolean) {
        context.monoDataStore.edit { it[Keys.LOCK_ON_LAUNCH] = enabled }
    }

    suspend fun setHideInRecents(enabled: Boolean) {
        context.monoDataStore.edit { it[Keys.HIDE_IN_RECENTS] = enabled }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.monoDataStore.edit { it[Keys.THEME_MODE] = themeMode.name }
    }

    suspend fun setCompactDensity(compact: Boolean) {
        context.monoDataStore.edit { it[Keys.COMPACT_DENSITY] = compact }
    }

    suspend fun setDefaultSortOrder(order: AppSortOrder) {
        context.monoDataStore.edit { it[Keys.DEFAULT_SORT_ORDER] = order.name }
    }

    suspend fun setShowSystemAppsByDefault(show: Boolean) {
        context.monoDataStore.edit { it[Keys.SHOW_SYSTEM_APPS] = show }
    }
}
