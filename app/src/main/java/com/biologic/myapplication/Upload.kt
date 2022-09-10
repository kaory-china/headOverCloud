package com.biologic.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import com.biologic.myapplication.domain.*
import com.example.myfirstapp.CreateDistribution
import com.example.myfirstapp.CreatePublication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import java.io.File
import java.lang.Thread.sleep
import java.net.URI
import java.security.MessageDigest

class Upload : AppCompatActivity() {

    val requestCode = 111
    var uri: Uri? = null
    var file: File? = null
    var fileName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

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
            uri = data?.data
            println("URI = " + uri)
            fileName = DocumentFile.fromSingleUri(this, data!!.data!!)!!.name
        }
    }

    // upload file to Pulp
    private fun uploadFile(fileName: String, uri: Uri) {
        val service: RetrofitService = RetrofitFactory(this).retrofitService()

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

        // get sha256 from file
        val md = MessageDigest.getInstance("SHA-256")
        val input = uploadFileTest.readBytes()
        val bytes = md.digest(input)
        val sha256File = bytes.map { String.format("%02X", it) }.joinToString(separator = "")
            .lowercase()

        var job = CoroutineScope(Dispatchers.IO).launch {

            val REPO_NAME = getResources().getString(R.string.repository_name)
            val DISTRO_BASE_PATH = getResources().getString(R.string.distribution_base_path)
            val DISTRO_NAME = getResources().getString(R.string.distribution_name)

            val artifact = service.getArtifact(sha256File).body()!!
            if (artifact.count == 0) {
                val uploadFileResponse = service.uploadFile(multipartBody)
                println(uploadFileResponse)
                println("Response body from artifact: " + uploadFileResponse.body())

                val createContentResponse =
                    service.createContent(fileName, uploadFileResponse.body()!!.pulp_href!!)
                println("Response body from createContent: " + createContentResponse.body())
            }

            // create a new pulp repo
            var reposResponse = service.getRepos(REPO_NAME).body()!!.results
            println("Response from getRepos: " + reposResponse)
            if (reposResponse!!.isEmpty()) {
                val newRepo = PulpFileRepository(
                    REPO_NAME,
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
                reposResponse.add(service.createFileRepository(newRepo).body()!!)
                println("Response body from createFileRepository: " + reposResponse)
            }
            reposResponse = service.getRepos(REPO_NAME).body()!!.results
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

            // adding file content into a repository
            val modifyContent = ModifyContent(
                arrayListOf(fileContentList.results!![0].pulp_href!!),
                ArrayList<String>(),
                reposResponse!![0].pulp_href + latestVersion
            )
            var task =
                service.addContentToRepo(reposResponse!![0].pulp_href!!, modifyContent)
            println("Response from addContentToRepo: " + task)

            // wait until the task to add content to repo finishes
            for (i in 1..10) {
                val taskStatus = service.getTask(task.body()!!.task!!)
                println("taskStatus: " + taskStatus)
                println("taskStatusBody: " + taskStatus.body())
                println("taskStatusBody: " + task.body())
                if (taskStatus.body()!!.state == "completed") {
                    break
                }
                sleep(1000)
            }

            println("PULP REPO FOUND: " + reposResponse!![0].pulp_href + latestVersion)

            // creating publication to a repository
            reposResponse = service.getRepos(REPO_NAME).body()!!.results
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

            val distributionList = service.getDistribution(DISTRO_NAME).body()!!
            if (distributionList.count == 0) {
                // create a new distribution with the publication found
                var distributionCreateResponse = CreateDistribution(
                    DISTRO_BASE_PATH,
                    null,
                    null,
                    DISTRO_NAME,
                    null,
                    publication.pulp_href,
                )
                task = service.createDistribution(distributionCreateResponse)
                println("Response from createDistribution: " + task)
            } else {
                var updateDistribution =
                    UpdateDistribution(DISTRO_BASE_PATH, null, DISTRO_NAME, publication.pulp_href!!)
                task = service.updateDistribution(
                    distributionList.results!![0].pulp_href,
                    updateDistribution
                )
                println("Response from updateDistribution: $task")
            }

        }
        runBlocking {
            job.join()
        }

    }
}