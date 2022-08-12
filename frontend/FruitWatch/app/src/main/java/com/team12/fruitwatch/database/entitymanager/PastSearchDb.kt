package com.team12.fruitwatch.database.entitymanager

import android.content.Context
import com.team12.fruitwatch.database.AbstractDb
import com.team12.fruitwatch.database.entities.PastSearch
import com.team12.fruitwatch.ui.main.fragments.search.PastSearchItemModel
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

                val course = PastSearch(
                    allPastSearches.getLong(allPastSearches.getColumnIndexOrThrow("id")),
                    allPastSearches.getString(allPastSearches.getColumnIndexOrThrow(COL_ITEM_NAME)),
                    LocalDateTime.parse( allPastSearches.getString(allPastSearches.getColumnIndexOrThrow(COL_SEARCH_DATE)),DateTimeFormatter.ISO_DATE),
                    allPastSearches.getBlob(allPastSearches.getColumnIndexOrThrow(COL_ITEM_IMAGE)),
                )
                coursesHashMap.set(allPastSearches.getLong(allPastSearches.getColumnIndexOrThrow("id")),course)
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
                
                val course = PastSearch(
                    allPastSearches.getLong(allPastSearches.getColumnIndexOrThrow("id")),
                    allPastSearches.getString(allPastSearches.getColumnIndexOrThrow(COL_ITEM_NAME)),
                    LocalDateTime.parse( allPastSearches.getString(allPastSearches.getColumnIndexOrThrow(COL_SEARCH_DATE)),DateTimeFormatter.ISO_DATE),
                    allPastSearches.getBlob(allPastSearches.getColumnIndexOrThrow(COL_ITEM_IMAGE))
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
                coursesArrayList.add(allPastSearches.getString(allPastSearches.getColumnIndexOrThrow(COL_ITEM_NAME)))
                allPastSearches.moveToNext()
            }
            allPastSearches.close()
        }
        close()
        return coursesArrayList
    }

    // Provides saved past searches for a Listview or Recycleview
    fun getPastSearchItemModelList():ArrayList<PastSearch>{
        open()
        val allPastSearches = all()
        val pastSearchArrayList = ArrayList<PastSearch>()
        if(allPastSearches.count > 0){
            allPastSearches.moveToFirst()
            while (!allPastSearches.isAfterLast){
                
                val pastSearch = PastSearch(
                    allPastSearches.getLong(allPastSearches.getColumnIndexOrThrow("id")),
                    allPastSearches.getString(allPastSearches.getColumnIndexOrThrow(COL_ITEM_NAME)),
                    LocalDateTime.parse( allPastSearches.getString(allPastSearches.getColumnIndexOrThrow(COL_SEARCH_DATE)),DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    allPastSearches.getBlob(allPastSearches.getColumnIndexOrThrow(COL_ITEM_IMAGE))
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
            val itemImage = pastSearch.getBlob(pastSearch.getColumnIndexOrThrow(COL_ITEM_IMAGE))
            val p = PastSearch(
                pastSearch.getLong(pastSearch.getColumnIndexOrThrow("id")),
                pastSearch.getString(pastSearch.getColumnIndexOrThrow(COL_ITEM_NAME)),
                LocalDateTime.parse( pastSearch.getString(pastSearch.getColumnIndexOrThrow(COL_SEARCH_DATE)),DateTimeFormatter.ISO_DATE),
                pastSearch.getBlob(pastSearch.getColumnIndexOrThrow(COL_ITEM_IMAGE))
            )
            pastSearch.close()
            close()

            return p

            }
        close()
        return null
    }

    fun createPastSearchEntry(pastSearch: PastSearch):Long{
        open()
        val values  = HashMap<String, Any>()
        values.put(COL_ITEM_NAME,pastSearch.itemName!!)
        values.put(COL_SEARCH_DATE,pastSearch.itemSearchDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        values.put(COL_ITEM_IMAGE,pastSearch.itemImage!!)
        val result = create(values)
        close()
        return result
    }

    fun deleteAllPastSearches(): Boolean{
        try{
        open()
        deleteAll()
        close()
            return true
        }catch (e : Exception){
            return false
        }
    }
}