package com.example.zendiary.ui.profile

import android.annotation.SuppressLint
import com.example.zendiary.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StoreFragment : Fragment() {
    private lateinit var stickersButton: TextView
    private lateinit var pagesButton: TextView

    private var recyclerView: RecyclerView? = null
    private var adapter: StoreAdapter? = null
    private var database: FirebaseDatabase? = null
    private var storeItems: MutableList<StoreItem>? = null




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the store fragment layout
        val root: View = inflater.inflate(R.layout.fragment_store, container, false)

        // Initialize buttons
        stickersButton = root.findViewById<TextView>(R.id.Stickers)
        pagesButton = root.findViewById<TextView>(R.id.Pages)

        // Initialize RecyclerView
        recyclerView = root.findViewById<RecyclerView>(R.id.recycler_view_notes)
        recyclerView!!.setLayoutManager(GridLayoutManager(requireContext(), 2))

        // Initialize store items and adapter
        storeItems = ArrayList<StoreItem>()
        adapter = StoreAdapter(storeItems as ArrayList<StoreItem>)
        recyclerView!!.setAdapter(adapter)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()

        // Set initial background
        stickersButton?.let {
            it.setBackgroundResource(R.drawable.green_cornered_bg)
        }
        pagesButton?.let {
            it.setBackgroundResource(R.drawable.white_cornered_bg)
        }


        // Set click listeners
        stickersButton.setOnClickListener(View.OnClickListener {
            stickersButton.setBackgroundResource(R.drawable.green_cornered_bg)
            pagesButton.setBackgroundResource(R.drawable.white_cornered_bg)
            loadStoreItems("stickers")
        })

        pagesButton.setOnClickListener(View.OnClickListener {
            pagesButton.setBackgroundResource(R.drawable.green_cornered_bg)
            stickersButton.setBackgroundResource(R.drawable.white_cornered_bg)
            loadStoreItems("themes")
        })

        // Load initial items (stickers)
        loadStoreItems("stickers")

        return root
    }

    private fun loadStoreItems(category: String) {
        val itemsRef = database!!.getReference("store/$category")
        itemsRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                storeItems!!.clear() // Clear the existing list
                for (itemSnapshot in snapshot.children) {
                    val name = itemSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    val imageUrl = itemSnapshot.child("image").getValue(String::class.java) ?: ""

                    // Retrieve price as Double, directly from Firebase
                    val price = itemSnapshot.child("price").getValue(Double::class.java) ?: 0.99  // Fallback to 0.99 if null

                    // Create a new StoreItem object
                    val item = StoreItem(name, price, imageUrl)

                    storeItems!!.add(item) // Add the item to the list
                }
                adapter?.notifyDataSetChanged() // Notify adapter about data changes
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to load items: ${error.message}")
            }
        })
    }





}