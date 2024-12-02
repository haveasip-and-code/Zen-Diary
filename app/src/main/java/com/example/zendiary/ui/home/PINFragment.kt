package com.example.zendiary.ui.home

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.zendiary.R
import android.util.Log

class PINFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout for the fragment
        val rootView = inflater.inflate(R.layout.fragment_pincode, container, false)
        Log.d("DEBUG_TAG", "Activity created") // Log thông báo mức DEBUG

        // Initialize buttons by their IDs
        val buttons = listOf(
            rootView.findViewById<FrameLayout>(R.id.btn1_container),
            rootView.findViewById<FrameLayout>(R.id.btn2_container),
            rootView.findViewById<FrameLayout>(R.id.btn3_container),
            rootView.findViewById<FrameLayout>(R.id.btn4_container),
            rootView.findViewById<FrameLayout>(R.id.btn5_container),
            rootView.findViewById<FrameLayout>(R.id.btn6_container),
            rootView.findViewById<FrameLayout>(R.id.btn7_container),
            rootView.findViewById<FrameLayout>(R.id.btn8_container),
            rootView.findViewById<FrameLayout>(R.id.btn9_container),
            rootView.findViewById<FrameLayout>(R.id.btn0_container)
        )

// Đảm bảo rằng `buttons` chứa danh sách các container (FrameLayout)
        buttons.forEach { buttonContainer ->
            val imageButton = buttonContainer.getChildAt(0) as ImageButton // ImageButton luôn là phần tử đầu tiên
            val textView = buttonContainer.getChildAt(1) as TextView       // TextView luôn là phần tử thứ hai

            // Đặt sự kiện nhấn cho từng container
            imageButton.setOnClickListener {
                // Gọi hàm để thay đổi appearance cho ImageButton và TextView
                changeButtonAppearance(buttonContainer)
            }
        }

        return rootView
    }

    private fun changeButtonAppearance(container: FrameLayout) {
        Log.d("DEBUG_TAG", "Activity created") // Log thông báo mức DEBUG

        // Lấy ImageButton và TextView bằng cách dựa vào thứ tự trong FrameLayout
        val imageButton = container.getChildAt(0) as ImageButton // ImageButton luôn là phần tử đầu tiên
        val textView = container.getChildAt(1) as TextView       // TextView luôn là phần tử thứ hai

        // Thay đổi giao diện
        imageButton.setBackgroundResource(R.drawable.background_click) // Highlighted background
        textView.setTextColor(Color.WHITE) // Đổi màu chữ thành trắng

        // Hoàn nguyên sau 0.5 giây
        Handler(Looper.getMainLooper()).postDelayed({
            imageButton.setBackgroundResource(R.drawable.button_background_pin) // Default background
            textView.setTextColor(Color.BLACK) // Default text color
        }, 250)
    }
}
