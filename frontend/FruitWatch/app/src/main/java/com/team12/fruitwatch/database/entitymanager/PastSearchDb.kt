package com.team12.fruitwatch.database.entitymanager

import android.content.Context
import android.util.Log
import com.team12.fruitwatch.database.AbstractDb
import com.team12.fruitwatch.database.entities.PastSearch
import com.team12.fruitwatch.ui.main.fragments.search.PastSearchItemModel
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//This class is used to interact with the PastSearches table in the Fruit Watch database.
// This class provides all the Past Search information in whaterver form/object is needed throughout Fruit Watch
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
        val pastSearchesHashMap = HashMap<Long,PastSearch>()
        if(allPastSearches.count > 0){
            allPastSearches.moveToFirst()
            while (!allPastSearches.isAfterLast){

                val pastSearch = PastSearch(
                    allPastSearches.getLong(allPastSearches.getColumnIndexOrThrow("id")),
                    allPastSearches.getString(allPastSearches.getColumnIndexOrThrow(COL_ITEM_NAME)),
                    LocalDateTime.parse( allPastSearches.getString(allPastSearches.getColumnIndexOrThrow(COL_SEARCH_DATE)),DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    allPastSearches.getBlob(allPastSearches.getColumnIndexOrThrow(COL_ITEM_IMAGE)),
                )
                pastSearchesHashMap.set(allPastSearches.getLong(allPastSearches.getColumnIndexOrThrow("id")),pastSearch)
                allPastSearches.moveToNext()
            }
            allPastSearches.close()
        }
        close()
        return pastSearchesHashMap
    }

    fun getPastSearchesList():ArrayList<PastSearch>{
        open()
        val allPastSearches = all()
        val pastSearchesArrayList = ArrayList<PastSearch>()
        if(allPastSearches.count > 0){
            allPastSearches.moveToFirst()
            while (!allPastSearches.isAfterLast){
                
                val pastSearch = PastSearch(
                    allPastSearches.getLong(allPastSearches.getColumnIndexOrThrow("id")),
                    allPastSearches.getString(allPastSearches.getColumnIndexOrThrow(COL_ITEM_NAME)),
                    LocalDateTime.parse( allPastSearches.getString(allPastSearches.getColumnIndexOrThrow(COL_SEARCH_DATE)),DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    allPastSearches.getBlob(allPastSearches.getColumnIndexOrThrow(COL_ITEM_IMAGE))
                )
                pastSearchesArrayList.add(pastSearch)
                allPastSearches.moveToNext()
            }
            allPastSearches.close()
        }
        close()
        return pastSearchesArrayList
    }

    fun getPastSearchNameList():ArrayList<String>{
        open()
        val allPastSearches = all()
        val pastSearchesArrayList = ArrayList<String>()
        if(allPastSearches.count > 0){
            allPastSearches.moveToFirst()
            while (!allPastSearches.isAfterLast){
                pastSearchesArrayList.add(allPastSearches.getString(allPastSearches.getColumnIndexOrThrow(COL_ITEM_NAME)))
                allPastSearches.moveToNext()
            }
            allPastSearches.close()
        }
        close()
        return pastSearchesArrayList
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
            val p = PastSearch(
                pastSearch.getLong(pastSearch.getColumnIndexOrThrow("id")),
                pastSearch.getString(pastSearch.getColumnIndexOrThrow(COL_ITEM_NAME)),
                LocalDateTime.parse( pastSearch.getString(pastSearch.getColumnIndexOrThrow(COL_SEARCH_DATE)),DateTimeFormatter.ISO_LOCAL_DATE_TIME),
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

    fun determineIfPastSearchLimitIsReached(): Boolean{
        open()
        val allPastSearches = all()
        val count = allPastSearches.count
        Log.d(TAG,"Past Search Limit Count is: $count")
        var result :Boolean = false
        if(count > context!!.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE).getString("past_search_list_length","20").toString().toInt() ) {
            allPastSearches.moveToFirst()
            val oldestPastSearchId =allPastSearches.getLong(allPastSearches.getColumnIndexOrThrow("id"))
            allPastSearches.close()
            result = delete(oldestPastSearchId) == 1
        }
        close()
        return result
    }

    fun deletePastSearch(id: Long): Boolean{
        open()
        val result = delete(id)
        close()
        return (result == 1)
    }

    fun checkIfSearchIsNew(name: String): Boolean {
        open()
        val search = find("$COL_ITEM_NAME = ?",name)
        close()
        return (search == null || search.count <= 0 )
    }

    fun getPastSearchByItemName(name: String): PastSearch? {
        open()
        val pastSearch = find("$COL_ITEM_NAME = ?",name)

        if(pastSearch != null){
            pastSearch.moveToFirst()
            val p = PastSearch(
                pastSearch.getLong(pastSearch.getColumnIndexOrThrow("id")),
                pastSearch.getString(pastSearch.getColumnIndexOrThrow(COL_ITEM_NAME)),
                LocalDateTime.parse( pastSearch.getString(pastSearch.getColumnIndexOrThrow(COL_SEARCH_DATE)),DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                pastSearch.getBlob(pastSearch.getColumnIndexOrThrow(COL_ITEM_IMAGE))
            )
            pastSearch.close()
            close()
            return p
        }
        close()
        return null
    }
}