package com.team12.fruitwatch.ui.main.fragments.search

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.replace
import androidx.recyclerview.widget.RecyclerView
import com.team12.fruitwatch.R
import com.team12.fruitwatch.controllers.NetworkRequestController
import com.team12.fruitwatch.database.entities.PastSearch
import com.team12.fruitwatch.ui.main.MainActivity
import com.team12.fruitwatch.ui.main.fragments.FragmentDataLink
import com.team12.fruitwatch.ui.main.fragments.results.ResultsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.ZoneOffset


class PastSearchRecyclerListAdapter(private val pastSearchList: List<PastSearchItemModel>, private  val activity: Activity) : RecyclerView.Adapter<PastSearchRecyclerListAdapter.ViewHolder>() {

    class ViewHolder(PastSearch: View) : RecyclerView.ViewHolder(PastSearch) {
        val itemName: TextView = itemView.findViewById(R.id.card_past_search_item_name)
        val itemDateSearched: TextView = itemView.findViewById(R.id.card_past_search_date_searched)
        val itemImage: ImageView = itemView.findViewById(R.id.card_past_search_image_searched)
        val rootLayout: View = itemView.findViewById(R.id.card_past_search_root_layout)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_past_search, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pastSearch: PastSearchItemModel = pastSearchList[position!!]
        holder.itemName.text = pastSearch.itemName
        holder.itemDateSearched.text = getDaysBetweenDate(pastSearch.itemSearchDate!!)
        holder.itemImage.setImageBitmap(pastSearch.itemImage)
        holder.rootLayout.setOnClickListener() {
            val confirm = AlertDialog.Builder(holder.itemView.context!!)
                .setTitle("Start Search")
                .setMessage("Are you sure you want to search for this item again?")
                .setPositiveButton("Yes, Search", DialogInterface.OnClickListener { dialog: DialogInterface, i: Int ->
                    CoroutineScope(Dispatchers.Main).launch {
                        searchForItem(pastSearch)
                    }
                }
                ).setNegativeButton("No", null).create()
            confirm.show()
        }
    }

    private fun searchForItem(pastSearch: PastSearchItemModel){
        GlobalScope.launch(Dispatchers.Main) {
            //val fruitName = result.name
            (activity as FragmentDataLink).startTextSearch(pastSearch)
        }
    }

    /** Create a File for saving an image or video */
    private fun getOutputImageFile(context: Context): File? {
        val directory: File = context.getDir("search_images", Context.MODE_PRIVATE)
        return File(directory, "image.png")
    }

    private fun convertBitmapToFile(bitmap: Bitmap,context: Context): File{
        //create a file to write bitmap data
        val file = getOutputImageFile(context)
        val bos : ByteArrayOutputStream = ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos)
        val bitmapdata : ByteArray  = bos.toByteArray()

        //write the bytes in file
        val fos : FileOutputStream = FileOutputStream(file)
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return file!!
    }

    private fun getDaysBetweenDate(dateOne : LocalDateTime): String{
        // Finding the absolute difference between
        // the current date and date given (in seconds)
        val mDifference = kotlin.math.abs(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - dateOne.toEpochSecond(ZoneOffset.UTC))

        // Converting seconds to dates
        val mDifferenceDates = mDifference / (24 * 60 * 60)

        // Converting the above integer to string
        return "$mDifferenceDates Days Ago "
    }

    override fun getItemCount(): Int {
        return pastSearchList.size
    }
}