package com.biologic.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.biologic.myapplication.domain.PulpArtifact
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
import java.io.File

class Upload : AppCompatActivity() {

    val service: RetrofitService = RetrofitFactory().retrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        pulpStatus()
        getFileRepos()

        var path: TextView = findViewById(R.id.path)
        var fileName: EditText = findViewById(R.id.nome_arquivo)

        val browse: Button = findViewById(R.id.browse)
        browse.setOnClickListener(View.OnClickListener {
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            path.text = startActivityForResult(Intent.createChooser(intent, "Select a file"), 111).toString()
        })

        val upload: Button = findViewById(R.id.salvar)
        upload.setOnClickListener(View.OnClickListener {
            uploadFile(path.text.toString(), fileName.editableText.toString())
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val path: TextView = findViewById(R.id.path)
        if (requestCode == 111 && resultCode == RESULT_OK) {
            path.text = data?.dataString
        }
    }

    // get Pulp status before proceeding
    // we need to make sure that pulp is running before running the other tasks
    fun pulpStatus() {
        println("Checking Pulp connection...")

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