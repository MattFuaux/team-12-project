package com.team12.fruitwatch.ui.main.fragments.search

import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.team12.fruitwatch.R
import com.team12.fruitwatch.database.entitymanager.PastSearchDb
import com.team12.fruitwatch.databinding.FragmentSearchBinding
import com.team12.fruitwatch.ui.main.fragments.FragmentDataLink
import kotlin.math.roundToInt

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private lateinit var swipeHelper: ItemTouchHelper
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        val deleteIcon = resources.getDrawable(R.drawable.ic_baseline_delete_forever_24, null)
        val pastSearchList: RecyclerView = binding.fragSearchPastSearchesList
        pastSearchList.layoutManager = LinearLayoutManager(context)
        val searchList = PastSearchDb(context).getPastSearchItemModelList()
        val adapter = PastSearchRecyclerListAdapter(searchList, requireActivity())
        pastSearchList.adapter = adapter

        swipeHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.RIGHT
        ) {
            //more code here
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
              return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.RIGHT) {
                    val pos = viewHolder.adapterPosition
                    if(PastSearchDb(context).deletePastSearch(searchList[pos].id!!)){
                        searchList.removeAt(pos)
                        adapter.notifyItemRemoved(pos)
                        Snackbar.make(
                            view!!.findViewById(R.id.card_past_search_root_layout),
                            "Deleted",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

                when {
                    (dX > width / 6) -> {
                        //1. Background color for current item being swiped
                        val viewItemRect = Rect(
                            viewHolder.itemView.left,
                            viewHolder.itemView.top,
                            viewHolder.itemView.right,
                            viewHolder.itemView.bottom
                        )
                        val itemRectPaint = Paint()
                        itemRectPaint.color = resources.getColor(android.R.color.holo_red_light,null)
                        c.drawRect(viewItemRect,itemRectPaint)

                        //2. Printing the icons and text
                        val textMargin = resources.getDimension(R.dimen.margin_lg)
                            .roundToInt()
                        val iconLeft = textMargin
                        deleteIcon.bounds = Rect(
                            textMargin,
                             viewHolder.itemView.top + 30,
                            textMargin + deleteIcon.intrinsicWidth,
                            viewHolder.itemView.top + deleteIcon.intrinsicHeight
                                    + (textMargin*2)
                        )
                        //3. Drawing icon and text
                       deleteIcon.draw(c)
                        val paint = Paint()
                        paint.textSize = resources.getDimension(R.dimen.font_size_md)
                        paint.color = resources.getColor(R.color.off_white,null)
                        c.drawText("Swipe to",textMargin.toFloat(),viewHolder.itemView.top+150f,paint)
                        c.drawText("delete",textMargin.toFloat(),viewHolder.itemView.top+200f,paint)
                    }
                    else -> {
                        //c.drawColor(Color.TRANSPARENT)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        swipeHelper.attachToRecyclerView(pastSearchList)

        val clearSearchHistoryButton = root.findViewById<Button>(R.id.frag_search_clear_history_btn)
        clearSearchHistoryButton.setOnClickListener {
            val noRecentSearch = AlertDialog.Builder(requireContext(), androidx.appcompat.R.style.Theme_AppCompat_Dialog)
                .setTitle("Clear Entire History")
                .setMessage("Are you sure you want to clear your entire search history?\nThis cannot be undone")
                .setPositiveButton("Yes, Continue", DialogInterface.OnClickListener { dialog: DialogInterface, i: Int ->
                    if(PastSearchDb(requireContext()).deleteAllPastSearches()){
                        searchList.clear()
                        adapter.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "Search History Deleted", Toast.LENGTH_LONG).show()
                    }
                }
                ).setNegativeButton("No", null).create()
            noRecentSearch.show()
        }

        val startSearchButton = root.findViewById<Button>(R.id.frag_search_take_pic_btn)
        startSearchButton.setOnClickListener {
            (activity as FragmentDataLink).openCamera()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}