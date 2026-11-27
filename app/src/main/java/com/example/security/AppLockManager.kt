package com.example.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.data.preferences.AutoLockTimeout
import com.example.data.preferences.PreferencesRepository
import com.example.data.preferences.SecurityPreferencesState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppLockManager(
    private val preferencesRepository: PreferencesRepository
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var latestPrefs: SecurityPreferencesState = SecurityPreferencesState()
    private var initialCheckDone = false
    private var backgroundTimestampMs: Long? = null

    init {
        scope.launch {
            preferencesRepository.preferences.collect { prefs ->
                latestPrefs = prefs
                if (!initialCheckDone) {
                    initialCheckDone = true
                    if (prefs.isPinEnabled && prefs.lockOnLaunch) {
                        _isLocked.value = true
                    }
                } else if (!prefs.isPinEnabled) {
                    _isLocked.value = false
                }
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        if (latestPrefs.isPinEnabled) {
            backgroundTimestampMs = System.currentTimeMillis()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        val bgAt = backgroundTimestampMs ?: return
        backgroundTimestampMs = null

        if (!latestPrefs.isPinEnabled) return
        val timeout = latestPrefs.autoLockTimeout
        if (timeout == AutoLockTimeout.MANUAL) return

        val elapsedMs = System.currentTimeMillis() - bgAt
        if (elapsedMs >= timeout.seconds * 1000L) {
            _isLocked.value = true
        }
    }

    fun lockNow() {
        if (latestPrefs.isPinEnabled) {
            _isLocked.value = true
        }
    }

    fun unlock() {
        _isLocked.value = false
        backgroundTimestampMs = null
    }
}
