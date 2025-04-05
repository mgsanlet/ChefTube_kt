package com.mgsanlet.cheftube.chore

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mgsanlet.cheftube.data.provider.UserProvider

class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla de usuarios
        db.execSQL(UserProvider.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En caso de actualización, eliminar tablas existentes y recrear
        db.execSQL("DROP TABLE IF EXISTS ${UserProvider.TABLE_USERS}")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "cheftube.db"
        private const val DATABASE_VERSION = 1
    }
}
