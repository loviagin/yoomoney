package com.example.yoomoneyintegration.data

import com.example.yoomoneyintegration.data.dto.CreatePayment
import com.example.yoomoneyintegration.data.dto.Response
import com.example.yoomoneyintegration.domain.IPaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.yoomoney.sdk.kassa.payments.TokenizationResult
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount

class RemotePaymentRepository(private val paymentApi: PaymentApi) : IPaymentRepository {

    override fun createPayment(
        amount: Amount,
        tokenizationResult: TokenizationResult
    ): Flow<Response> {
        return flow {
            val response = paymentApi.createPayment(
                createPayment = CreatePayment(
                    amount = amount.value,
                    currency = amount.currency.currencyCode,
                    method = tokenizationResult.paymentMethodType.name.lowercase(),
                    token = tokenizationResult.paymentToken
                )
            )

            if (!response.success) {
                throw NetworkException(response.message)
            }
            emit(response)
        }
    }

    override fun getPaymentStatus(id: String): Flow<Response> {
        return flow {

            val response = paymentApi.getPayment(id)

            if (!response.success) {
                throw NetworkException(response.message)
            }
            emit(response)
        }
    }
}