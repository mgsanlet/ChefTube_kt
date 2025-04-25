package com.mgsanlet.cheftube.data.source.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mgsanlet.cheftube.domain.model.DomainUser as User
import com.mgsanlet.cheftube.data.util.Constants.Database
import com.mgsanlet.cheftube.domain.util.DomainResult
import com.mgsanlet.cheftube.domain.util.error.UserError
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
                ${Database.COLUMN_USERNAME} TEXT UNIQUE NOT NULL,
                ${Database.COLUMN_EMAIL} TEXT UNIQUE NOT NULL,
                ${Database.COLUMN_PASSWORD} TEXT NOT NULL
            )
        """.trimIndent()
    }

    fun insertUser(user: User): DomainResult<Unit, UserError> {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(Database.COLUMN_ID, user.id)
                put(Database.COLUMN_USERNAME, user.username)
                put(Database.COLUMN_EMAIL, user.email)
                put(Database.COLUMN_PASSWORD, user.password)
            }

            val result = db.insert(Database.TABLE_USERS, null, values)
            if (result != -1L){
                DomainResult.Success(Unit)
            }else{
                DomainResult.Error(UserError.Unknown())
            }
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    fun updateUser(user: User): DomainResult<Unit, UserError> {
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
            if (result > 0){
                DomainResult.Success(Unit)
            }else{
                DomainResult.Error(UserError.Unknown())
            }
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    fun getUserById(id: String): DomainResult<User, UserError> {
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
                "${Database.COLUMN_ID} = ?",
                arrayOf(id),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    val user = User(
                        id = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_ID)),
                        username = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_USERNAME)),
                        email = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_EMAIL)),
                        password = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_PASSWORD))
                    )
                    DomainResult.Success(user)
                } else {
                    DomainResult.Error(UserError.UserNotFound)
                }
            }
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    fun getUserByName(name: String): DomainResult<User, UserError> {
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
                "${Database.COLUMN_USERNAME} = ?",
                arrayOf(name),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    val user = User(
                        id = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_ID)),
                        username = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_USERNAME)),
                        email = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_EMAIL)),
                        password = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_PASSWORD))
                    )
                    DomainResult.Success(user)
                } else {
                    DomainResult.Error(UserError.UserNotFound)
                }
            }
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    fun getUserByEmail(email: String): DomainResult<User, UserError> {
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
                "${Database.COLUMN_EMAIL} = ?",
                arrayOf(email),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    val user = User(
                        id = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_ID)),
                        username = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_USERNAME)),
                        email = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_EMAIL)),
                        password = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_PASSWORD))
                    )
                    DomainResult.Success(user)
                } else {
                    DomainResult.Error(UserError.UserNotFound)
                }
            }
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
        }
    }

    fun getUserByEmailOrUsername(identity: String): DomainResult<User, UserError> {
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
                "${Database.COLUMN_EMAIL} = ? OR ${Database.COLUMN_USERNAME} = ?",
                arrayOf(identity, identity),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    val user = User(
                        id = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_ID)),
                        username = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_USERNAME)),
                        email = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_EMAIL)),
                        password = it.getString(it.getColumnIndexOrThrow(Database.COLUMN_PASSWORD))
                    )
                    DomainResult.Success(user)
                } else {
                    DomainResult.Error(UserError.UserNotFound)
                }
            }
        } catch (e: Exception) {
            DomainResult.Error(UserError.Unknown(e.message))
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