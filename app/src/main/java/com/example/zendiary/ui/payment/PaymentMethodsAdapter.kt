package com.example.zendiary.ui.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zendiary.R
import com.example.zendiary.data.PaymentMethod

class PaymentMethodsAdapter(
    private val paymentMethods: MutableList<PaymentMethod>,
    private val onPaymentMethodSelected: (PaymentMethod) -> Unit
) : RecyclerView.Adapter<PaymentMethodsAdapter.PaymentMethodViewHolder>() {

    inner class PaymentMethodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val iconImageView: ImageView = view.findViewById(R.id.ivImagePaymentMethod)
        private val titleTextView: TextView = view.findViewById(R.id.tvTitlePaymentMethod)
        private val infoLayout: RelativeLayout = view.findViewById(R.id.rlPaymentInfo)
        private val addPaymentLayout: RelativeLayout = view.findViewById(R.id.rlAddPayment)

        fun bind(paymentMethod: PaymentMethod) {
            // Set data to views
            iconImageView.setImageResource(paymentMethod.iconResId)
            titleTextView.text = paymentMethod.name

            // Show or hide specific layouts
            if (paymentMethod.isSelected) {
                infoLayout.visibility = View.VISIBLE
                addPaymentLayout.visibility = View.GONE
            } else {
                infoLayout.visibility = View.GONE
                addPaymentLayout.visibility = View.VISIBLE
            }

            // Handle click event
            itemView.setOnClickListener {
                onPaymentMethodSelected(paymentMethod)
            }
        }
    }

    fun updateData(newPaymentMethods: List<PaymentMethod>) {
        paymentMethods.clear()
        paymentMethods.addAll(newPaymentMethods)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_method, parent, false)
        return PaymentMethodViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
        holder.bind(paymentMethods[position])
    }

    override fun getItemCount(): Int = paymentMethods.size

    // Update the list dynamically
    fun updateList(newList: List<PaymentMethod>) {
        paymentMethods.clear()
        paymentMethods.addAll(newList)
        notifyDataSetChanged()
    }
}
