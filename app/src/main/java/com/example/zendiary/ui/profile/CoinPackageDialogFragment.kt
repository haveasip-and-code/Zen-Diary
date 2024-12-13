package com.example.zendiary.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.example.zendiary.R


class CoinPackageDialogFragment : DialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private var database: FirebaseDatabase? = null
    private var coinPackages: MutableList<CoinPackage> = mutableListOf()
    private var userId: String = ""

    companion object {
        fun newInstance(userId: String): CoinPackageDialogFragment {
            val fragment = CoinPackageDialogFragment()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_coin_packages, container, false)

        recyclerView = view.findViewById(R.id.recyclerView_coin_packages)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up back button listener
        val backButton: ImageView = view.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            dismiss() // Dismiss the dialog when the back button is pressed
        }

        database = FirebaseDatabase.getInstance()
        userId = arguments?.getString("userId") ?: ""

        fetchCoinPackages()

        return view
    }

    override fun onStart() {
        super.onStart()

        // Access the dialog window
        val window = dialog?.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(), // Set width to 85% of screen width
            ViewGroup.LayoutParams.WRAP_CONTENT // Height adjusts based on content
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent) // Make background transparent
    }


    private fun fetchCoinPackages() {
        val packagesRef = database!!.getReference("store/packages")
        packagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                coinPackages.clear()
                for (packageSnapshot in snapshot.children) {
                    val name = packageSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    val coins = packageSnapshot.child("coin").getValue(Int::class.java) ?: 0
                    val price = packageSnapshot.child("price").getValue(Int::class.java) ?: 0

                    val coinPackage = CoinPackage(name, coins, price)
                    coinPackages.add(coinPackage)
                }
                recyclerView.adapter = CoinPackageAdapter(coinPackages) { coinPackage ->
                    onCoinPackageClick(coinPackage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to fetch coin packages: ${error.message}")
            }
        })
    }

    private fun onCoinPackageClick(coinPackage: CoinPackage) {
        val balanceRef = database!!.getReference("users/$userId/balance")
        balanceRef.get().addOnSuccessListener { balanceSnapshot ->
            val currentBalance = balanceSnapshot.getValue(Int::class.java) ?: 0
            val updatedBalance = currentBalance + coinPackage.coins
            balanceRef.setValue(updatedBalance).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("CoinPackageDialog", "Successfully added ${coinPackage.coins} coins.")
                    dismiss() // Close dialog after purchase
                } else {
                    Log.e("CoinPackageDialog", "Failed to update balance.")
                }
            }
        }
    }
}
