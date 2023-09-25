package com.example.yoomoneyintegration.domain.usecase

import com.example.yoomoneyintegration.domain.IPaymentRepository
import ru.yoomoney.sdk.kassa.payments.TokenizationResult
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount
import javax.inject.Inject

class GetPaymentUseCase @Inject constructor(
    private val paymentRepository: IPaymentRepository
) {
    operator fun invoke(id: String) =
        paymentRepository.getPaymentStatus(id)
}