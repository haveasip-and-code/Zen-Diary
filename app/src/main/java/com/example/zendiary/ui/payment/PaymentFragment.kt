package com.example.zendiary.ui.payment

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zendiary.R

class PaymentFragment : Fragment() {

    private lateinit var viewModel: PaymentViewModel
    private lateinit var adapter: PaymentMethodsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[PaymentViewModel::class.java]

        // Set up RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvPaymentMethod)
        adapter = PaymentMethodsAdapter(mutableListOf()) { selectedMethod ->
            // Update selected payment method in ViewModel
            viewModel.updateSelectedPaymentMethod(selectedMethod)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observe payment methods
        viewModel.paymentMethods.observe(viewLifecycleOwner) { updatedList ->
            adapter.updateList(updatedList)
        }

        return view
    }
}