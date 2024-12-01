package com.example.zendiary.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val privacyPolicyButton: LinearLayout = binding.privacyPolicyButton // Lấy reference của nút "Edit Profile"
        privacyPolicyButton.setOnClickListener {
            // Sử dụng NavController để điều hướng sang EditProfileFragment
            findNavController().navigate(R.id.action_profileFragment_to_privacyPolicyFragment)
        }


        // Thiết lập sự kiện click cho nút "Edit Profile"
        val editProfileButton: LinearLayout = binding.editProfileButton // Lấy reference của nút "Edit Profile"
        editProfileButton.setOnClickListener {
            // Sử dụng NavController để điều hướng sang EditProfileFragment
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        // Thiết lập sự kiện click cho nút "Edit Profile"
        val serviceButton: LinearLayout = binding.serviceButton // Lấy reference của nút "Edit Profile"
        serviceButton.setOnClickListener {
            // Sử dụng NavController để điều hướng sang EditProfileFragment
            findNavController().navigate(R.id.action_profileFragment_to_serviceFragment)
        }

        val helpButton: LinearLayout = binding.helpButton // Lấy reference của nút "Edit Profile"
        helpButton.setOnClickListener {
            // Sử dụng NavController để điều hướng sang EditProfileFragment
            findNavController().navigate(R.id.action_profileFragment_to_helpFragment)
        }

        val reminder: LinearLayout = binding.reminderButton // Lấy reference của nút "Edit Profile"
        reminder.setOnClickListener {
            // Sử dụng NavController để điều hướng sang EditProfileFragment
            findNavController().navigate(R.id.action_profileFragment_to_reminder)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
