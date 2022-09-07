package com.biologic.myapplication

import com.biologic.myapplication.domain.*
import com.example.myfirstapp.PulpResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface RetrofitService {

    @GET("/pulp/api/v3/status/")
    fun getPulpStatus(): Call<PulpResponse>

    // Get a file repository
    //@GET("/pulp/api/v3/repositories/file/file/?name={fileName}")
    @GET("/pulp/api/v3/repositories/file/file/")
    fun getPulpFileRepository(@Query("name") name: String): Call<PulpFileRepository>

    // Create a new file repository
    // it should return a 201
    // name is the only required field (the others are optional)
    @Headers("Content-Type: application/json")
    @POST("/pulp/api/v3/repositories/file/file/")
    //fun createFileRepository(@Body pulpFileRepo: PulpFileRepository): Call<PulpFileRepository>
    suspend fun createFileRepository(@Body pulpFileRepo: PulpFileRepository): Response<PulpFileRepository>

    // Upload a new file to Pulp
    // it should return a 201
    /* @Headers("Content-Type: application/json") */
    @Multipart
    @POST("/pulp/api/v3/artifacts/")
    //fun uploadFile(@Part file: MultipartBody.Part): Call<PulpArtifact>
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<PulpArtifact>

    // Create file content from Artifact
    // it should return a 202
    /* @Headers("Content-Type: application/json") */
    @FormUrlEncoded
    @POST("/pulp/api/v3/content/file/files/")
    suspend fun createContent(
        @Field("relative_path") relativePath: String,
        @Field("artifact") artifact: String
    ): Response<PulpContent>
    //): Call<PulpContent>

    // Return the Content from relative_path name
    @Headers("Content-Type: application/json")
    @GET("/pulp/api/v3/content/file/files/")
    suspend fun getFileContent(@Query("relative_path") relativePath: String?): Response<PulpContentList>

    @GET("/pulp/api/v3/content/file/files/")
    suspend fun getFileContents(): Response<PulpContentList>

    // Add file content to repository
    // it should return a 202
    @FormUrlEncoded
    @POST("{pulp_href_repository}modify/")
    suspend fun addContentToRepo(
        @Path("pulp_href_repository") repo: String,
        @Field("add_content_units") content: PulpContentList
    ): Response<Task>

}