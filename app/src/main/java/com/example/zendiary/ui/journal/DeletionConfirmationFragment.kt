package com.example.zendiary.ui.journal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zendiary.R
import com.example.zendiary.databinding.FragmentDeletionConfirmationBinding

class DeletionConfirmationFragment : Fragment(R.layout.fragment_deletion_confirmation) {

    private lateinit var binding: FragmentDeletionConfirmationBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDeletionConfirmationBinding.bind(view)

        // Handle the "Back to Home" button click
        binding.btnBackToHome.setOnClickListener {
            findNavController().navigate(R.id.navigation_home)
        }
    }
}
