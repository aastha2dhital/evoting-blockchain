package com.example.evotingmobileapp.auth

import androidx.lifecycle.ViewModel
import com.example.evotingmobileapp.BuildConfig
import com.example.evotingmobileapp.blockchain.DemoWallets
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
    private fun normalizedWalletAddress(): String = walletAddress.trim()

    fun hasWalletConnection(): Boolean {
        return isWalletConnected && normalizedWalletAddress().isNotBlank()
    }

    fun isAdminWallet(): Boolean {
        return hasWalletConnection() &&
                normalizedWalletAddress().equals(
                    BuildConfig.ADMIN_WALLET_ADDRESS,
                    ignoreCase = true
                )
    }

    fun isVoterWallet(): Boolean {
        return hasWalletConnection() && !isAdminWallet()
    }

    fun canAccessAdmin(): Boolean {
        return selectedRole == UserRole.ADMIN && isAdminWallet()
    }

    fun canAccessVoter(): Boolean {
        return selectedRole == UserRole.VOTER && isVoterWallet()
    }
}

class AuthSessionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun connectWallet(
        walletAddress: String,
        preferredRole: UserRole? = null
    ) {
        val normalizedAddress = walletAddress.trim()

        val resolvedRole = when (preferredRole) {
            UserRole.ADMIN -> {
                if (normalizedAddress.equals(BuildConfig.ADMIN_WALLET_ADDRESS, ignoreCase = true)) {
                    UserRole.ADMIN
                } else {
                    null
                }
            }

            UserRole.VOTER -> {
                if (normalizedAddress.isNotBlank() &&
                    !normalizedAddress.equals(BuildConfig.ADMIN_WALLET_ADDRESS, ignoreCase = true)
                ) {
                    UserRole.VOTER
                } else {
                    null
                }
            }

            null -> null
        }

        _uiState.update {
            it.copy(
                isWalletConnected = normalizedAddress.isNotBlank(),
                walletAddress = normalizedAddress,
                selectedRole = resolvedRole
            )
        }
    }

    fun signInAsAdmin() {
        connectWallet(
            walletAddress = BuildConfig.ADMIN_WALLET_ADDRESS,
            preferredRole = UserRole.ADMIN
        )
    }

    fun signInAsDemoVoter(walletAddress: String = DemoWallets.defaultVoterAddress) {
        connectWallet(
            walletAddress = walletAddress,
            preferredRole = UserRole.VOTER
        )
    }

    fun disconnectWallet() {
        _uiState.value = AuthUiState()
    }

    fun selectAdminRole() {
        _uiState.update { currentState ->
            if (currentState.isAdminWallet()) {
                currentState.copy(selectedRole = UserRole.ADMIN)
            } else {
                currentState.copy(selectedRole = null)
            }
        }
    }

    fun selectVoterRole() {
        _uiState.update { currentState ->
            if (currentState.isVoterWallet()) {
                currentState.copy(selectedRole = UserRole.VOTER)
            } else {
                currentState.copy(selectedRole = null)
            }
        }
    }

    fun clearSelectedRole() {
        _uiState.update { currentState ->
            currentState.copy(selectedRole = null)
        }
    }

    fun isCurrentWallet(address: String): Boolean {
        val normalizedAddress = address.trim()
        val currentState = _uiState.value

        return currentState.hasWalletConnection() &&
                currentState.walletAddress.equals(normalizedAddress, ignoreCase = true)
    }
}