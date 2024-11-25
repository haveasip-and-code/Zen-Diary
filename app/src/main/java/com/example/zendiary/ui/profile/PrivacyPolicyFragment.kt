package com.example.zendiary.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.EditText
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zendiary.R

class PrivacyPolicyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout cho fragment
        val rootView = inflater.inflate(R.layout.privacypolicy_fragment, container, false)

        // Lấy các view từ layout
        val backButton: ImageButton = rootView.findViewById(R.id.backButtonPolicy)

        // Thiết lập sự kiện cho nút "Back"
        backButton.setOnClickListener {
            // Sử dụng NavController để quay lại ProfileFragment
            findNavController().navigateUp() // Điều này sẽ đưa bạn quay lại fragment trước đó
        }
        return rootView
    }
}
