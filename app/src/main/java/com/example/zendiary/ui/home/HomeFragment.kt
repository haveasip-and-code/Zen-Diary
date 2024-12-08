package com.example.zendiary.ui.home

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zendiary.databinding.FragmentHomeBinding
import com.example.zendiary.ui.journal.JournalFragment
import com.google.firebase.database.*
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseDatabase
    private val notes = mutableListOf<Note>()
    private lateinit var adapter: NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance()

        // Initialize RecyclerView Adapter
//        adapter = NotesAdapter(notes) { note ->
//            val journalFragment = JournalFragment().apply {
//                arguments = Bundle().apply {
//                    putParcelable("note", note) // Ensure `note` implements Parcelable
//                }
//            }
//
//            requireActivity().supportFragmentManager.beginTransaction()
//                .replace(R.id.nav_host_fragment_activity_main, journalFragment)
//                .addToBackStack(null) // Optional: to allow back navigation
//                .commit()
//        }
        binding.recyclerViewNotes.adapter = adapter


        // Set GridLayoutManager with 2 columns
        binding.recyclerViewNotes.layoutManager = GridLayoutManager(requireContext(), 2)


//        // Add padding at the bottom of the RecyclerView (optional)
//        binding.recyclerViewNotes.addItemDecoration(object : RecyclerView.ItemDecoration() {
//            override fun getItemOffsets(
//                outRect: Rect,
//                view: View,
//                parent: RecyclerView,
//                state: RecyclerView.State
//            ) {
//                val position = parent.getChildAdapterPosition(view)
//                val itemCount = state.itemCount
//                if (position == itemCount - 1) {
//                    outRect.bottom = resources.getDimensionPixelSize(R.dimen.recycler_view_bottom_padding)
//                }
//            }
//        })

        // Load data from Firebase
        loadNotesFromFirebase("userId_12345") // Replace "userId" with the actual user ID

        return root
    }

    private fun loadNotesFromFirebase(userId: String) {
        val entriesRef = database.getReference("users/$userId/entries")
        entriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notes.clear() // Clear the current list to avoid duplication
                for (entrySnapshot in snapshot.children) {
                    // Get the entry ID (the key of the current entry in the database)
                    val entryId = entrySnapshot.key ?: "Unknown ID"

                    // Get other details of the note
                    val header = entrySnapshot.child("header").getValue(String::class.java) ?: "Header"
                    val previewText = entrySnapshot.child("text").getValue(String::class.java) ?: "No text available"
                    val date = entrySnapshot.child("date").getValue(String::class.java) ?: "Unknown Date"
                    val imageUrl = entrySnapshot.child("imageUrl").getValue(String::class.java)

                    // Add the fetched note to the list, now including the entryId
                    notes.add(Note(header, previewText, date, imageUrl, entryId))
                }
                adapter.notifyDataSetChanged() // Notify the adapter about data changes
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error (e.g., show a toast message)
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}


