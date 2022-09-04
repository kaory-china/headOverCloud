package com.biologic.myapplication

import retrofit2.http.GET
import retrofit2.http.POST

interface RetrofitService {

    @GET("/pulp/api/v3/status/")
    fun getStatus()

    // atrala artifact com content, publication

}