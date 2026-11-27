package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.AppSortOrder
import com.example.data.preferences.AutoLockTimeout
import com.example.data.preferences.PinCrypto
import com.example.data.preferences.PreferencesRepository
import com.example.data.preferences.SecurityPreferencesState
import com.example.data.preferences.ThemeMode
import com.example.security.AppLockManager
import com.example.security.BiometricAvailability
import com.example.security.BiometricHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SecurityViewModel(
    private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val appLockManager: AppLockManager
) : ViewModel() {

    val preferences: StateFlow<SecurityPreferencesState> = preferencesRepository.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SecurityPreferencesState()
        )

    val isLocked: StateFlow<Boolean> = appLockManager.isLocked

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    private val _biometricAvailability = MutableStateFlow(BiometricHelper.checkAvailability(context))
    val biometricAvailability: StateFlow<BiometricAvailability> = _biometricAvailability.asStateFlow()

    fun refreshBiometricStatus() {
        _biometricAvailability.value = BiometricHelper.checkAvailability(context)
    }

    fun verifyAndUnlock(pin: String): Boolean {
        val state = preferences.value
        val valid = PinCrypto.verifyPin(pin, state.pinHash, state.pinSalt)
        if (valid) {
            _pinError.value = null
            appLockManager.unlock()
        } else {
            _pinError.value = "Incorrect PIN. Try again."
        }
        return valid
    }

    fun unlockWithBiometrics() {
        _pinError.value = null
        appLockManager.unlock()
    }

    fun clearPinError() {
        _pinError.value = null
    }

    fun lockNow() {
        appLockManager.lockNow()
    }

    fun enableOrUpdatePin(newPin: String) {
        if (newPin.length < 4) return
        viewModelScope.launch {
            preferencesRepository.enablePin(newPin)
            _pinError.value = null
        }
    }

    fun disablePin() {
        viewModelScope.launch {
            preferencesRepository.disablePin()
            appLockManager.unlock()
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBiometricEnabled(enabled)
        }
    }

    fun setAutoLockTimeout(timeout: AutoLockTimeout) {
        viewModelScope.launch {
            preferencesRepository.setAutoLockTimeout(timeout)
        }
    }

    fun setLockOnLaunch(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setLockOnLaunch(enabled)
        }
    }

    fun setHideInRecents(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHideInRecents(enabled)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setCompactDensity(compact: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setCompactDensity(compact)
        }
    }

    fun setDefaultSortOrder(order: AppSortOrder) {
        viewModelScope.launch {
            preferencesRepository.setDefaultSortOrder(order)
        }
    }

    fun setShowSystemAppsByDefault(show: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setShowSystemAppsByDefault(show)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(true)
        }
    }

    fun verifyPinOnly(pin: String): Boolean {
        val state = preferences.value
        return PinCrypto.verifyPin(pin, state.pinHash, state.pinSalt)
    }

    class Factory(
        private val context: Context,
        private val preferencesRepository: PreferencesRepository,
        private val appLockManager: AppLockManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SecurityViewModel(context.applicationContext, preferencesRepository, appLockManager) as T
        }
    }
}
