package com.biologic.myapplication

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import com.biologic.myapplication.domain.*
import com.example.myfirstapp.CreateDistribution
import com.example.myfirstapp.CreatePublication
import com.example.myfirstapp.PulpResponse
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream
import java.lang.Thread.*
import java.net.URI
import java.util.Objects.isNull

class Upload : AppCompatActivity() {

    val service: RetrofitService = RetrofitFactory().retrofitService()

    val newRepo = PulpFileRepository(
        "test",
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
    val requestCode = 111
    var uri: Uri? = null
    var path: String? = null
    var file: File? = null
    var type: String? = null
    var fileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        var path: TextView = findViewById(R.id.path)


        val browse: Button = findViewById(R.id.browse)
        browse.setOnClickListener(View.OnClickListener {
            val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, "msg"), requestCode)
        })

        val upload: Button = findViewById(R.id.salvar)

        upload.setOnClickListener(View.OnClickListener {

            uploadFile(fileName!!, uri!!)

            val i = Intent(this, ItemsList::class.java)
            startActivity(i)
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCode && resultCode == Activity.RESULT_OK) {
            val directoryUri = data?.data ?: return
            uri = data?.data
            println("URI = " + uri)
            type = data.type
            println("type = " + type)

            fileName = DocumentFile.fromSingleUri(this,data!!.data!!)!!.name
            //file = directoryUri.toFile()
        }
    }

    // upload file to Pulp
    private fun uploadFile(fileName: String, uri: Uri) {
        // [TODO]: This should be captured by the upload file button
        //val uploadFileTest = File(path, fileName)

        val uploadFileTest = File("/storage/emulated/0/Download/", fileName)
        val testeFile = Uri.fromFile(uploadFileTest).toFile()

        //var body: RequestBody = RequestBody.create(null, file)
        val contentPart = InputStreamRequestBody(null, applicationContext.contentResolver, uri)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("", "file", contentPart)
            .build()
        println("REQUEST-BODY = " + requestBody)
        val multipartBody = MultipartBody.Part.createFormData("file", fileName, contentPart)

        var job = CoroutineScope(Dispatchers.IO).launch {

            println("ContentPart = : " + contentPart)
            println("MultipartBody: " + multipartBody)
            println("fileName: " + fileName)
            println("path: " + path)

            // [TODO] we should check if the artifact already exists
            // create pulp artifact
            val uploadFileResponse = service.uploadFile(multipartBody)
            println("Response from artifact: " + uploadFileResponse)
            println("Response BODY from artifact: " + uploadFileResponse.body())

            // [TODO] we should check if the artifact failed to be created
            // [TODO] we should check if the content already exists
            // create file content from artifact
            val createContentResponse =
                service.createContent(fileName, uploadFileResponse.body()!!.pulp_href!!)
            println("Response body from createContent: " + createContentResponse.body())

            // [TODO] this should run only a single time for the
            //        entire app life!
            // create a new pulp repo


            var reposResponse = service.getRepos("test").body()!!.results
            println("Response from getRepos: " + reposResponse)
            if (reposResponse!!.isEmpty()) {
                reposResponse.add(service.createFileRepository(newRepo).body()!!)
                println("Response body from createFileRepository: " + reposResponse)
            }
            reposResponse = service.getRepos("test").body()!!.results
            var uri = URI(reposResponse!![0].latest_version_href)
            var latestVersion = "versions/" + uri.path.substring(uri.path.lastIndexOf('/') - 1)
            println("LATEST VERSION = $latestVersion")

            // get file content created
            // pulp api does not return this information on create request
            var fileContentList = PulpContentList(0, null, null, null)
            for (i in 1..10) {
                val responseGetFileContent = service.getFileContent(fileName)
                if (responseGetFileContent.body()!!.count!! > 0) {
                    fileContentList = responseGetFileContent.body()!!
                    break
                }
                sleep(1000)
            }
            println("Response body from getFileContent: " + fileContentList)

            // [TODO] in a re-execution the repository will not be recreated,
            //        so pulpFileRepository var will be null ... we need to handle this scenario
            // adding file content into a repository
            val modifyContent = ModifyContent(
                arrayListOf(fileContentList.results!![0].pulp_href!!),
                ArrayList<String>(),
                reposResponse!![0].pulp_href + latestVersion
            )
            var task =
                service.addContentToRepo(reposResponse!![0].pulp_href!!, modifyContent)
            println("Response from addContentToRepo: " + task)


            sleep(10000)
            println("PULP REPO FOUND: " + reposResponse!![0].pulp_href + latestVersion)
            // [TODO] in a re-execution the repository will not be recreated,
            //        so pulpFileRepository var will be null ... we need to handle this scenario
            // creating publication to a repository

            reposResponse = service.getRepos("test").body()!!.results
            uri = URI(reposResponse!![0].latest_version_href)
            latestVersion = "versions/" + uri.path.substring(uri.path.lastIndexOf('/') - 1)
            println("LATEST VERSION = $latestVersion")
            val publicationCreateResponse = CreatePublication(
                reposResponse!![0].pulp_href + latestVersion
            )
            task = service.createPublication(publicationCreateResponse)
            println("Response from createPublication: " + task)

            var publication = Publication(null, null, null, null, null, null)

            // wait until publication is available in pulp
            // (it takes some time for it to get created)
            // and when it is found assign it to "publication" var
            // retry checks
            loop@ for (i in 0..10) {
                var publications = service.getPublications()
                println("Response from getPublications: " + publications)
                for (i in publications.body()!!.results!!) {
                    if (i.repository_version == reposResponse!![0]!!.pulp_href + latestVersion) {
                        publication = i
                        println("Publication found! : " + publication)
                        break@loop
                    }
                }
                sleep(1000)
            }

            val distributionList = service.getDistribution("new_dist").body()!!
            if (distributionList.count == 0) {
                // create a new distribution with the publication found
                var distributionCreateResponse = CreateDistribution(
                    "fiap",
                    null,
                    null,
                    "new_dist",
                    null,
                    publication.pulp_href,
                )
                task = service.createDistribution(distributionCreateResponse)
                println("Response from createDistribution: " + task)
            } else {
                var updateDistribution =
                    UpdateDistribution("fiap", null, "new_dist", publication.pulp_href!!)
                task = service.updateDistribution(
                    distributionList.results!![0].pulp_href,
                    updateDistribution
                )
                println("Response from updateDistribution: $task")
            }

        }
        runBlocking {
            job.join() }

    }
}