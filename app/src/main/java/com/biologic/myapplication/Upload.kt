package com.biologic.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.biologic.myapplication.domain.*
import com.example.myfirstapp.CreateDistribution
import com.example.myfirstapp.CreatePublication
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

class Upload : AppCompatActivity() {

    val service: RetrofitService = RetrofitFactory().retrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        var path: TextView = findViewById(R.id.path)
        var fileName: EditText = findViewById(R.id.nome_arquivo)

        val browse: Button = findViewById(R.id.browse)
        browse.setOnClickListener(View.OnClickListener {
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            path.text = startActivityForResult(
                Intent.createChooser(intent, "Select a file"),
                111
            ).toString()
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
            path.text = data?.data.toString()
        }
    }

    // upload file to Pulp
    private fun uploadFile(path: String, fileName: String) {

        // [TODO]: This should be captured by the upload file button
        val uploadFileTest = File(path, fileName)

        var body: RequestBody = RequestBody.create(null, path)
        val multipartBody = MultipartBody.Part.createFormData(fileName, fileName, body)

        var job = CoroutineScope(Dispatchers.IO).launch {

            println(multipartBody)
            println(fileName+" .....  "+path)
            // [TODO] we should check if the artifact already exists
            // create pulp artifact
            val uploadFileResponse = service.uploadFile(multipartBody)
            println(uploadFileResponse)
            println("Response body from artifact: " + uploadFileResponse.body())

            // [TODO] we should check if the artifact failed to be created
            // [TODO] we should check if the content already exists
            // create file content from artifact
            val createContentResponse =
                service.createContent(fileName, uploadFileResponse.body()!!.pulp_href!!)
            println("Response body from createContent: " + createContentResponse.body())

            // [TODO] this should run only a single time for the
            //        entire app life!
            // create a new pulp repo
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
            val pulpFileRepository = service.createFileRepository(newRepo)
            println("Response body from createFileRepository: " + pulpFileRepository.body())

            // get file content created
            // pulp api does not return this information on create request
            var fileContentList: PulpContentList = PulpContentList(0, null, null, null)
            for (i in 1..10) {
                val responseGetFileContent = service.getFileContent(fileName)
                if (responseGetFileContent.body()!!.count!! > 0) {
                    fileContentList = responseGetFileContent.body()!!
                    break
                }
                Thread.sleep(1000)
            }
            println("Response body from getFileContent: " + fileContentList)

            // [TODO] in a re-execution the repository will not be recreated,
            //        so pulpFileRepository var will be null ... we need to handle this scenario
            // adding file content into a repository
            val modifyContent: ModifyContent = ModifyContent(
                arrayListOf(fileContentList.results!![0].pulp_href!!),
                ArrayList<String>(),
                pulpFileRepository.body()!!.pulp_href + "versions/0/"
            )
            var task =
                service.addContentToRepo(pulpFileRepository.body()!!.pulp_href!!, modifyContent)
            println("Response from addContentToRepo: " + task)

            // [TODO] in a re-execution the repository will not be recreated,
            //        so pulpFileRepository var will be null ... we need to handle this scenario
            // creating publication to a repository
            val publicationCreateResponse = CreatePublication(
                null,
                pulpFileRepository.body()!!.pulp_href,
                fileName
            )
            task = service.createPublication(publicationCreateResponse)
            println("Response from createPublication: " + task)

            var publication: Publication = Publication(null, null, null, null, null, null)

            // wait until publication is available in pulp
            // (it takes some time for it to get created)
            // and when it is found assign it to "publication" var
            loop@ for (i in 0..10) {
                var publications = service.getPublications()
                println("Response from getPublications: " + publications)
                for (i in publications.body()!!.results!!) {
                    if (i.manifest == fileName) {
                        publication = i
                        println("Publication found! : " + publication)
                        break@loop
                    }
                }
                Thread.sleep(1000)
            }

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
        }
        runBlocking { job.join() }
    }


}