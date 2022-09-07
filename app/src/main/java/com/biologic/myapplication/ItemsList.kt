package com.biologic.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.biologic.myapplication.domain.PulpArtifact
import com.biologic.myapplication.domain.PulpContent
import com.biologic.myapplication.domain.PulpContentList
import com.biologic.myapplication.domain.PulpFileRepository
import com.example.myfirstapp.PulpResponse
import kotlinx.coroutines.*
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class ItemsList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_list)

        var contentList = getFileContents()

        var layoutManager: RecyclerView.LayoutManager?
        var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>?
        var recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        adapter = RecyclerAdapter(contentList)
        recyclerView.adapter = adapter

        val perfilButton: Button = findViewById(R.id.perfil)
        perfilButton.setOnClickListener(View.OnClickListener {
            val i = Intent(this, Perfil::class.java)
            startActivity(i)
        })

        val uploadButton: Button = findViewById(R.id.addFile)
        uploadButton.setOnClickListener(View.OnClickListener {
            val i = Intent(this, Upload::class.java)
            startActivity(i)
        })

    }

    fun getFileContents(): ArrayList<PulpContent> {
        val service: RetrofitService = RetrofitFactory().retrofitService()
        var contentList = ArrayList<PulpContent>()
        var test = CoroutineScope(Dispatchers.IO).launch {
            for (i in 1..10) {
                val response = service.getFileContents()
                Log.i("Response body from createFileRepository:", response.body().toString())
                if (! response.body()?.results!!.isNullOrEmpty()) {
                    contentList = response.body()?.results!!
                    break
                }
                Thread.sleep(1000)
            }
        }
        runBlocking { test.join() }
        return contentList
    }

    // get Pulp status before proceeding
    // we need to make sure that pulp is running before running the other tasks
    fun pulpStatus() {
        println("Checking Pulp connection...")
        val service: RetrofitService = RetrofitFactory().retrofitService()
        service.getPulpStatus().enqueue(object : Callback<PulpResponse> {
            override fun onResponse(
                call: Call<PulpResponse>,
                response: Response<PulpResponse>
            ) {
                println(response)

                // we are expecting a 200 OK AND
                // just as a double-check checking if pulpcore is connected to database before proceeding
                if ((response.code() == 200) && (response.body()?.database_connection?.connected == true)) {
                    println(response.body())
                }
            }

            override fun onFailure(call: Call<PulpResponse>, t: Throwable) {
                println("fdsjklfjsflsjfflskdls fsdkfklfskfljsklfjdskfljsklfjslfkdslfkdsdfs")
                println(t)
            }
        })
    }

    // get Pulp Repository before proceeding
    // we need to make sure that the repo does not exist to avoid issues trying to
    // create another file repository with the same name
    fun getFileRepos() {
        val service: RetrofitService = RetrofitFactory().retrofitService()
        val repos: ArrayList<PulpFileRepository>? = null

        service.getPulpFileRepository("test").enqueue(object : Callback<PulpFileRepository> {
            override fun onResponse(
                call: Call<PulpFileRepository>,
                response: Response<PulpFileRepository>
            ) {
                println(response)

                // we are expecting a 200 OK AND
                // just as a double-check checking if pulpcore is connected to database before proceeding
                if (response.code() == 200) {
                    println("Response from getFileRepos: " + response.body()?.results)
                    repos?.addAll(response.body()?.results!!)
                }
            }

            override fun onFailure(call: Call<PulpFileRepository>, t: Throwable) {
                println("fdsjklfjsflsjfflskdls fsdkfklfskfljsklfjdskfljsklfjslfkdslfkdsdfs")
                println(t)
            }
        })
    }

    // crate a Pulp File Repository
    fun createFileRepos(createdRepo: ArrayList<PulpFileRepository>) {
        val service: RetrofitService = RetrofitFactory().retrofitService()
        val newRepo = PulpFileRepository(
            "test2",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )

        CoroutineScope(Dispatchers.IO).launch {
            val response =
                service.createFileRepository(newRepo)
            try {
                println(response)
                // we are expecting a 201 Content Created
                println("Response body from createFileRepository: " + response.body())
                createdRepo.add(response.body()!!)
            } catch (e: Exception) {
                println("Exception ${e.message}")
            }
        }
    }

    // upload file to Pulp
    private fun uploadFile(filePath: String, fileName: String) {
        val service: RetrofitService = RetrofitFactory().retrofitService()
/*        val fileName = "download.jpeg"
        val uploadFileTest = File("/storage/emulated/0/Download/", fileName)

        val service = retrofit.create(PulpService::class.java)
        var body: RequestBody = RequestBody.create(null, uploadFileTest)
        val multipartBody = MultipartBody.Part.createFormData("file", fileName, body)*/

        //fun createUploadRequestBody(file: File) = file.asRequestBody()
        var body: RequestBody = RequestBody.create(null, filePath)
        val multipartBody = MultipartBody.Part.createFormData("file", fileName, body)

        CoroutineScope(Dispatchers.IO).launch {
            val response = service.uploadFile(multipartBody)
            response.body()
            try {
                println(response)
                println("Response body from artifact: " + response.body())
                artifactToContent(response.body(), fileName) // + create content from artifact

                var newRepository: ArrayList<PulpFileRepository> = ArrayList<PulpFileRepository>()
                createFileRepos(newRepository) // + create repository

                // workaround!!
                // this is a retry to check again with pulp if the content is available
                var contentList: ArrayList<PulpContentList> = ArrayList<PulpContentList>()

                val result: Deferred<String> = async {
                    for (i in 1..10) {
                        getFileContent(fileName, contentList)
                        if (contentList.size > 0) {
                            break
                        }
                        Thread.sleep(1000)
                    }
                    "finished"
                }
                result.await()
                println("contentList: " + contentList)

                addContentToRepo(newRepository[0].pulp_href, contentList[0])
            } catch (e: Exception) {
                println("Exception ${e.message}")
            }
        }
    }

    // artifactToContent creates a file Content from Artifact
    fun artifactToContent(artifact: PulpArtifact?, fileName: String?) {
        val service: RetrofitService = RetrofitFactory().retrofitService()
        CoroutineScope(Dispatchers.IO).launch {
            val response =
                service.createContent(fileName.toString(), artifact?.pulp_href.toString())
            try {
                println(response)

                // the expected response is a 202 and the
                // PulpContent should have all fields null
                // (pulp does not return a body when creating content from artifact)
                println("Response body from createContent: " + response.body())
            } catch (e: Exception) {
                println("Exception ${e.message}")
            }
        }
    }

    // getFileContent retrieves a file content based on its relative_path name
    fun getFileContent(relativePath: String?, contentList: ArrayList<PulpContentList>?) {
        val service: RetrofitService = RetrofitFactory().retrofitService()
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getFileContent(relativePath.toString())
            try {
                println(response)
                println("Response body from getFileContent: " + response.body())
                contentList?.add(response.body()!!)
            } catch (e: Exception) {
                println("Exception ${e.message}")
            }
        }
    }

    // addContentToRepo adds a content to a repository
    private fun addContentToRepo(repoHref: String?, contentList: PulpContentList) {
        val service: RetrofitService = RetrofitFactory().retrofitService()
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.addContentToRepo(repoHref.toString(), contentList)
            try {
                println(response)
                println("Response body from addContentToRepo: " + response.body())
            } catch (e: Exception) {
                println("Exception ${e.message}")
            }
        }
    }
}