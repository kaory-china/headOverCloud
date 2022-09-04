package com.biologic.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class Perfil : AppCompatActivity() {
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val email: TextView = findViewById(R.id.email)
        val db = DatabaseManager(this, "login")
        val users = db.getUsers()
        if (users.count > 0) {
            users.moveToFirst()
            email.text = users.getString(users.getColumnIndex("login"))
        }

        val logout: Button = findViewById(R.id.logout)
        logout.setOnClickListener(View.OnClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        })
    }
}