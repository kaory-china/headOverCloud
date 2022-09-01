package com.biologic.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // LOGO DA EMPRESA
        val logo: ImageView = findViewById(R.id.logo)
        var logoUrl = "https://raw.githubusercontent.com/eumagnun/imagens_apoio/main/leao.jpg"
        Glide.with(this).load(logoUrl).into(logo!!)

        val login: EditText = findViewById(R.id.login)
        val password: EditText = findViewById(R.id.senha)
        val db = DatabaseManager(this, "login")

        // REGISTRO DE NOVO USUARIO
        val registerButton: Button = findViewById(R.id.button)
        registerButton.setOnClickListener() {
            db.deleteUsers()
            db.registerNewUser(1, login.text.toString(), password.text.toString())
            Toast.makeText(this, "Registro feito com sucesso.", Toast.LENGTH_SHORT).show()

            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.fiap.com.br"))
            startActivity(i)
        }

        // LOGIN DE USUARIO EXISTENTE
        val loginButton: Button = findViewById(R.id.buttonCadastrar)
        loginButton.setOnClickListener() {
            val users = db.getUsers()

            var dbLogin = ""
            var dbPassword = ""
            if (users.count > 0) {
                users.moveToFirst()
                dbLogin = users.getString(users.getColumnIndex("login"))
                dbPassword = users.getString(users.getColumnIndex("password"))
            }

            if (login.equals(dbLogin) && password.equals(dbPassword)) {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.fiap.com.br"))
                startActivity(i)
            } else {
                Toast.makeText(this, "Login inválido.", Toast.LENGTH_SHORT).show()
            }

        }

    }
}