package com.example.yoomoneyintegration.presentation.confirmation

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.yoomoneyintegration.R
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConfirmationActivity : AppCompatActivity(R.layout.activity_confirmation) {

    private val viewModel: ConfirmationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra("ID")

        val text = findViewById<TextView>(R.id.url)
        val progress = findViewById<CircularProgressIndicator>(R.id.progress)

        viewModel.state.observe(this) {
            if (it.isError) {
                text.text = it.errorMessage
                progress.visibility = View.GONE
            }
            if (it.isLoading) {
                progress.visibility = View.VISIBLE
            }
            if (it.isSuccess) {
                text.text = if (it.payment?.paid == true) "Success paid" else "Payment canceled. Check your card"
                progress.visibility = View.GONE
            }
        }

        id?.let(viewModel::checkPayment)
    }
}