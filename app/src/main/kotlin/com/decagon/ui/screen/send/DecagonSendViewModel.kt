package com.decagon.ui.screen.send

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.usecase.DecagonSendTokenUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DecagonSendViewModel(
    private val sendTokenUseCase: DecagonSendTokenUseCase
) : ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private var currentActivity: FragmentActivity? = null

    private val _sendState = MutableStateFlow<DecagonLoadingState<DecagonTransaction>>(
        DecagonLoadingState.Idle
    )
    val sendState: StateFlow<DecagonLoadingState<DecagonTransaction>> = _sendState.asStateFlow()

    init {
        Timber.d("DecagonSendViewModel initialized")
    }

    fun setActivity(activity: FragmentActivity?) {
        currentActivity = activity
        Timber.d("Activity set: ${activity != null}")
    }

    fun sendToken(toAddress: String, amount: Double) {
        val activity = currentActivity ?: run {
            Timber.e("Cannot send: Activity is null")
            _sendState.value = DecagonLoadingState.Error(
                IllegalStateException("Activity required"),
                "Cannot access biometric authentication"
            )
            return
        }

        Timber.i("Initiating send: $amount SOL to ${toAddress.take(4)}...")
        viewModelScope.launch {
            _sendState.value = DecagonLoadingState.Loading

            val result = sendTokenUseCase(toAddress, amount, activity)

            _sendState.value = result.fold(
                onSuccess = { tx ->
                    Timber.i("Send successful: ${tx.signature}")
                    DecagonLoadingState.Success(tx)
                },
                onFailure = { error ->
                    Timber.e(error, "Send failed")
                    DecagonLoadingState.Error(error, error.message ?: "Send failed")
                }
            )
        }
    }

    fun resetState() {
        Timber.d("Resetting send state")
        _sendState.value = DecagonLoadingState.Idle
    }
}