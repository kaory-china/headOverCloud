package com.biologic.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstapp.PulpResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItemsList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_list)

        var layoutManager: RecyclerView.LayoutManager? = null
        var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null
        var recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        adapter = RecyclerAdapter()
        recyclerView.adapter = adapter

        val perfilButton: Button = findViewById(R.id.perfil)
        perfilButton.setOnClickListener(View.OnClickListener {
            val i = Intent(this, Perfil::class.java)
            startActivity(i)
            getFiles()
        })

        val uploadButton: Button = findViewById(R.id.addFile)
        uploadButton.setOnClickListener(View.OnClickListener {
            val i = Intent(this, Upload::class.java)
            startActivity(i)
        })

    }

    fun getFiles() {
        val call = RetrofitFactory().retrofitService().getPulpStatus()

        call.enqueue(object : Callback<PulpResponse> {
            override fun onResponse(call: Call<PulpResponse>, response: Response<PulpResponse>) {
                response.body()?.let {
                    Log.i("PulpResponse.kt", it.toString())

                }
            }

            override fun onFailure(call: Call<PulpResponse>, t: Throwable) {
                t?.message?.let { it1 -> Log.e("Erro", it1) }
            }
        })
    }

    fun openFile() {
        val call = RetrofitFactory().retrofitService().getPulpStatus()

        call.enqueue(object : Callback<PulpResponse> {
            override fun onResponse(call: Call<PulpResponse>, response: Response<PulpResponse>) {
                response.body()?.let {
                    Log.i("CEP", it.toString())
                    Toast.makeText(this@ItemsList, it.toString(), Toast.LENGTH_LONG).show()
                } ?: Toast.makeText(this@ItemsList, "CEP Não Localizado", Toast.LENGTH_LONG)
                    .show()
            }

            override fun onFailure(call: Call<PulpResponse>, t: Throwable) {
                t?.message?.let { it1 -> Log.e("Erro", it1) }
            }
        })
    }

    fun addFile() {

        val call = RetrofitFactory().retrofitService().getPulpStatus()

        call.enqueue(object : Callback<PulpResponse> {
            override fun onResponse(call: Call<PulpResponse>, response: Response<PulpResponse>) {
                response.body()?.let {
                    Log.i("CEP", it.toString())
                    Toast.makeText(this@ItemsList, it.toString(), Toast.LENGTH_LONG).show()
                } ?: Toast.makeText(this@ItemsList, "CEP Não Localizado", Toast.LENGTH_LONG)
                    .show()
            }

            override fun onFailure(call: Call<PulpResponse>, t: Throwable) {
                t?.message?.let { it1 -> Log.e("Erro", it1) }
            }
        })

    }

    fun deleteFile() {

        val call = RetrofitFactory().retrofitService().getPulpStatus()

        call.enqueue(object : Callback<PulpResponse> {
            override fun onResponse(call: Call<PulpResponse>, response: Response<PulpResponse>) {
                response.body()?.let {
                    Log.i("CEP", it.toString())
                    Toast.makeText(this@ItemsList, it.toString(), Toast.LENGTH_LONG).show()
                } ?: Toast.makeText(this@ItemsList, "CEP Não Localizado", Toast.LENGTH_LONG)
                    .show()
            }

            override fun onFailure(call: Call<PulpResponse>, t: Throwable) {
                t?.message?.let { it1 -> Log.e("Erro", it1) }
            }
        })

    }
}