package com.octane.wallet.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Approval
import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.usecases.security.ObserveApprovalsUseCase
import com.octane.wallet.domain.usecases.security.RevokeApprovalUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Security Dashboard ViewModel (V1.8 Feature).
 * Manages token approvals and security health score.
 */
class SecurityViewModel(
    private val observeApprovalsUseCase: ObserveApprovalsUseCase,
    private val revokeApprovalUseCase: RevokeApprovalUseCase
) : ViewModel() {

    // ==================== UI State ====================

    /**
     * Active token approvals.
     */
    private val _approvals = MutableStateFlow<LoadingState<List<Approval>>>(LoadingState.Loading)
    val approvals: StateFlow<LoadingState<List<Approval>>> = _approvals.asStateFlow()

    /**
     * Security health score (based on approval count and risk level).
     * 0-3: Good (Green)
     * 4-7: Warning (Yellow)
     * 8+: Critical (Red)
     */
    val securityScore: StateFlow<SecurityScore> = approvals.map { state ->
        val approvalCount = (state as? LoadingState.Success)?.data?.size ?: 0

        when {
            approvalCount == 0 -> SecurityScore.Perfect
            approvalCount <= 3 -> SecurityScore.Good(approvalCount)
            approvalCount <= 7 -> SecurityScore.Warning(approvalCount)
            else -> SecurityScore.Critical(approvalCount)
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, SecurityScore.Perfect)

    /**
     * Revocation flow state.
     */
    private val _revocationState = MutableStateFlow(RevocationState())
    val revocationState: StateFlow<RevocationState> = _revocationState.asStateFlow()

    // ==================== One-Time Events ====================

    private val _events = MutableSharedFlow<SecurityEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<SecurityEvent> = _events.asSharedFlow()

    init {
        observeApprovals()
    }

    // ==================== Observation ====================

    private fun observeApprovals() {
        viewModelScope.launch {
            observeApprovalsUseCase()
                .collect { state ->
                    _approvals.value = state
                }
        }
    }

    // ==================== Revocation Actions ====================

    /**
     * Initiate revocation flow (show confirmation dialog).
     */
    fun onRevokeClick(approval: Approval) {
        _revocationState.value = RevocationState(
            showConfirmation = true,
            approvalToRevoke = approval
        )
    }

    /**
     * Confirm revocation (execute transaction).
     */
    fun onConfirmRevoke() {
        val approval = _revocationState.value.approvalToRevoke

        if (approval == null) {
            viewModelScope.launch {
                _events.emit(SecurityEvent.Error("No approval selected"))
            }
            return
        }

        viewModelScope.launch {
            _revocationState.value = _revocationState.value.copy(isRevoking = true)

            revokeApprovalUseCase(approval.id)
                .onSuccess { transaction ->
                    _events.emit(SecurityEvent.RevocationSuccess(transaction))
                    _revocationState.value = RevocationState()
                }
                .onFailure { e ->
                    _events.emit(SecurityEvent.Error(e.message ?: "Revocation failed"))
                    _revocationState.value = _revocationState.value.copy(isRevoking = false)
                }
        }
    }

    /**
     * Cancel revocation.
     */
    fun onCancelRevoke() {
        _revocationState.value = RevocationState()
    }

    // ==================== Formatting ====================

    fun formatAllowance(allowance: String): String {
        return if (allowance == "unlimited") {
            "UNLIMITED"
        } else {
            allowance
        }
    }

    fun getScoreColor(score: SecurityScore): Color {
        return when (score) {
            SecurityScore.Perfect -> Color(0xFF4ECDC4) // Teal
            is SecurityScore.Good -> Color(0xFF4ECDC4) // Teal
            is SecurityScore.Warning -> Color(0xFFF7DC6F) // Yellow
            is SecurityScore.Critical -> Color(0xFFFF6B6B) // Red
        }
    }

    fun getScoreLabel(score: SecurityScore): String {
        return when (score) {
            SecurityScore.Perfect -> "Perfect"
            is SecurityScore.Good -> "Good"
            is SecurityScore.Warning -> "Warning"
            is SecurityScore.Critical -> "Critical"
        }
    }
}

/**
 * Security health score.
 */
sealed interface SecurityScore {
    data object Perfect : SecurityScore
    data class Good(val activeApprovals: Int) : SecurityScore
    data class Warning(val activeApprovals: Int) : SecurityScore
    data class Critical(val activeApprovals: Int) : SecurityScore
}

/**
 * Revocation flow state.
 */
data class RevocationState(
    val showConfirmation: Boolean = false,
    val approvalToRevoke: Approval? = null,
    val isRevoking: Boolean = false
)

/**
 * Security events.
 */
sealed interface SecurityEvent {
    data class RevocationSuccess(val transaction: Transaction) : SecurityEvent
    data class Error(val message: String) : SecurityEvent
}