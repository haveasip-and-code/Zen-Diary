package com.example.zendiary.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.zendiary.databinding.FragmentHomeBinding
import com.google.firebase.database.*
import androidx.navigation.fragment.findNavController
import com.example.zendiary.R
import com.example.zendiary.utils.Note
import com.example.zendiary.Global
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseDatabase
    private val notes = mutableListOf<Note>()
    private lateinit var adapter: NotesAdapter
    private var userId = Global.userId

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Global.isNewEntry = false
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance()

        // Initialize RecyclerView Adapter
        adapter = NotesAdapter(
            notes,
            onNoteClicked = { note ->
                val bundle = Bundle().apply {
                    putParcelable("note", note) // Ensure `Note` implements Parcelable
                }
                findNavController().navigate(R.id.journalFragment, bundle)
            },
            onLongClick = { note ->
                showDeleteConfirmationDialog(note) // Show confirmation dialog
            }
        )

        binding.recyclerViewNotes.adapter = adapter

        // Set GridLayoutManager with 2 columns
        binding.recyclerViewNotes.layoutManager = GridLayoutManager(requireContext(), 2)

        // Load data from Firebase
        loadNotesFromFirebase(userId)

        // Set User Name and Date
        loadUserDataAndDate(userId)

        Global.isNewEntry = false

        // Reference the ImageView
        val notificationIcon: ImageView = root.findViewById(R.id.iv_notification)

        // Set click listener
        notificationIcon.setOnClickListener {
            showNotificationDialog()
        }

        // Set search button click listener
        binding.searchBar.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_searchFragment)
        }
        return root
    }

    private fun showNotificationDialog() {
        // Inflate the custom layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_notification, null)

        // Reference the views inside the custom layout
        //val iconImageView: ImageView = dialogView.findViewById(R.id.iv_dialog_icon)
        val titleTextView: TextView = dialogView.findViewById(R.id.tv_dialog_title)
        val messageTextView: TextView = dialogView.findViewById(R.id.tv_dialog_message)
        val viewAllButton: Button = dialogView.findViewById(R.id.btn_dialog_view_all)
        val dismissButton: Button = dialogView.findViewById(R.id.btn_dialog_dismiss)

        // Optional: Customize the content dynamically
        titleTextView.text = "Notification"
        messageTextView.text = """
        Here are your latest updates:
        1. Upcoming meeting at 3 PM.
        2. Upcoming meeting at 9 AM.
        3. 3 Years anniversary
    """.trimIndent()

        // Build the dialog
        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        // Set button click listeners
        viewAllButton.setOnClickListener {
            Toast.makeText(requireContext(), "View All Clicked", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dismissButton.setOnClickListener {
            dialog.dismiss() // Close the dialog
        }

        // Show the dialog
        dialog.show()
    }

    private fun showDeleteConfirmationDialog(note: Note) {
        // Create a confirmation dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this entry?")
            .setPositiveButton("Delete") { _, _ ->
                deleteEntryFromFirebase(note)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteEntryFromFirebase(note: Note) {
        val entryRef = database.getReference("users/$userId/entries/${note.entryId}")
        entryRef.removeValue().addOnSuccessListener {
            // Remove the note locally and notify adapter
            notes.remove(note)
            adapter.notifyDataSetChanged()
        }.addOnFailureListener {
            // Show an error message if deletion fails
            Toast.makeText(requireContext(), "Failed to delete entry", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadNotesFromFirebase(userId: String?) {
        val entriesRef = database.getReference("users/$userId/entries")
        entriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notes.clear() // Clear the current list to avoid duplication
                for (entrySnapshot in snapshot.children) {
                    // Get the entry ID (the key of the current entry in the database)
                    val entryId = entrySnapshot.key ?: "Unknown ID"

                    // Get other details of the note
                    val header = entrySnapshot.child("headerEntry").getValue(String::class.java) ?: "Header"
                    val previewText = entrySnapshot.child("text").getValue(String::class.java) ?: "No text available"
                    val dateTime = entrySnapshot.child("date").getValue(String::class.java) ?: "Unknown Date"
                    val imageUrl = entrySnapshot.child("imageUrl").getValue(String::class.java)

                    // Extract only the date part (e.g., "2024-11-17") from the datetime string
                    val date = dateTime.substringBefore("T") // Safely extracts the part before 'T'

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

    private fun loadUserDataAndDate(userId: String?) {
        val userRef = database.getReference("users/$userId/profile")

        // Fetch user's name from Firebase
        userRef.child("fullname").get().addOnSuccessListener { snapshot ->
            val userName = snapshot.getValue(String::class.java) ?: "User"

            // Split the name into parts and take the last part as the last name
            val lastName = userName.split(" ").last() // Take the last part of the name


            binding.nameofUser.text = "Hi, $lastName"  // Updated ID here
        }

        // Get current date and format it as DD/MM/YYYY
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        binding.textDate.text = currentDate
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
