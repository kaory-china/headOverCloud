package com.biologic.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userLogin: EditText = findViewById(R.id.login)
        val userPassword: EditText = findViewById(R.id.senha)
        val db = DatabaseManager(this, "login")

        // LOGIN DE USUARIO EXISTENTE
        val loginButton: Button = findViewById(R.id.button)

        loginButton.setOnClickListener(View.OnClickListener {
            val users = db.getUsers()

            var dbLogin = ""
            var dbPassword = ""
            if (users.count > 0) {
                users.moveToFirst()
                dbLogin = users.getString(users.getColumnIndex("login"))
                dbPassword = users.getString(users.getColumnIndex("password"))
            }

            if (userLogin.editableText.toString().equals(dbLogin) && userPassword.editableText.toString().equals(dbPassword)) {
                Toast.makeText(this, "Bem vindo(a) de volta!.", Toast.LENGTH_SHORT).show()
                val i = Intent(this, ItemsList::class.java)
                startActivity(i)
            } else {
                Toast.makeText(this, "Login inválido.", Toast.LENGTH_SHORT).show()
            }

        })

        // BOTAO DE SE REGISTRAR
        val registerButton: Button = findViewById(R.id.cadastrar)

        registerButton.setOnClickListener(View.OnClickListener {
            val i = Intent(this, UserRegister::class.java)
            startActivity(i)
        })

    }
}