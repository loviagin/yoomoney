package com.example.yoomoneyintegration.presentation.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yoomoneyintegration.data.dto.Payment
import com.example.yoomoneyintegration.data.dto.Response
import com.example.yoomoneyintegration.domain.usecase.CreatePaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import ru.yoomoney.sdk.kassa.payments.TokenizationResult
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters
import javax.inject.Inject

sealed interface Event {
    data class OpenConfirmation(
        val payment: Payment
    ) : Event

    data class ShowMessage(
        val message: String
    ) : Event

    object ShowView : Event
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val createPaymentUseCase: CreatePaymentUseCase
) : ViewModel() {

    var paymentId: String? = null

    lateinit var paymentParameters: PaymentParameters

    val events = MutableLiveData<Event>()

    fun createPayment(tokenizationResult: TokenizationResult) {
        viewModelScope.launch {
            createPaymentUseCase(paymentParameters.amount, tokenizationResult)
                .catch {
                    Log.e("TAG", "createPayment: $it")
                    events.postValue(Event.ShowView)
                }
                .collect { response: Response ->
                    val payment = response.payment
                    paymentId = payment.id
                    events.postValue(Event.OpenConfirmation(payment))
                }
        }
    }
}