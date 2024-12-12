package com.example.zendiary.data

data class PaymentMethod(
    val name: String,
    val description: String,
    val iconResId: Int, // Resource ID for the icon
    var isSelected: Boolean = false // To indicate if the method is selected
)

