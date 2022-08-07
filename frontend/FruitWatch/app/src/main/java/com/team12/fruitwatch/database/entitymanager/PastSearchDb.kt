package com.team12.fruitwatch.database.entitymanager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.team12.fruitwatch.database.AbstractDb
import com.team12.fruitwatch.database.entities.PastSearch
import com.team12.fruitwatch.ui.main.fragments.search.PastSearchItemModel
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


//import com.team12.fruitwatch.ui.courses.PastSearchItemModel

class PastSearchDb(val context: Context?) : AbstractDb(context) {

    companion object {
        const val COL_ITEM_NAME = "item_name"
        const val COL_ITEM_IMAGE = "item_image"
        const val COL_SEARCH_DATE = "search_date"
        const val TAG = "PastSearchDb"
        const val tableName = "past_searches"
    }

    override val tableName: String
        get() = Companion.tableName
    override val TAG: String
        get() = Companion.TAG

    override val columns: Array<String>
         get() = arrayOf(
            AbstractDb.Companion.COL_ID, COL_ITEM_NAME, COL_ITEM_IMAGE, COL_SEARCH_DATE
        )

     override fun isStringColumn(c: String): Boolean {
        return c === COL_ITEM_NAME || c === COL_SEARCH_DATE
    }

     override fun isIntegerColumn(c: String): Boolean {
        return false
    }

     override fun isFloatColumn(c: String): Boolean {
        return false
    }

     override fun isLongColumn(c: String): Boolean {
        return c === AbstractDb.Companion.COL_ID
    }

     override fun isBlobColumn(c: String?): Boolean {
        return c === COL_ITEM_IMAGE
    }



    fun getAllPastSearchesHashMap():HashMap<Long,PastSearch>{
        open()
        val allPastSearches = all()
        val coursesHashMap = HashMap<Long,PastSearch>()
        if(allPastSearches.count > 0){
            allPastSearches.moveToFirst()
            while (!allPastSearches.isAfterLast){
                val itemImage = allPastSearches.getBlob(allPastSearches.getColumnIndex(COL_ITEM_IMAGE))
                val course = PastSearch(
                    allPastSearches.getLong(allPastSearches.getColumnIndex("id")),
                    allPastSearches.getString(allPastSearches.getColumnIndex(COL_ITEM_NAME)),
                    LocalDateTime.parse( allPastSearches.getString(allPastSearches.getColumnIndex(COL_SEARCH_DATE)),DateTimeFormatter.ISO_DATE),
                    BitmapFactory.decodeByteArray(itemImage,0, itemImage.size),
                )
                coursesHashMap.set(allPastSearches.getLong(allPastSearches.getColumnIndex("id")),course)
                allPastSearches.moveToNext()
            }
            allPastSearches.close()
        }
        close()
        return coursesHashMap
    }

    fun getPastSearchesList():ArrayList<PastSearch>{
        open()
        val allPastSearches = all()
        val coursesArrayList = ArrayList<PastSearch>()
        if(allPastSearches.count > 0){
            allPastSearches.moveToFirst()
            while (!allPastSearches.isAfterLast){
                val itemImage = allPastSearches.getBlob(allPastSearches.getColumnIndex(COL_ITEM_IMAGE))
                val course = PastSearch(
                    allPastSearches.getLong(allPastSearches.getColumnIndex("id")),
                    allPastSearches.getString(allPastSearches.getColumnIndex(COL_ITEM_NAME)),
                    LocalDateTime.parse( allPastSearches.getString(allPastSearches.getColumnIndex(COL_SEARCH_DATE)),DateTimeFormatter.ISO_DATE),
                    BitmapFactory.decodeByteArray(itemImage,0, itemImage.size)
                )
                coursesArrayList.add(course)
                allPastSearches.moveToNext()
            }
            allPastSearches.close()
        }
        close()
        return coursesArrayList
    }

    fun getPastSearchNameList():ArrayList<String>{
        open()
        val allPastSearches = all()
        val coursesArrayList = ArrayList<String>()
        if(allPastSearches.count > 0){
            allPastSearches.moveToFirst()
            while (!allPastSearches.isAfterLast()){
                coursesArrayList.add(allPastSearches.getString(allPastSearches.getColumnIndex(COL_ITEM_NAME)))
                allPastSearches.moveToNext()
            }
            allPastSearches.close()
        }
        close()
        return coursesArrayList
    }

    // Provides saved past searches for a Listview or Recycleview
    fun getPastSearchItemModelList():ArrayList<PastSearchItemModel>{
        open()
        val allPastSearches = all()
        val pastSearchArrayList = ArrayList<PastSearchItemModel>()
        if(allPastSearches.count > 0){
            allPastSearches.moveToFirst()
            while (!allPastSearches.isAfterLast){
                val itemImage = allPastSearches.getBlob(allPastSearches.getColumnIndex(COL_ITEM_IMAGE))
                val pastSearch = PastSearchItemModel(
                    allPastSearches.getLong(allPastSearches.getColumnIndex("id")),
                    allPastSearches.getString(allPastSearches.getColumnIndex(COL_ITEM_NAME)),
                    LocalDateTime.parse( allPastSearches.getString(allPastSearches.getColumnIndex(COL_SEARCH_DATE)),DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    BitmapFactory.decodeByteArray(itemImage,0, itemImage.size)
                )
                pastSearchArrayList.add(pastSearch)
                allPastSearches.moveToNext()
            }
            allPastSearches.close()
        }
        close()
        return pastSearchArrayList
    }
    fun getPastSearch(id:Long):PastSearch?{
        open()
        val pastSearch = get(id)

        if(pastSearch != null){
            pastSearch.moveToFirst()
            val itemImage = pastSearch.getBlob(pastSearch.getColumnIndex(COL_ITEM_IMAGE))
            val p = PastSearch(
                pastSearch.getLong(pastSearch.getColumnIndex("id")),
                pastSearch.getString(pastSearch.getColumnIndex(COL_ITEM_NAME)),
                LocalDateTime.parse( pastSearch.getString(pastSearch.getColumnIndex(COL_SEARCH_DATE)),DateTimeFormatter.ISO_DATE),
                BitmapFactory.decodeByteArray(itemImage,0, itemImage.size)
            )
            pastSearch.close()
            close()

            return p

            }
        close()
        return null
    }

    fun createPastSearchEntry(pastSearch: PastSearch):Boolean{
        open()
        val values  = HashMap<String, Any>()
        values.put(COL_ITEM_NAME,pastSearch.itemName!!)
        values.put(COL_SEARCH_DATE,pastSearch.itemSearchDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        val stream = ByteArrayOutputStream()
        pastSearch.itemImage!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray: ByteArray = stream.toByteArray()
        pastSearch.itemImage!!.recycle()
        values.put(COL_ITEM_IMAGE,byteArray)
        val result = create(values)
        close()
        return result != -1L
    }
}