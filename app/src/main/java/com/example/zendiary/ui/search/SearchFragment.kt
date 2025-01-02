package com.example.zendiary.ui.search

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zendiary.databinding.FragmentSearchBinding
import com.example.zendiary.ui.analytics.adapters.DayPreviewAdapter

class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels()
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!


    private lateinit var searchResultAdapter: DayPreviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout and set up binding
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        setupSearchResultRecyclerView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the back button click listener using binding
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Set up the search button click listener
        binding.btnSeach.setOnClickListener {
            val query = binding.etSearch.text.toString() // Get text from EditText
            if (query.isNotEmpty()) {
                viewModel.searchEntries(query) // Trigger search in ViewModel
            }
        }

        setupSearchResultRecyclerView()

        observeViewModel()
    }

    private fun setupSearchResultRecyclerView() {
        searchResultAdapter = DayPreviewAdapter()
        binding.rvSearchResults.apply {
            adapter = searchResultAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        // Observe recommendations list
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchResultAdapter.submitList(results)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leaks by setting the binding to null
        _binding = null
    }
}
