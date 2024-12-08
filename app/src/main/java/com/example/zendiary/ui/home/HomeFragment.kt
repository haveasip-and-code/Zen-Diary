package com.example.zendiary.ui.home

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zendiary.R
import com.example.zendiary.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Sample data for the RecyclerView
        val notes = listOf(
            Note(
                header = "Work Notes",
                previewText = "This is a preview of note 1.",
                date = "Sunday, Jan 21",
                imageUrl = null
            ),
            Note(
                header = "Personal Notes",
                previewText = "This is a preview of note 2.",
                date = "Monday, Jan 22",
                imageUrl = "https://example.com/note_image_2.jpg"
            ),
            Note(
                header = "Shopping List",
                previewText = "This is a preview of note 3.",
                date = "Tuesday, Jan 23",
                imageUrl = null
            ),
            Note(
                header = "Shopping List",
                previewText = "This is a preview of note 3.",
                date = "Tuesday, Jan 23",
                imageUrl = null
            ),
            Note(
                header = "Shopping List",
                previewText = "This is a preview of note 3.",
                date = "Tuesday, Jan 23",
                imageUrl = null
            ),
            Note(
                header = "Shopping List",
                previewText = "This is a preview of note 3.",
                date = "Tuesday, Jan 23",
                imageUrl = null
            )
        )


        // Set up RecyclerView
        val recyclerView = binding.recyclerViewNotes
        val adapter = NotesAdapter(notes)
        recyclerView.adapter = adapter

        // Set GridLayoutManager with 2 columns
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Add padding at the bottom of the RecyclerView
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                val itemCount = state.itemCount

                // Add bottom padding to the last item
                if (position == itemCount - 1) {
                    outRect.bottom = resources.getDimension(R.dimen.recycler_view_bottom_padding).toInt()
                }
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}