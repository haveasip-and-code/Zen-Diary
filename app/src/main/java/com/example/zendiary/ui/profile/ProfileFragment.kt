package com.example.zendiary.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.zendiary.Global
import com.example.zendiary.R
import com.example.zendiary.databinding.FragmentProfileBinding
import com.example.zendiary.ui.home.LogIn
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var isNotificationOn = true // Variable to track notification state

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Reference to Firebase database
        val database: DatabaseReference = FirebaseDatabase.getInstance()
            .getReference("users/${Global.userId}/profile")

        // Fetch data from Firebase and update UI
        database.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val email = dataSnapshot.child("email").value.toString()
                val fullname = dataSnapshot.child("fullname").value.toString()
                val phone = dataSnapshot.child("phone").value.toString()

                // Update TextViews using ViewBinding
                binding.userName.text = fullname
                binding.userEmail.text = email + " | " + phone
            } else {
                Toast.makeText(requireContext(), "User profile data not found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to fetch data: ${it.message}", Toast.LENGTH_SHORT).show()
        }

        // Observe ViewModel text and update TextView
        homeViewModel.text.observe(viewLifecycleOwner) { text ->
            binding.textProfile.text = text
        }

        // Toggle Notification State
        binding.notificationLayout.setOnClickListener {
            isNotificationOn = !isNotificationOn
            binding.notificationStatus.text = if (isNotificationOn) "ON" else "OFF"
        }

        // Set click listeners for navigation buttons
        binding.privacyPolicyButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_privacyPolicyFragment)
        }

        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        binding.serviceButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_serviceFragment)
        }

        binding.helpButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_helpFragment)
        }

        binding.reminderButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_reminder)
        }

        binding.storeButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_storeFragment)
        }

        // Handle logout button click
        binding.logoutButton.setOnClickListener {
            val intent = Intent(requireActivity(), LogIn::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
