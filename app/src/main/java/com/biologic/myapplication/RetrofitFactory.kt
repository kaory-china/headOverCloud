package com.biologic.myapplication

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitFactory(context: Context?) {

    val URL: String = context!!.getResources().getString(R.string.ip_address)
    val user: String = context!!.getResources().getString(R.string.user)
    val password: String = context!!.getResources().getString(R.string.password)

    val client = OkHttpClient.Builder()
        .addInterceptor(PulpAuth(user, password))
        .build()

    val retrofitFactory = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun retrofitService(): RetrofitService {
        return retrofitFactory.create(RetrofitService::class.java)
    }

}