package com.example.evotingmobileapp.auth

import androidx.lifecycle.ViewModel
import com.example.evotingmobileapp.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class UserRole {
    ADMIN,
    VOTER
}

data class AuthUiState(
    val isWalletConnected: Boolean = false,
    val walletAddress: String = "",
    val selectedRole: UserRole? = null
) {
    fun isAdminWallet(): Boolean {
        return isWalletConnected &&
                walletAddress.equals(BuildConfig.ADMIN_WALLET_ADDRESS, ignoreCase = true)
    }

    fun canAccessAdmin(): Boolean {
        return isAdminWallet()
    }

    fun canAccessVoter(): Boolean {
        return isWalletConnected && !isAdminWallet()
    }
}

class AuthSessionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun connectWallet(walletAddress: String) {
        val normalizedAddress = walletAddress.trim()

        _uiState.update {
            it.copy(
                isWalletConnected = normalizedAddress.isNotBlank(),
                walletAddress = normalizedAddress,
                selectedRole = null
            )
        }
    }

    fun disconnectWallet() {
        _uiState.value = AuthUiState()
    }

    fun selectAdminRole() {
        _uiState.update { currentState ->
            if (currentState.canAccessAdmin()) {
                currentState.copy(selectedRole = UserRole.ADMIN)
            } else {
                currentState
            }
        }
    }

    fun selectVoterRole() {
        _uiState.update { currentState ->
            if (currentState.canAccessVoter()) {
                currentState.copy(selectedRole = UserRole.VOTER)
            } else {
                currentState
            }
        }
    }

    fun clearSelectedRole() {
        _uiState.update { currentState ->
            currentState.copy(selectedRole = null)
        }
    }
}