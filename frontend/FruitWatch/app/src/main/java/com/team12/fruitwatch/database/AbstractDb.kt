package com.team12.fruitwatch.database


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.util.HashMap

/**
 * This class provides basic functionality to use a database table.
 *
 * @author Andreas Bender
 */
abstract class AbstractDb(context: Context?) {
    protected var databaseHelper: DatabaseHelper
    protected var database: SQLiteDatabase? = null
    //protected var mSnapshotManager: SnapshotManager
    fun open() {
        if (database == null || !database!!.isOpen()) {
            database = databaseHelper.getWritableDatabase()
            //mSnapshotManager.setDbFile(File(database!!.getPath()))
        }
    }

    fun close() {
        if (database != null && database!!.isOpen()) databaseHelper.close()
    }

    /**
     * Create a new entry.
     *
     * @param values A HashMap containing Column - Value pairs.
     * new HashMap<String></String>, Object>().put(COL_IP4, "127.0.0.1");
     * @return The ID of the newly created entry or -1 if an error occured.
     */
    fun create(values: HashMap<String, *>): Long {

        val storeValues = ContentValues()
        for ((column, value) in values) {
            if (isStringColumn(column)) {
                storeValues.put(column, value as String)
            } else if (isIntegerColumn(column)) {
                storeValues.put(column, value as Int)
            } else if (isLongColumn(column)) {
                storeValues.put(column, value as Long)
            } else if (isFloatColumn(column)) {
                storeValues.put(column, value as Float)
            } else if (isBlobColumn(column)) {
                storeValues.put(column, value as ByteArray)
            } else {
                Log.w(
                    TAG, tableName +
                            "- create: Trying to insert an unknown column type! " +
                            "(Column: " + column + ")"
                )
            }
        }
        return database!!.insert(tableName, null, storeValues)
    }

    fun delete(id: Long): Int {
        return database!!.delete(tableName, COL_ID + " = " + id, null)
    }

    fun deleteAll(): Int {
        return database!!.delete(tableName, null, null)
    }

    fun find(columnsToSearch: String?, searchArgs: Array<String?>?): Cursor? {
        val cursor: Cursor = database!!.query(
            tableName, columns,
            columnsToSearch, searchArgs, null, null, null, null
        )
        if (cursor != null) {
            cursor.moveToFirst()
        }
        return cursor
    }

    fun find(columnsToSearch: String?, searchArg: String?): Cursor? {
        val cursor: Cursor = database!!.query(
            tableName, columns,
            columnsToSearch, arrayOf(searchArg), null, null, null, null
        )
        if (cursor != null) {
            cursor.moveToFirst()
        }
        return cursor
    }

    fun getEntryCount(id: Long): Long {
        val cursor = get(id)
        return cursor!!.count.toLong()
    }

    /**
     * Update an existing entry.
     *
     * @param id     The id identifying the entry.
     * @param values A HashMap containing Column - Value pairs.
     * new HashMap<String></String>, Object>().put(COL_IP4, "127.0.0.1");
     * @return True if any row has been changed. False otherwise.
     */
    fun update(id: Long, values: HashMap<String, *>): Boolean {
        val storeValues = ContentValues()
        for ((column, value) in values) {
            if (isStringColumn(column)) {
                storeValues.put(column, value as String)
            } else if (isIntegerColumn(column)) {
                storeValues.put(column, value as Int)
            } else if (isLongColumn(column)) {
                storeValues.put(column, value as Long)
            } else if (isFloatColumn(column)) {
                storeValues.put(column, value as Float)
            } else if (isBlobColumn(column)) {
                storeValues.put(column, value as ByteArray)
            } else {
                Log.w(
                    TAG, tableName +
                            "- update: Trying to update an unknown column type! " +
                            "(Column: " + column + ")"
                )
            }
        }
        return database!!.update(tableName, storeValues, COL_ID + " = " + id, null) > 0
    }

    /**
     * Find an item based on its ID.
     *
     * @param id The ID of the required item.
     * @return A Cursor pointing to the required item or null if it's not present.
     */
    operator fun get(id: Long): Cursor? {
        val cursor: Cursor = database!!.query(
            true,
            tableName,
            columns,
            COL_ID + " =  ? ",
            arrayOf(id.toString()),
            null,
            null,
            null,
            null
        )
        if (cursor != null) {
            cursor.moveToFirst()
        }
        return cursor
    }

    /**
     * Find all items of the table.
     *
     * @return A Cursor pointing to the first item of the table.
     */
    fun all(): Cursor {
        return database!!.query(tableName, columns, null, null, null, null, null)
    }

    val isEmpty: Boolean
        get() = all().count == 0

    /**
     * @return All columns of the table, including the ID-column.
     */
    protected abstract val columns: Array<String>

    /**
     * @return The name of the table.
     */
    protected abstract val tableName: String

    /**
     * @return The log tag used by LogCat, something like "MY_APP - DB"
     */
    protected abstract val TAG: String

    /**
     * @param c The column which needs to be evaluated.
     * @return True if the column stores Integers, false otherwise.
     */
    protected abstract fun isLongColumn(c: String): Boolean

    /**
     * @param c The column which needs to be evaluated.
     * @return True if the column stores Strings, false otherwise.
     */
    protected abstract fun isStringColumn(c: String): Boolean

    /**
     * @param c The column which needs to be evaluated.
     * @return True if the column stores Integers, false otherwise.
     */
    protected abstract fun isIntegerColumn(c: String): Boolean

    /**
     * @param c The column which needs to be evaluated.
     * @return True if the column stores Integers, false otherwise.
     */
    protected abstract fun isFloatColumn(c: String): Boolean

    /**
     * @param c The column which needs to be evaluated.
     * @return True if the column stores Blobs, false otherwise.
     */
    protected abstract fun isBlobColumn(c: String?): Boolean

    companion object {
        const val COL_ID = "id"
        protected var DEBUG_DATABASE = false
    }

    init {
        databaseHelper = DatabaseHelper(context)
        //mSnapshotManager = SnapshotManager.getInstance()!!
    }
}