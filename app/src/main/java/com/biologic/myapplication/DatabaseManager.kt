package com.biologic.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseManager(context: Context, name: String?) : SQLiteOpenHelper(context, name, null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val criarTabela =
            "CREATE TABLE tbl_login (id_login INT NOT NULL, login VARCHAR(20), password VARCHAR(20), PRIMARY KEY (id_login));"
        db!!.execSQL(criarTabela)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS tbl_login")
        onCreate(db)
    }

    fun registerNewUser(id: Int, login: String, password: String) {
        var db = this.writableDatabase

        var cv = ContentValues()
        cv.put("id_login", id)
        cv.put("login", login)
        cv.put("password", password)

        db.insert("tbl_login", "id_login", cv)
    }

    fun getUsers(): Cursor {
        var db = this.readableDatabase
        var cur = db.rawQuery("SELECT login, password FROM tbl_login", null)

        return cur
    }

    fun deleteUsers() {
        var db = this.writableDatabase
        db.delete("tbl_login", "id_login=1", null)
    }


}