package com.example.yoomoneyintegration.presentation.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import com.example.yoomoneyintegration.presentation.confirmation.ConfirmationActivity
import com.example.yoomoneyintegration.presentation.SecureWebViewActivity
import com.example.yoomoneyintegration.R
import com.example.yoomoneyintegration.databinding.ActivityMainBinding
import com.example.yoomoneyintegration.data.dto.Payment
import dagger.hilt.android.AndroidEntryPoint
import ru.yoomoney.sdk.kassa.payments.Checkout.createConfirmationIntent
import ru.yoomoney.sdk.kassa.payments.Checkout.createTokenizationResult
import ru.yoomoney.sdk.kassa.payments.Checkout.createTokenizeIntent
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.*
import java.math.BigDecimal
import java.util.Currency

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private val tokenizeResult = registerForActivityResult(StartActivityForResult()) { result ->
        handleTokenizationResult(result)
    }

    private val confirmationResult = registerForActivityResult(StartActivityForResult()) { result ->
        handleConfirmationResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.send.setOnClickListener {

            binding.send.isEnabled = false

            startTokenize(
                title = "Подписка",
                description = "Вы получите эксклюзивный доступ к платному контенту автора",
                amount = BigDecimal.valueOf(10)
            )
        }

        viewModel.events.observe(this) {
            when (it) {
                is Event.OpenConfirmation -> {
                    openConfirmation(it.payment)
                    binding.view.visibility = View.VISIBLE
                    binding.loading.visibility = View.GONE
                }

                is Event.ShowView -> {
                    binding.view.visibility = View.VISIBLE
                    binding.loading.visibility = View.GONE
                }

                is Event.ShowMessage -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleTokenizationResult(result: ActivityResult) {

        binding.send.isEnabled = true

        when (result.resultCode) {
            RESULT_OK -> {
                result.data?.let {
                    val tokenizationResult = createTokenizationResult(it)
                    viewModel.createPayment(tokenizationResult)

                    binding.view.visibility = View.GONE
                    binding.loading.visibility = View.VISIBLE
                }
            }

            RESULT_CANCELED -> {
                Toast.makeText(this, "Tokenization Canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleConfirmationResult(result: ActivityResult) {
        when (result.resultCode) {
            RESULT_OK -> {
                Log.d("TAG", "openConfirmation: handle confirmation result")

                val intent = Intent(this, ConfirmationActivity::class.java).apply {
                    putExtra("ID", viewModel.paymentId)
                }
                startActivity(intent)

                Toast.makeText(this, "Confirmation OK: ${result.data}", Toast.LENGTH_SHORT).show()
            }

            RESULT_CANCELED -> {
                Toast.makeText(this, "Tokenization Canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openConfirmation(payment: Payment) {

        if (payment.confirmationUrl.isNullOrBlank()) {
            Log.d("TAG", "openConfirmation: confirmationUrl is null")
            val intent = Intent(this, ConfirmationActivity::class.java).apply {
                putExtra("ID", payment.id)
            }
            startActivity(intent)
        } else if (payment.confirmationType == "redirect") {
            val intent = Intent(this, SecureWebViewActivity::class.java).apply {
                putExtra("URL", payment.confirmationUrl)
                putExtra("ID", payment.id)
            }
            startActivity(intent)
        } else {
            val intent = createConfirmationIntent(
                context = this@MainActivity,
                confirmationUrl = payment.confirmationUrl,
                paymentMethodType = payment.methodType,
                clientApplicationKey = getString(R.string.client_key),
            )
            confirmationResult.launch(intent)
        }
    }

    private fun startTokenize(title: String, description: String, amount: BigDecimal) {

//        val testParameters = TestParameters(
//            showLogs = true,
//            googlePayTestEnvironment = true,
//            mockConfiguration = MockConfiguration(
//                completeWithError = false,
//                paymentAuthPassed = true,
//                linkedCardsCount = 2,
//                serviceFee = Amount(BigDecimal.ONE, Currency.getInstance("RUB"))
//            )
//        )

        val paymentParameters = PaymentParameters(
            amount = Amount(amount, Currency.getInstance("RUB")),
            title = title,
            subtitle = description,
            clientApplicationKey = getString(R.string.client_key),
            shopId = getString(R.string.shopId),
            savePaymentMethod = SavePaymentMethod.OFF,
            customReturnUrl = getString(R.string.redirectUrl),
            paymentMethodTypes = setOf(
                PaymentMethodType.YOO_MONEY,
                PaymentMethodType.BANK_CARD,
                PaymentMethodType.SBERBANK,
                PaymentMethodType.SBP
            ),
            userPhoneNumber = "",
            authCenterClientId = getString(R.string.client_key)
        )

        viewModel.paymentParameters = paymentParameters

        val intent = createTokenizeIntent(this, paymentParameters/*, testParameters*/)
        tokenizeResult.launch(intent)
    }
}