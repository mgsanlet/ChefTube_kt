package com.mgsanlet.cheftube.data.provider

import android.content.ContentValues
import android.util.Log
import com.mgsanlet.cheftube.chore.DatabaseHelper
import com.mgsanlet.cheftube.data.model.User

/**
 * Proveedor de datos para la gestión de usuarios en la base de datos local
 */
class UserProvider(private val dbHelper: DatabaseHelper) {
    
    companion object {
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD_HASH = "password_hash"

        val CREATE_TABLE = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_USERNAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD_HASH TEXT NOT NULL
            )
        """.trimIndent()
    }

    fun insertUser(user: User): Boolean {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_ID, user.id)
                put(COLUMN_USERNAME, user.username)
                put(COLUMN_EMAIL, user.email)
                put(COLUMN_PASSWORD_HASH, user.getPasswordHash())
            }
            
            val result = db.insert(TABLE_USERS, null, values)
            result != -1L
        } catch (e: Exception) {
            false
        }
    }

    fun getUserById(id: String): User? {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                TABLE_USERS,
                arrayOf(COLUMN_ID, COLUMN_USERNAME, COLUMN_EMAIL, COLUMN_PASSWORD_HASH),
                "$COLUMN_ID = ?",
                arrayOf(id),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    val user = User(
                        id = it.getString(it.getColumnIndexOrThrow(COLUMN_ID)),
                        username = it.getString(it.getColumnIndexOrThrow(COLUMN_USERNAME)),
                        email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        passwordHash = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH))
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

    fun getUserByName(name: String): User? {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                TABLE_USERS,
                arrayOf(COLUMN_ID, COLUMN_USERNAME, COLUMN_EMAIL, COLUMN_PASSWORD_HASH),
                "$COLUMN_USERNAME = ?",
                arrayOf(name),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    val user = User(
                        id = it.getString(it.getColumnIndexOrThrow(COLUMN_ID)),
                        username = it.getString(it.getColumnIndexOrThrow(COLUMN_USERNAME)),
                        email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        passwordHash = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH))
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

    fun getUserByEmail(email: String): User? {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                TABLE_USERS,
                arrayOf(COLUMN_ID, COLUMN_USERNAME, COLUMN_EMAIL, COLUMN_PASSWORD_HASH),
                "$COLUMN_EMAIL = ?",
                arrayOf(email),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    val user = User(
                        id = it.getString(it.getColumnIndexOrThrow(COLUMN_ID)),
                        username = it.getString(it.getColumnIndexOrThrow(COLUMN_USERNAME)),
                        email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        passwordHash = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH))
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

    fun getUserByEmailOrUsername(identity: String): User? {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                TABLE_USERS,
                arrayOf(COLUMN_ID, COLUMN_USERNAME, COLUMN_EMAIL, COLUMN_PASSWORD_HASH),
                "$COLUMN_EMAIL = ? OR $COLUMN_USERNAME = ?",
                arrayOf(identity, identity),
                null,
                null,
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    val user = User(
                        id = it.getString(it.getColumnIndexOrThrow(COLUMN_ID)),
                        username = it.getString(it.getColumnIndexOrThrow(COLUMN_USERNAME)),
                        email = it.getString(it.getColumnIndexOrThrow(COLUMN_EMAIL)),
                        passwordHash = it.getString(it.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH))
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

    fun updateUser(user: User): Boolean {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_USERNAME, user.username)
                put(COLUMN_EMAIL, user.email)
                put(COLUMN_PASSWORD_HASH, user.getPasswordHash())
            }

            val result = db.update(
                TABLE_USERS,
                values,
                "$COLUMN_ID = ?",
                arrayOf(user.id)
            )
            result > 0
        } catch (e: Exception) {
            false
        }
    }
}
