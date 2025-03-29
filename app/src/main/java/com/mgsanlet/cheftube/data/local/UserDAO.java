package com.mgsanlet.cheftube.data.local;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mgsanlet.cheftube.data.model.User;

/**
 * Data Access Object (DAO) class for managing user-related database operations.
 * This class provides methods for registering new users, validating user credentials,
 * checking for existing usernames and emails, updating user information, and logging
 * the contents of the user database.
 * @author MarioG
 */
public class UserDAO {

    /**
     * Registers a new user by inserting their information into the database.
     *
     * @param newUser The {@link User} object containing the user's information to be registered.
     * @param ctx The context from which the method is called, used to access the database.
     */
    public static void register(User newUser, Context ctx) {
        DataBaseHelper dbHelper = new DataBaseHelper(ctx);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL("INSERT INTO credential (user_id, username, email, password) " +
                            "VALUES (?, ?, ?, ?)",
                    new String[]{newUser.getId(), newUser.getUsername(),
                            newUser.getEmail(), newUser.getPassword()});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("UserDAO", "Error registering user: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
        logDBContent(ctx);
    }

    /**
     * Validates the user credentials by checking if the entered username/email and password
     * match any of the users in the database.
     *
     * @param inputIdentity The username or email entered by the user.
     * @param inputPwd The password entered by the user.
     * @param ctx The context from which the method is called, used to access the database.
     * @return The valid {@link User} object if credentials match, null otherwise.
     */
    public static @Nullable User getValidUser(String inputIdentity, String inputPwd, Context ctx) {
        User validUser = null;
        DataBaseHelper dbHelper = new DataBaseHelper(ctx);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            db.beginTransaction();
            Cursor cursor = db.rawQuery("SELECT * FROM credential " +
                            " WHERE (username = ? OR email = ?) AND password = ?",
                    new String[]{inputIdentity, inputIdentity, inputPwd});
            if (cursor.moveToFirst()) {
                String userid = cursor.getString(0);
                String username = cursor.getString(1);
                String email = cursor.getString(2);
                // Do not log the password for security reasons
                validUser = new User(userid, username, email, cursor.getString(3));
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("UserDAO", "Error validating user: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
        return validUser;
    }

    /**
     * Checks if the entered email already exists in the database.
     *
     * @param inputEmail The email to check for existence.
     * @param ctx The context from which the method is called, used to access the database.
     * @return True if the email already exists, false otherwise.
     */
    public static boolean isExistentEmail(String inputEmail, Context ctx) {
        boolean isExistent = false;
        DataBaseHelper dbHelper = new DataBaseHelper(ctx);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            db.beginTransaction();
            Cursor cursor = db.rawQuery("SELECT * FROM credential WHERE email = ?", new String[]{inputEmail});
            isExistent = cursor.getCount() > 0;
            cursor.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("UserDAO", "Error checking email existence: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
        return isExistent;
    }

    /**
     * Checks if the entered username already exists in the database.
     *
     * @param inputUsername The username to check for existence.
     * @param ctx The context from which the method is called, used to access the database.
     * @return True if the username already exists, false otherwise.
     */
    public static boolean isExistentUsername(String inputUsername, Context ctx) {
        boolean isExistent = false;
        DataBaseHelper dbHelper = new DataBaseHelper(ctx);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            db.beginTransaction();
            Cursor cursor = db.rawQuery("SELECT * FROM credential WHERE username = ?", new String[]{inputUsername});
            isExistent = cursor.getCount() > 0;
            cursor.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("UserDAO", "Error checking username existence: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
        return isExistent;
    }

    /**
     * Updates an existing user's information in the database.
     * Matches users by their unique ID and replaces the old user object with the updated information.
     *
     * @param updatedUser The {@link User} object containing the updated information.
     * @param ctx The context from which the method is called, used to access the database.
     */
    public static void updateUser(User updatedUser, Context ctx) {
        Log.i("dbtest", "-UPDATE- id: " + updatedUser.getId());
        DataBaseHelper dbHelper = new DataBaseHelper(ctx);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL("UPDATE CREDENTIAL SET username = ?, email = ?, password = ? " +
                            "WHERE user_id = ?",
                    new String[]{updatedUser.getUsername(), updatedUser.getEmail(),
                            updatedUser.getPassword(), updatedUser.getId()});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("UserDAO", "Error updating user: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
        logDBContent(ctx);
    }

    /**
     * Logs the contents of the user database to the console for debugging purposes.
     *
     * @param ctx The context from which the method is called, used to access the database.
     */
    public static void logDBContent(Context ctx) {
        DataBaseHelper dbHelper = new DataBaseHelper(ctx);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try {
            db.beginTransaction();
            Cursor cursor = db.rawQuery("SELECT * FROM credential", null);
            while (cursor.moveToNext()) {
                Log.i("dbtest", "id: " + cursor.getString(0) + " username: "
                        + cursor.getString(1) + " email: " + cursor.getString(2) +
                        " password: " + cursor.getString(3));
                // -Logging password only for educational purposes-
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("UserDAO", "Error logging database content: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
    }
}