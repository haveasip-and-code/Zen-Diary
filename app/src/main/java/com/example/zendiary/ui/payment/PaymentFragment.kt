package com.example.zendiary.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zendiary.Global
import com.example.zendiary.R
import com.example.zendiary.data.PaymentMethod
import com.example.zendiary.databinding.FragmentPaymentBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.FirebaseDatabase
import kotlin.properties.Delegates

class PaymentFragment : Fragment() {
    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!! // Non-null binding property
    private lateinit var adapter: PaymentMethodsAdapter
    private val viewModel: PaymentViewModel by viewModels()
    private lateinit var packageName: String
    private var packageCoin by Delegates.notNull<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the binding
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve package name and coin value from the bundle
        arguments?.let { bundle ->
            packageName = bundle.getString("selectedPackage", "Unknown Package")
            packageCoin = bundle.getInt("packageCoin", 0)
        }

        // Set up RecyclerView
        adapter = PaymentMethodsAdapter(mutableListOf()) { selectedMethod ->
            // Update the selected payment method in the ViewModel
            viewModel.updateSelectedPaymentMethod(selectedMethod)
        }
        binding.rvPaymentMethod.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPaymentMethod.adapter = adapter

        // Observe the payment methods from ViewModel
        viewModel.paymentMethods.observe(viewLifecycleOwner) { paymentMethods ->
            adapter.updateData(paymentMethods) // Update RecyclerView adapter with new data
        }

        binding.ibBackButtonPayment.setOnClickListener {
            findNavController().navigateUp()
        }

        // Set up "Choose" button click
        binding.btnConfirmPaymentMethod.setOnClickListener {
            val selectedMethod = viewModel.paymentMethods.value?.find { it.isSelected }
            if (selectedMethod != null) {
                showConfirmPaymentDialog(selectedMethod)
            } else {
                Toast.makeText(requireContext(), "Please select a payment method", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Show the confirmation dialog
    private fun showConfirmPaymentDialog(selectedPaymentMethod: PaymentMethod) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirm_payment, null)
        bottomSheetDialog.setContentView(dialogView)

        // Set payment method details in the dialog
        val tvMethodName = dialogView.findViewById<TextView>(R.id.tvSelectedPaymentMethod)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmPayment)

        tvMethodName.text = selectedPaymentMethod.name

        btnConfirm.setOnClickListener {
            // Get the user's balance from Firebase
            val userId = Global.userId
            val userBalanceRef = FirebaseDatabase.getInstance().getReference("users/$userId/balance")

            // Fetch current balance
            userBalanceRef.get().addOnSuccessListener { snapshot ->
                val currentBalance = snapshot.getValue(Int::class.java) ?: 0

                val newBalance = currentBalance + packageCoin
                userBalanceRef.setValue(newBalance).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Payment confirmed! New balance: $newBalance",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Navigate to the store fragment
                        findNavController().navigate(R.id.action_paymentFragment_to_storeFragment)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to update balance. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch balance.", Toast.LENGTH_SHORT).show()
            }

            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
