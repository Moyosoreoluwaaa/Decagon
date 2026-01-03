package com.decagon.ui.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.util.LoadingState
import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.model.StakingPosition
import com.decagon.domain.usecase.staking.ClaimRewardsUseCase
import com.decagon.domain.usecase.staking.ObserveStakingPositionsUseCase
import com.decagon.domain.usecase.staking.StakeTokensUseCase
import com.decagon.domain.usecase.staking.UnstakeTokensUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Staking Dashboard ViewModel (V1.1 Feature).
 * Manages staking positions, delegations, and reward claiming.
 */
class StakingViewModel(
    private val observeStakingPositionsUseCase: ObserveStakingPositionsUseCase,
    private val stakeTokensUseCase: StakeTokensUseCase,
    private val unstakeTokensUseCase: UnstakeTokensUseCase,
    private val claimRewardsUseCase: ClaimRewardsUseCase
) : ViewModel() {

    // ==================== UI State ====================

    /**
     * All active staking positions.
     */
    private val _stakingPositions =
        MutableStateFlow<LoadingState<List<StakingPosition>>>(LoadingState.Loading)
    val stakingPositions: StateFlow<LoadingState<List<StakingPosition>>> =
        _stakingPositions.asStateFlow()

    /**
     * Total staked value (sum of all positions).
     */
    val totalStaked: StateFlow<Double> = stakingPositions.map { state ->
        (state as? LoadingState.Success)?.data?.sumOf {
            it.amountStaked.toDoubleOrNull() ?: 0.0
        } ?: 0.0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    /**
     * Total rewards earned across all positions.
     */
    val totalRewards: StateFlow<Double> = stakingPositions.map { state ->
        (state as? LoadingState.Success)?.data?.sumOf {
            it.rewardsEarned.toDoubleOrNull() ?: 0.0
        } ?: 0.0
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    /**
     * Average APY across all positions.
     */
    val averageApy: StateFlow<Double> = stakingPositions.map { state ->
        val positions = (state as? LoadingState.Success)?.data ?: return@map 0.0
        if (positions.isEmpty()) return@map 0.0
        positions.map { it.apy }.average()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    /**
     * Staking flow UI state (bottom sheet).
     */
    private val _stakingFlowState = MutableStateFlow(StakingFlowState())
    val stakingFlowState: StateFlow<StakingFlowState> = _stakingFlowState.asStateFlow()

    // ==================== One-Time Events ====================

    private val _events = MutableSharedFlow<StakingEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<StakingEvent> = _events.asSharedFlow()

    init {
        observeStakingPositions()
    }

    // ==================== Observation ====================

    private fun observeStakingPositions() {
        viewModelScope.launch {
            observeStakingPositionsUseCase()
                .collect { state ->
                    _stakingPositions.value = state
                }
        }
    }

    // ==================== Staking Actions ====================

    /**
     * Open staking flow (bottom sheet).
     */
    fun onStartStaking() {
        _stakingFlowState.value = StakingFlowState(
            isBottomSheetVisible = true,
            step = StakingStep.VALIDATOR_SELECTION
        )
    }

    /**
     * Select validator.
     */
    fun onValidatorSelected(
        validatorAddress: String,
        validatorName: String,
        apy: Double
    ) {
        _stakingFlowState.value = _stakingFlowState.value.copy(
            selectedValidator = validatorAddress,
            selectedValidatorName = validatorName,
            selectedValidatorApy = apy,
            step = StakingStep.AMOUNT_INPUT
        )
    }

    /**
     * Update stake amount.
     */
    fun onAmountChanged(amount: String) {
        _stakingFlowState.value = _stakingFlowState.value.copy(
            amount = amount
        )
    }

    /**
     * Execute staking transaction.
     */
    fun onConfirmStake() {
        val state = _stakingFlowState.value
        val amount = state.amount.toDoubleOrNull()

        if (state.selectedValidator.isNullOrBlank() || amount == null || amount <= 0) {
            viewModelScope.launch {
                _events.emit(StakingEvent.Error("Invalid staking details"))
            }
            return
        }

        viewModelScope.launch {
            _stakingFlowState.value = _stakingFlowState.value.copy(isSubmitting = true)

            stakeTokensUseCase(
                validatorAddress = state.selectedValidator,
                validatorName = state.selectedValidatorName ?: "Unknown",
                amountSol = amount
            )
                .onSuccess { transaction ->
                    _events.emit(StakingEvent.StakeSuccess(transaction))
                    closeStakingFlow()
                }
                .onFailure { e ->
                    _events.emit(StakingEvent.Error(e.message ?: "Staking failed"))
                }

            _stakingFlowState.value = _stakingFlowState.value.copy(isSubmitting = false)
        }
    }

    /**
     * Unstake tokens from a position.
     */
    fun onUnstake(positionId: String) {
        viewModelScope.launch {
            unstakeTokensUseCase(positionId)
                .onSuccess { transaction ->
                    _events.emit(StakingEvent.UnstakeSuccess(transaction!!))
                }
                .onFailure { e ->
                    _events.emit(StakingEvent.Error(e.message ?: "Unstake failed"))
                }
        }
    }

    /**
     * Claim rewards from a position.
     */
    fun onClaimRewards(positionId: String) {
        viewModelScope.launch {
            claimRewardsUseCase(positionId)
                .onSuccess { transaction ->
                    _events.emit(StakingEvent.ClaimSuccess(transaction!!))
                }
                .onFailure { e ->
                    _events.emit(StakingEvent.Error(e.message ?: "Claim failed"))
                }
        }
    }

    /**
     * Close staking flow.
     */
    fun closeStakingFlow() {
        _stakingFlowState.value = StakingFlowState()
    }

    // ==================== Formatting ====================

    fun formatApy(apy: Double): String {
        return "${"%.2f".format(apy)}%"
    }

    fun getApyColor(apy: Double): Color {
        return when {
            apy >= 10.0 -> Color(0xFF4ECDC4) // High APY - Teal
            apy >= 5.0 -> Color(0xFFF7DC6F) // Medium APY - Yellow
            else -> Color(0xFF8E8E93) // Low APY - Gray
        }
    }
}

/**
 * Staking flow UI state (bottom sheet).
 */
data class StakingFlowState(
    val isBottomSheetVisible: Boolean = false,
    val step: StakingStep = StakingStep.VALIDATOR_SELECTION,
    val selectedValidator: String? = null,
    val selectedValidatorName: String? = null,
    val selectedValidatorApy: Double = 0.0,
    val amount: String = "",
    val isSubmitting: Boolean = false
)

enum class StakingStep {
    VALIDATOR_SELECTION,
    AMOUNT_INPUT,
    CONFIRMATION
}

/**
 * Staking events.
 */
sealed interface StakingEvent {
    data class StakeSuccess(val transaction: DecagonTransaction?) : StakingEvent
    data class UnstakeSuccess(val transaction: DecagonTransaction) : StakingEvent
    data class ClaimSuccess(val transaction: DecagonTransaction) : StakingEvent
    data class Error(val message: String) : StakingEvent
}