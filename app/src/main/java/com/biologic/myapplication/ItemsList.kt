package com.biologic.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItemsList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_list)

        // INICIO TESTE
        val arrayAdapter: ArrayAdapter<*>
        val users = arrayOf(
            "Virat Kohli", "Rohit Sharma", "Steve Smith",
            "Kane Williamson", "Ross Taylor", "Virat Kohli"
        )

        // access the listView from xml file
        var mListView = findViewById<ListView>(R.id.filesList)
        arrayAdapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, users)
        mListView.adapter = arrayAdapter

        // FIM TESTE
    }

    fun getFiles() {
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