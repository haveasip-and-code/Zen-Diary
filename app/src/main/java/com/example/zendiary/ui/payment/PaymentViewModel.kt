package com.example.zendiary.ui.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.zendiary.R
import com.example.zendiary.data.PaymentMethod

class PaymentViewModel : ViewModel() {

    private val _paymentMethods = MutableLiveData<MutableList<PaymentMethod>>()
    val paymentMethods: LiveData<MutableList<PaymentMethod>> get() = _paymentMethods

    init {
        // Initialize with default payment methods
        _paymentMethods.value = mutableListOf(
            PaymentMethod("MoMo", "Fast and secure mobile payment", R.drawable.ic_momo_logo, isSelected = true),
            PaymentMethod("Zalo Pay", "Convenient Zalo mobile payment", R.drawable.ic_zalopay_logo)
        )
    }

    // Add a new payment method
    fun addPaymentMethod(paymentMethod: PaymentMethod) {
        val currentList = _paymentMethods.value ?: mutableListOf()
        currentList.add(paymentMethod)
        _paymentMethods.value = currentList
    }

    // Update the selected payment method
    fun updateSelectedPaymentMethod(selectedMethod: PaymentMethod) {
        val currentList = _paymentMethods.value ?: return
        currentList.forEach { it.isSelected = it == selectedMethod }
        _paymentMethods.value = currentList
    }
}
