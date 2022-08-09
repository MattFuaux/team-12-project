package com.team12.fruitwatch.ui.main.fragments.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.team12.fruitwatch.R
import com.team12.fruitwatch.database.entitymanager.PastSearchDb
import com.team12.fruitwatch.databinding.FragmentSearchBinding
import com.team12.fruitwatch.ui.main.fragments.FragmentDataLink

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val searchViewModel =
            ViewModelProvider(this).get(SearchViewModel::class.java)

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val pastSearchList: RecyclerView = binding.fragSearchPastSearchesList
        pastSearchList.layoutManager = LinearLayoutManager(context)
        pastSearchList.adapter = PastSearchRecyclerListAdapter(PastSearchDb(context).getPastSearchItemModelList(), requireActivity())

        val startSearchButton = root.findViewById<Button>(R.id.frag_search_take_pic_btn)
        startSearchButton.setOnClickListener(){
            (activity as FragmentDataLink).openCamera()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}