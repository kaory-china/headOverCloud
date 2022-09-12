package com.biologic.myapplication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.biologic.myapplication.domain.PulpContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


class ItemsList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_item)

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

        adapter!!.setOnClickListener(object : ClickListener<PulpContent> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onClickListener(content: PulpContent) {
                Log.i("SOLICITANDO ARQUIVO DO CONTENT: ", content.toString())
                getFile(content.relative_path.toString())
            }
        })

    }

    // getFileContents returns the list of pulp content files
    fun getFileContents(): ArrayList<PulpContent> {
        val service: RetrofitService = RetrofitFactory(this).retrofitService()
        var contentList = ArrayList<PulpContent>()

        // we are assigning the output from launch so that we can block
        // the thread execution until we get the response from request
        var job = CoroutineScope(Dispatchers.IO).launch {
            val response = service.getFileContent()
            contentList = response.body()?.results!!
            Log.i("Response body from getFileContents:", response.body().toString())
        }

        // block thread until we get the response from getFileContents
        runBlocking { job.join() }
        return contentList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getFile(fileName: String) {
        println("Downloading file: $fileName")
        val service = RetrofitFactory(this).retrofitService()

        val job = CoroutineScope(Dispatchers.IO).launch {
            val distribution = service.getDistribution("new_dist").body()!!.results!![0].base_path
            println("distribution = : $distribution")
            val path =
                getResources().getString(R.string.ip_address) + "/pulp/content/$distribution/$fileName"
            println("path = : $path")

            try {
                println(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS))
                var newFile = URL(path).openStream();
                println("newFile = : $newFile")
                var pathGet = Paths.get("storage/emulated/0/Documents/$fileName")
                println(pathGet)
                Files.copy(newFile, pathGet, StandardCopyOption.REPLACE_EXISTING)
                println("ARQUIVO BAIXADO")
            } catch (e: Exception) {
                println(e)
            }
        }
        runBlocking { job.join() }
    }
}

