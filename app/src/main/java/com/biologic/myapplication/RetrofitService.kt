package com.biologic.myapplication

import com.biologic.myapplication.domain.PulpArtifact
import com.biologic.myapplication.domain.PulpContent
import com.biologic.myapplication.domain.PulpFileRepository
import com.example.myfirstapp.PulpResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {

    @GET("/pulp/api/v3/status/")
    fun getPulpStatus() : Call<PulpResponse>

    // Get a file repository
    //@GET("/pulp/api/v3/repositories/file/file/?name={fileName}")
    @GET("/pulp/api/v3/repositories/file/file/")
    fun getPulpFileRepository(@Query("name") name: String ) : Call<PulpFileRepository>

    // Create a new file repository
    // it should return a 201
    // name is the only required field (the others are optional)
    @Headers("Content-Type: application/json")
    @POST("/pulp/api/v3/repositories/file/file/")
    fun createFileRepository(@Body pulpFileRepo: PulpFileRepository): Call<PulpFileRepository>

    // Upload a new file to Pulp
    // it should return a 201
    /* @Headers("Content-Type: application/json") */
    @Multipart
    @POST("/pulp/api/v3/artifacts/")
    fun uploadFile(@Part file: MultipartBody.Part): Call<PulpArtifact>

    // Create file content from Artifact
    // it should return a 201
    /* @Headers("Content-Type: application/json") */
    @FormUrlEncoded
    @POST("/pulp/api/v3/content/file/files/")
    fun createContent(@Field("relative_path") relativePath: String, @Field("artifact") artifact: String): Call<PulpContent>

}