package com.team12.fruitwatch.database

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteException
import android.util.Log
import com.team12.fruitwatch.database.entitymanager.*
import java.io.File

class DatabaseHelper (context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val TABLES = arrayOf<String>(PastSearchDb.tableName)
    
    private val CREATE_COURSES_TABLE = "CREATE TABLE `" + PastSearchDb.tableName + "` (" +
            "`" + AbstractDb.COL_ID + "` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`" + PastSearchDb.COL_ITEM_NAME + "` TEXT NOT NULL," +
            "`" + PastSearchDb.COL_SEARCH_DATE + "` DATETIME NOT NULL," +
            "`" + PastSearchDb.COL_ITEM_IMAGE + "` BLOB NOT NULL);"


    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "Running onCreate Method")
        try {
            db.execSQL(CREATE_COURSES_TABLE)
            Log.d(TAG, "PastSearch Table Created")
            Log.d(TAG, "Local Database Build Successful.")
        } catch (scoe: SQLiteCantOpenDatabaseException) {
            Log.d(TAG, "Local Database Couldn't be Opened or Created.")
            scoe.printStackTrace()
        } catch (se: SQLiteException) {
            Log.e(TAG, "Local Database Creation Failed.")
            se.printStackTrace()
        }
        Log.w(DatabaseHelper::class.java.name, "Database Created")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w(
            DatabaseHelper::class.java.name,
            "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data"
        )
        db.execSQL("DROP TABLE " + PastSearchDb.tableName + ";", null)
        Log.d(TAG, "PastSearch Table Dropped.")

        onCreate(db)
    }

    fun getDatabaseFile(context: Context): File {
        return context.getDatabasePath(DATABASE_NAME)
    }

    fun getDatabasePath(context: Context): String {
        return context.getDatabasePath(DATABASE_NAME).path
    }

    fun getDatabasePathWithoutFileNameAndExt(context: Context): String {
        return context.getDatabasePath(DATABASE_NAME).path.replace(DATABASE_NAME, "")
    }

    fun getDatabaseAbsolutePath(context: Context): String {
        return context.getDatabasePath(DATABASE_NAME).absolutePath
    }

    companion object {
        const val DATABASE_NAME = "FruitWatchAppDB.sqlite"
        private const val DATABASE_VERSION = 1
        private const val TAG = "DatabaseHelper"
        const val DEBUG_DATABASE = true
    }
}