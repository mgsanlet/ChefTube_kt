package com.mgsanlet.cheftube.data.source.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mgsanlet.cheftube.data.model.UserDto
import com.mgsanlet.cheftube.data.util.Constants.Database
import javax.inject.Inject

/**
 * Proveedor de datos para la gestión de usuarios en la base de datos local
 */
class UserLocalDataSource @Inject constructor(
    private val dbHelper: DatabaseHelper
) {

    companion object {

        val CREATE_TABLE = """
            CREATE TABLE ${Database.TABLE_USERS} (
                ${Database.COLUMN_ID} TEXT PRIMARY KEY,
                ${Database.COLUMN_USERNAME} TEXT NOT NULL,
                ${Database.COLUMN_EMAIL} TEXT UNIQUE NOT NULL,
                $${Database.COLUMN_PASSWORD} TEXT NOT NULL
            )
        """.trimIndent()
    }

    fun insertUser(user: UserDto): Boolean {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(Database.COLUMN_ID, user.id)
                put(Database.COLUMN_USERNAME, user.username)
                put(Database.COLUMN_EMAIL, user.email)
                put(Database.COLUMN_PASSWORD, user.password)
            }

            val result = db.insert(Database.TABLE_USERS, null, values)
            result != -1L
        } catch (e: Exception) {
            false
        }
    }

    fun getUserById(id: String): UserDto? {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                Database.TABLE_USERS,
                arrayOf(
                    Database.COLUMN_ID,
                    Database.COLUMN_USERNAME,
                    Database.COLUMN_EMAIL,
                    Database.COLUMN_PASSWORD
                ),
                "$Database.COLUMN_ID = ?",
                arrayOf(id),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    val user = UserDto(
                        id = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_ID)),
                        username = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_USERNAME)),
                        email = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_EMAIL)),
                        password = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_PASSWORD))
                    )
                    user
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getUserByName(name: String): UserDto? {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                Database.TABLE_USERS,
                arrayOf(
                    Database.COLUMN_ID,
                    Database.COLUMN_USERNAME,
                    Database.COLUMN_EMAIL,
                    Database.COLUMN_PASSWORD
                ),
                "$Database.COLUMN_USERNAME = ?",
                arrayOf(name),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    val user = UserDto(
                        id = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_ID)),
                        username = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_USERNAME)),
                        email = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_EMAIL)),
                        password = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_PASSWORD))
                    )
                    user
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getUserByEmail(email: String): UserDto? {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                Database.TABLE_USERS,
                arrayOf(
                    Database.COLUMN_ID,
                    Database.COLUMN_USERNAME,
                    Database.COLUMN_EMAIL,
                    Database.COLUMN_PASSWORD
                ),
                "$Database.COLUMN_EMAIL = ?",
                arrayOf(email),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    val user = UserDto(
                        id = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_ID)),
                        username = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_USERNAME)),
                        email = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_EMAIL)),
                        password = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_PASSWORD))
                    )
                    user
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getUserByEmailOrUsername(identity: String): UserDto? {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                Database.TABLE_USERS,
                arrayOf(
                    Database.COLUMN_ID,
                    Database.COLUMN_USERNAME,
                    Database.COLUMN_EMAIL,
                    Database.COLUMN_PASSWORD
                ),
                "$Database.COLUMN_EMAIL = ? OR $Database.COLUMN_USERNAME = ?",
                arrayOf(identity, identity),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    val user = UserDto(
                        id = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_ID)),
                        username = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_USERNAME)),
                        email = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_EMAIL)),
                        password = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_PASSWORD))
                    )
                    user
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun updateUser(user: UserDto): Boolean {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(Database.COLUMN_USERNAME, user.username)
                put(Database.COLUMN_EMAIL, user.email)
                put(Database.COLUMN_PASSWORD, user.password)
            }

            val result = db.update(
                Database.TABLE_USERS, values, "${Database.COLUMN_ID} = ?", arrayOf(user.id)
            )
            result > 0
        } catch (e: Exception) {
            false
        }
    }
}

class DatabaseHelper @Inject constructor(
    context: Context
) :
    SQLiteOpenHelper(context, Database.NAME, null, Database.VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla de usuarios
        db.execSQL(UserLocalDataSource.CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En caso de actualización, eliminar tablas existentes y recrear
        db.execSQL("DROP TABLE IF EXISTS ${Database.TABLE_USERS}")
        onCreate(db)
    }
}