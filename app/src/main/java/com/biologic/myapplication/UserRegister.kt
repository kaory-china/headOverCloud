package com.biologic.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class UserRegister : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_register)

        val login: EditText = findViewById(R.id.newLogin)
        val password: EditText = findViewById(R.id.newPassword)
        val db = DatabaseManager(this, "login")
        val i = Intent(this, ItemsList::class.java)

        val registerButton: Button = findViewById(R.id.buttonCadastrar)
        registerButton.setOnClickListener( View.OnClickListener {
            db.deleteUsers()
            db.registerNewUser(1, login.editableText.toString(), password.editableText.toString())
            Toast.makeText(this, "Registro feito com sucesso.", Toast.LENGTH_SHORT).show()
            startActivity(i)
        })
    }
}