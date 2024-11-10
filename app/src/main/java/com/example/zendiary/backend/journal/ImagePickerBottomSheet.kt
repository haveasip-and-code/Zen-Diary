package com.example.zendiary.backend.journal

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.zendiary.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ImagePickerBottomSheet() : BottomSheetDialogFragment() {
    private lateinit var listener: OnImageOptionSelectedListener

    // This will ensure that the parent activity or fragment implements the listener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Ensure the parent fragment (not activity) implements the listener
        listener = if (parentFragment is OnImageOptionSelectedListener) {
            parentFragment as OnImageOptionSelectedListener
        } else if (context is OnImageOptionSelectedListener) {
            context
        } else {
            throw RuntimeException("$context must implement OnImageOptionSelectedListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this bottom sheet dialog
        return inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the buttons to call the respective methods in the listener
        view.findViewById<Button>(R.id.btn_gallery).setOnClickListener {
            listener.onGalleryOptionSelected()  // Callback to parent activity/fragment
            dismiss()  // Dismiss the bottom sheet
        }

        view.findViewById<Button>(R.id.btn_camera).setOnClickListener {
            listener.onCameraOptionSelected()  // Callback to parent activity/fragment
            dismiss()  // Dismiss the bottom sheet
        }

        view.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            // Handle cancel button click
            dismiss()  // Dismiss the bottom sheet when cancel is clicked
        }
    }

    // Listener interface to pass the selected option back to the parent activity/fragment
    interface OnImageOptionSelectedListener {
        fun onGalleryOptionSelected()
        fun onCameraOptionSelected()
    }
}
