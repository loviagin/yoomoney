package com.example.yoomoneyintegration.data

import com.example.yoomoneyintegration.data.dto.CreatePayment
import com.example.yoomoneyintegration.data.dto.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentApi {

    @POST(".")
    suspend fun createPayment(@Body createPayment: CreatePayment): Response

    @GET("{id}")
    suspend fun getPayment(@Path("id") id: String): Response
}