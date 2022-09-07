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

        getDistribution()

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

    // getFileContents returns the list of pulp content files
    fun getDistribution(): ArrayList<PulpContent> {
        val service: RetrofitService = RetrofitFactory().retrofitService()
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

    // getFileContents returns the list of pulp content files
    fun getFileContents(): ArrayList<PulpContent> {
        val service: RetrofitService = RetrofitFactory().retrofitService()
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

}