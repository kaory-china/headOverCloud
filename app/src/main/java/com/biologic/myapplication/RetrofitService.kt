package com.biologic.myapplication

import retrofit2.Call
import retrofit2.http.GET

interface RetrofitService {

    @GET("/pulp/api/v3/status/")
    fun getPulpStatus() : Call<PulpResponse>

    // atrala artifact com content, publication

}