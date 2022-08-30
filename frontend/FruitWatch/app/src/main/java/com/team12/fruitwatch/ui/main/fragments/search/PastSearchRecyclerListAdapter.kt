package com.team12.fruitwatch.ui.main.fragments.search

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.team12.fruitwatch.R
import com.team12.fruitwatch.database.entities.PastSearch
import com.team12.fruitwatch.ui.animation.LoadingAnimationController
import com.team12.fruitwatch.ui.main.fragments.FragmentDataLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset


// Maps PastSearch objects to the list of past searches on the SearchFragment
class PastSearchRecyclerListAdapter(private val pastSearchList: List<PastSearch>, private val activity: Activity) : RecyclerView.Adapter<PastSearchRecyclerListAdapter.ViewHolder>() {

    class ViewHolder(PastSearch: View) : RecyclerView.ViewHolder(PastSearch) {
        val itemName: TextView = itemView.findViewById(R.id.card_past_search_item_name)
        val itemDateSearched: TextView = itemView.findViewById(R.id.card_past_search_date_searched)
        val itemImage: ImageView = itemView.findViewById(R.id.card_past_search_image_searched)
        val rootLayout: View = itemView.findViewById(R.id.card_past_search_root_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_past_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pastSearch: PastSearch = pastSearchList[position]
        holder.itemName.text = pastSearch.itemName
        holder.itemDateSearched.text = getDaysBetweenDate(pastSearch.itemSearchDate!!)
        holder.itemImage.setImageBitmap(BitmapFactory.decodeByteArray(pastSearch.itemImage,0,pastSearch.itemImage!!.size))
        holder.rootLayout.setOnClickListener {
            val confirm = AlertDialog.Builder(holder.itemView.context!!,R.style.Theme_FruitWatch_Dialog)
                .setTitle("Start Search")
                .setMessage("Are you sure you want to search for this item again?")
                .setPositiveButton("Yes, Search", DialogInterface.OnClickListener { dialog: DialogInterface, i: Int ->
                    searchForItem(pastSearch)
                }
                ).setNegativeButton("No", null).create()
            confirm.show()
        }
    }

    private fun searchForItem(pastSearch: PastSearch){
        (activity as LoadingAnimationController).onStartLoading()
        (activity as FragmentDataLink).startTextSearch(pastSearch)
    }

    // Calculates the days since last searched
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