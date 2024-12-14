package com.example.zendiary.ui.profile

import android.annotation.SuppressLint
import com.example.zendiary.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.zendiary.Global

class StoreFragment : Fragment()
    , CoinPackageDialogFragment.OnPackageSelectedListener
{
    private lateinit var stickersButton: TextView
    private lateinit var pagesButton: TextView
    private lateinit var buyFlowerButton: ImageView
    private lateinit var coinFlowerTextView: TextView

    private var recyclerView: RecyclerView? = null
    private var adapter: StoreAdapter? = null
    private var database: FirebaseDatabase? = null
    private var storeItems: MutableList<StoreItem>? = null
    private var userId: String? = Global.userId

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the store fragment layout
        val root: View = inflater.inflate(R.layout.fragment_store, container, false)

        // Initialize buttons and text views
        stickersButton = root.findViewById(R.id.Stickers)
        pagesButton = root.findViewById(R.id.Pages)
        coinFlowerTextView = root.findViewById(R.id.coinFlower)

        // Retrieve user ID from arguments or set default
        userId = arguments?.getString("userId") ?: "userId_12345"

        // Initialize RecyclerView
        recyclerView = root.findViewById(R.id.recycler_view_notes)
        recyclerView!!.layoutManager = GridLayoutManager(requireContext(), 2)

        // Initialize store items and adapter
        storeItems = ArrayList()
        adapter = StoreAdapter(storeItems as ArrayList<StoreItem>) { storeItem ->
            onStoreItemClick(storeItem)
        }
        recyclerView!!.adapter = adapter

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()

        // Set initial background for buttons
        stickersButton.setBackgroundResource(R.drawable.green_cornered_bg)
        pagesButton.setBackgroundResource(R.drawable.white_cornered_bg)

        // Set click listeners for category selection
        stickersButton.setOnClickListener {
            stickersButton.setBackgroundResource(R.drawable.green_cornered_bg)
            pagesButton.setBackgroundResource(R.drawable.white_cornered_bg)
            loadStoreItems("stickers")
        }

        pagesButton.setOnClickListener {
            pagesButton.setBackgroundResource(R.drawable.green_cornered_bg)
            stickersButton.setBackgroundResource(R.drawable.white_cornered_bg)
            loadStoreItems("themes")
        }


        buyFlowerButton = root.findViewById(R.id.buyFlower)

        buyFlowerButton.setOnClickListener {
            val coinPackageDialog = CoinPackageDialogFragment.newInstance(userId!!)
            coinPackageDialog.show(childFragmentManager, "CoinPackageDialog")
        }




        // Back button setup
        val backButton: ImageButton = root.findViewById(R.id.backButtonProfileEdit)
        backButton.setOnClickListener {
            // Navigate back to the previous fragment
            findNavController().navigateUp()
        }


        // Load initial items (stickers) and balance
        loadStoreItems("stickers")
        loadUserBalance()

        return root
    }


    override fun onPackageSelected(packageName: String) {
        val bundle = Bundle().apply {
            putString("selectedPackage", packageName)  // Pass the selected package name
        }

        // Navigate to the PaymentFragment with the package name
        findNavController().navigate(R.id.action_storeFragment_to_paymentFragment, bundle)
    }

    private fun loadStoreItems(category: String) {
        val purchasedCategory = "purchased${category.capitalize()}"
        val userPurchasedRef = database!!.getReference("users/$userId/store/$purchasedCategory")

        Log.d("StoreFragment", "Purchased Names Directory: $userPurchasedRef")

        val itemsRef = database!!.getReference("store/$category")

        // Fetch purchased items first
        userPurchasedRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(purchasedSnapshot: DataSnapshot) {
                // Get the list of purchased items by their keys
                val purchasedItemNames = purchasedSnapshot.children.mapNotNull { it.key }.toSet()

                // Log purchased item names
                Log.d("StoreFragment", "Purchased Names: $purchasedItemNames")

                // Now fetch store items
                itemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        storeItems!!.clear() // Clear the existing list
                        for (itemSnapshot in snapshot.children) {
                            val itemName = itemSnapshot.child("name").getValue(String::class.java) ?: "Unknown"

                            // Log each store item name and check if it is purchased
                            Log.d("StoreFragment", "Checking item name: $itemName")
                            if (purchasedItemNames.contains(itemName)) {
                                Log.d("StoreFragment", "Skipping purchased item: $itemName")
                                continue
                            }

                            val imageUrl = itemSnapshot.child("image").getValue(String::class.java)
                                ?: "https://i.ibb.co/mBFVZgM/paper-default.png"

                            val price = itemSnapshot.child("price").let { priceSnapshot ->
                                when {
                                    priceSnapshot.value is Double -> priceSnapshot.getValue(Double::class.java)
                                    priceSnapshot.value is String -> priceSnapshot.getValue(String::class.java)
                                        ?.toDoubleOrNull() ?: 0.99
                                    else -> 0.99
                                }
                            }

                            // Log the store item details
                            Log.d("StoreFragment", "Adding item: $itemName, Price: $price")

                            val item = StoreItem(itemName, price ?: 0.99, imageUrl)
                            storeItems!!.add(item) // Add the item to the list
                        }
                        adapter?.notifyDataSetChanged() // Notify adapter about data changes
                        Log.d("StoreFragment", "Final Store Items: $storeItems")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseError", "Failed to load store items: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to load purchased items: ${error.message}")
            }
        })
    }







    private fun loadUserBalance() {
        val userBalanceRef = database!!.getReference("users/$userId/balance")
        userBalanceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val balance = snapshot.getValue(Int::class.java) ?: 0 // Default balance = 0
                coinFlowerTextView.text = balance.toString()
                Log.d("StoreFragment", "User balance: $balance")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to load balance: ${error.message}")
            }
        })
    }



    private fun onStoreItemClick(storeItem: StoreItem) {
        val currentCategory = if (stickersButton.background.constantState == requireContext().getDrawable(R.drawable.green_cornered_bg)?.constantState) {
            "purchasedStickers"
        } else {
            "purchasedThemes"
        }

        val purchasedRef = database!!.getReference("users/$userId/store/$currentCategory")
        val balanceRef = database!!.getReference("users/$userId/balance")

        purchasedRef.get().addOnSuccessListener { snapshot ->
            val purchasedItems = snapshot.children.mapNotNull { it.key }

            Log.d("StoreFragment", "Purchased Names: $purchasedItems")

            val isPurchased = purchasedItems.contains(storeItem.name)

            if (isPurchased) {
                Log.d("StoreFragment", "User already owns: ${storeItem.name}")
            } else {
                balanceRef.get().addOnSuccessListener { balanceSnapshot ->
                    val currentBalance = balanceSnapshot.getValue(Int::class.java) ?: 0
                    if (currentBalance >= storeItem.price) {
                        purchaseItem(storeItem, purchasedRef, balanceRef, currentBalance)
                    } else {
                        Log.d("StoreFragment", "Insufficient balance for: ${storeItem.name}")
                    }
                }.addOnFailureListener {
                    Log.e("StoreFragment", "Failed to retrieve user balance")
                }
            }
        }.addOnFailureListener {
            Log.e("StoreFragment", "Failed to retrieve purchased items")
        }
    }


    @SuppressLint("RestrictedApi")
    private fun purchaseItem(
        storeItem: StoreItem,
        userPurchasedRef: DatabaseReference,
        balanceRef: DatabaseReference,
        currentBalance: Int
    ) {
        val updatedBalance = currentBalance - storeItem.price.toInt()

        val updates = hashMapOf<String, Any>(
            "${userPurchasedRef.path}/${storeItem.name}" to true,
            balanceRef.path.toString() to updatedBalance
        )

        database!!.reference.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("StoreFragment", "Successfully purchased: ${storeItem.name}")

                // Reload the store to reflect updated purchases
                //loadStoreItems(userPurchasedRef.parent?.key.toString())
            } else {
                Log.e("StoreFragment", "Failed to purchase item: ${storeItem.name}")
            }
        }
    }






}
