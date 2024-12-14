package com.example.zendiary.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.zendiary.databinding.FragmentProfileBinding
import androidx.navigation.fragment.findNavController
import com.example.zendiary.R

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var isNotificationOn = true // Biến trạng thái để theo dõi

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Thiết lập sự kiện cho TextView và ViewModel
        val textView: TextView = binding.textProfile
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Thiết lập sự kiện click cho mục "Notifications"
        val notificationLayout = binding.notificationLayout // Bạn cần xác định ID cho layout của notifications
        val notificationStatus = binding.notificationStatus // TextView hiển thị trạng thái

        notificationLayout.setOnClickListener {
            // Chuyển đổi trạng thái (ON/OFF)
            isNotificationOn = !isNotificationOn

            // Cập nhật trạng thái hiển thị
            if (isNotificationOn) {
                notificationStatus.text = "ON"
            } else {
                notificationStatus.text = "OFF"
            }
        }

        // Thiết lập sự kiện click cho nút "Privacy Policy"
        val privacyPolicyButton: LinearLayout = binding.privacyPolicyButton
        privacyPolicyButton.setOnClickListener {
            // Điều hướng sang PrivacyPolicyFragment
            findNavController().navigate(R.id.action_profileFragment_to_privacyPolicyFragment)
        }

        // Thiết lập sự kiện click cho nút "Edit Profile"
        val editProfileButton: LinearLayout = binding.editProfileButton
        editProfileButton.setOnClickListener {
            // Điều hướng sang EditProfileFragment
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        // Thiết lập sự kiện click cho nút "Service"
        val serviceButton: LinearLayout = binding.serviceButton
        serviceButton.setOnClickListener {
            // Điều hướng sang ServiceFragment
            findNavController().navigate(R.id.action_profileFragment_to_serviceFragment)
        }

        // Thiết lập sự kiện click cho nút "Help"
        val helpButton: LinearLayout = binding.helpButton
        helpButton.setOnClickListener {
            // Điều hướng sang HelpFragment
            findNavController().navigate(R.id.action_profileFragment_to_helpFragment)
        }

        // Thiết lập sự kiện click cho nút "Reminder"
        val reminder: LinearLayout = binding.reminderButton
        reminder.setOnClickListener {
            // Điều hướng sang ReminderFragment
            findNavController().navigate(R.id.action_profileFragment_to_reminder)
        }

        // Bind and set up the Store button
        val storeButton: LinearLayout = binding.storeButton
        storeButton.setOnClickListener {
            // Navigate to StoreFragment
            findNavController().navigate(R.id.action_navigation_profile_to_storeFragment)
        }


        // Sử dụng ViewBinding thay vì findViewById cho logout_button
        val logoutButton = binding.logoutButton
        logoutButton.setOnClickListener {
            // Điều hướng sang PinCodeFragment
            findNavController().navigate(R.id.action_profileFragment_to_pinCode)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
