package com.example.zendiary.ui.journal

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import com.example.zendiary.R
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.MediaStore
import android.text.Spannable
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zendiary.backend.journal.ColorPickerDialog
import com.example.zendiary.backend.journal.DrawingView
import com.example.zendiary.backend.journal.ImagePickerBottomSheet
import com.example.zendiary.backend.journal.PenToolBottomSheetDialog

class JournalFragment : Fragment(), ImagePickerBottomSheet.OnImageOptionSelectedListener {

    companion object {
        fun newInstance() = JournalFragment()
    }

    private val viewModel: JournalViewModel by viewModels()

    private lateinit var drawingView: DrawingView
    private lateinit var drawingToolbar: LinearLayout
    private lateinit var ibHandwriting: ImageButton
    private lateinit var journalFrame: FrameLayout
    private lateinit var ibUndo: ImageButton
    private lateinit var ibRedo: ImageButton
    private lateinit var ibColorPicker: ImageButton
    private lateinit var ibShapePicker: ImageButton
    private lateinit var ibPenPicker: ImageButton
    private lateinit var ibEraser: ImageButton

    private lateinit var ibAddImage: ImageButton
    private lateinit var imagePreviewContainer: LinearLayout
    private lateinit var ivImagePreview: ImageView

    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private val STORAGE_PERMISSION_REQUEST_CODE = 1002
    private val CAMERA_REQUEST = 101
    private val PICK_IMAGE_REQUEST = 102

    private var selectedImageUri: Uri? = null

    private lateinit var popupWindow: PopupWindow
    private lateinit var editText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_journal, container, false)
        editText = view.findViewById(R.id.et_content)

        initToolbarPopup() // Khởi tạo popup toolbar
        setupEditTextSelectionListener() // Lắng nghe sự kiện chọn văn bản

        // Initialize the layout and views
        initViews(view)

        // Set up button click listeners
        setupButtonListeners(view)

        ibAddImage.setOnClickListener {
            // Show the bottom sheet dialog when the image button is clicked
            val bottomSheet = ImagePickerBottomSheet()
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

        view.post {
            showPopup()  // Gọi showPopup sau khi giao diện đã được vẽ
        }
        return view
    }

    // Function to initialize the views and layout
    private fun initViews(view: View) {
        drawingToolbar = view.findViewById(R.id.drawing_toolbar)
        journalFrame = view.findViewById(R.id.journalFrame)

        // Initialize the DrawingView and add it to the layout
        drawingView = DrawingView(requireContext(), null).apply {
            visibility = View.GONE  // Initially hidden
        }
        journalFrame.addView(drawingView)  // Add the DrawingView as an overlay

        ibAddImage = view.findViewById(R.id.ib_add_image)

        ivImagePreview = view.findViewById(R.id.iv_image_preview)
        imagePreviewContainer = view.findViewById(R.id.image_preview_container)
    }

    // Track whether eraser is currently active or not
    private var isEraserActive = false

    // Function to set up button click listeners
    private fun setupButtonListeners(view: View) {
        ibHandwriting = view.findViewById(R.id.ib_handwriting)
        ibHandwriting.setOnClickListener {
            toggleHandwritingMode()
        }

        ibUndo = view.findViewById(R.id.ib_undo)
        ibUndo.setOnClickListener {

        }

        ibRedo = view.findViewById(R.id.ib_redo)
        ibRedo.setOnClickListener {

        }

        ibColorPicker = view.findViewById(R.id.ib_color_picker)
        ibColorPicker.setOnClickListener {
            openColorPicker()
        }

        ibShapePicker = view.findViewById(R.id.ib_shape_picker)
        ibShapePicker.setOnClickListener {

        }

        ibPenPicker = view.findViewById(R.id.ib_pen_tool)
        ibPenPicker.setOnClickListener {
            openPenToolBottomSheet()
        }

        ibEraser = view.findViewById(R.id.ib_eraser_tool)
        ibEraser.setOnClickListener {
            // Toggle the eraser mode
            isEraserActive = !isEraserActive

            // Enable or disable eraser mode based on the current state
            drawingView.enableEraserMode(isEraserActive)

            // Optionally, change the button icon to indicate eraser mode is active or not
            if (isEraserActive) {
                ibEraser.setBackgroundResource(R.drawable.bg_bottom_toolbar_journal_btn_selected)  // Use an active icon
            } else {
                ibEraser.setBackgroundColor(resources.getColor(android.R.color.transparent, null))  // Use the default icon
            }
        }
    }

    private fun openColorPicker() {
        // Open the color picker dialog
        val colorPickerDialog = ColorPickerDialog(requireContext()) { selectedColor ->
            // When a color is selected, update the drawing view's brush color
            drawingView.setBrushColor(selectedColor)
        }
        colorPickerDialog.show()
    }

    private fun openPenToolBottomSheet() {
        // Show the Pen Tool Bottom Sheet Dialog
        val penToolDialog = PenToolBottomSheetDialog { selectedBrush, brushWidth ->
            // Handle the brush selection and width adjustment
            drawingView.setBrushType(selectedBrush)
            drawingView.setBrushWidth(brushWidth)
        }

        penToolDialog.show(parentFragmentManager, "PenToolBottomSheet")
    }

    override fun onGalleryOptionSelected() {
        // Open gallery to pick an image
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // This method is invoked when the camera option is selected
    override fun onCameraOptionSelected() {
        // Check camera permission first
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Open the camera
            openCamera()
        } else {
            // Request camera permission
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    // This method opens the camera to take a picture
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST)
    }


    // Handle the result after image is picked
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    selectedImageUri = data?.data
                    insertImageIntoPreview(selectedImageUri)
                }
                CAMERA_REQUEST -> {
                    val photo: Bitmap = data?.extras?.get("data") as Bitmap
                    insertImageIntoPreview(photo)
                }
            }
        }
    }

    // Insert image into the preview container's ImageView
    private fun insertImageIntoPreview(imageUri: Uri?) {
        imageUri?.let {
            // Convert the image URI into a bitmap
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
            ivImagePreview.setImageBitmap(bitmap)

            // Make the image preview container visible
            imagePreviewContainer.visibility = View.VISIBLE

            Log.d("ImagePicker", "Image selected from gallery")
        }
    }

    private fun insertImageIntoPreview(bitmap: Bitmap) {
        // Insert the bitmap directly into the ImageView
        ivImagePreview.setImageBitmap(bitmap)

        // Make the image preview container visible
        imagePreviewContainer.visibility = View.VISIBLE

        Log.d("ImagePicker", "Image taken from camera")
    }

    // Khởi tạo popup toolbar
    private fun initToolbarPopup() {
        val toolbarView = layoutInflater.inflate(R.layout.toolbar_formatting, null)
        popupWindow = PopupWindow(toolbarView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            isOutsideTouchable = true
            isFocusable = true
        }

        // Xử lý sự kiện cho các nút trong popup toolbar
        val btnBold = toolbarView.findViewById<ImageButton>(R.id.btn_bold)
        val btnItalic = toolbarView.findViewById<ImageButton>(R.id.btn_italic)

        btnBold.setOnClickListener { applyBoldStyle() }
        btnItalic.setOnClickListener { applyItalicStyle() }
    }

    // Thiết lập sự kiện chọn văn bản trong EditText
    private fun setupEditTextSelectionListener() {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && editText.selectionStart != editText.selectionEnd) {
                showToolbar()
            } else {
                popupWindow.dismiss()
            }
        }

        editText.setOnClickListener {
            if (editText.selectionStart != editText.selectionEnd) {
                showToolbar()
            } else {
                popupWindow.dismiss()
            }
        }
    }

    // Hiển thị thanh công cụ ngay bên dưới vùng chọn văn bản
    private fun showToolbar() {
        val location = IntArray(2)
        editText.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1] + editText.height
        popupWindow.showAtLocation(editText, Gravity.TOP or Gravity.START, x, y)
    }

    // Hàm áp dụng định dạng in đậm cho văn bản được chọn
    private fun applyBoldStyle() {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        if (start < end) {
            editText.text.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        popupWindow.dismiss()
    }

    // Hàm áp dụng định dạng in nghiêng cho văn bản được chọn
    private fun applyItalicStyle() {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        if (start < end) {
            editText.text.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        popupWindow.dismiss()
    }

    // Toggle the visibility of the drawing mode
    private fun toggleHandwritingMode() {
        if (drawingView.visibility == View.GONE) {
            drawingView.visibility = View.VISIBLE
            drawingToolbar.visibility = View.VISIBLE
            ibHandwriting.setBackgroundResource(R.drawable.bg_bottom_toolbar_journal_btn_selected)  // Set to colored background
        } else {
            drawingView.visibility = View.GONE
            drawingToolbar.visibility = View.GONE
            ibHandwriting.setBackgroundColor(resources.getColor(android.R.color.transparent, null))  // Set to transparent background
        }
    }

    private fun showPopup() {
        // Inflate layout popup
        val inflater = layoutInflater
        val popupView = inflater.inflate(R.layout.popup_layout, null)

        // Tạo nền tối mờ cho popup
        val parentView = requireActivity().window.decorView.rootView
        val dimBackground = ColorDrawable(Color.BLACK)
        dimBackground.alpha = 150 // Độ mờ của nền (0-255)
        parentView.overlay.add(dimBackground)

        // Tạo PopupWindow với kích thước toàn màn hình
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT, // Chiều rộng toàn màn hình
            ViewGroup.LayoutParams.MATCH_PARENT  // Chiều cao toàn màn hình
        ).apply {
            isOutsideTouchable = true
            isFocusable = true
            elevation = 10F // Thêm độ nổi để popup nổi bật hơn
            setBackgroundDrawable(ColorDrawable(Color.WHITE)) // Nền trắng cho popup

            setOnDismissListener {
                // Xóa nền mờ khi popup bị đóng
                parentView.overlay.remove(dimBackground)
            }
        }

        // Get the RecyclerView and set it up
        val recyclerView: RecyclerView = popupView.findViewById(R.id.popupRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3) // 3 columns

        // List of items to display in the popup
        val items = listOf("Family", "Family", "Family", "Family", "Music", "Family")

        // Set the adapter with a click listener
        recyclerView.adapter = PopupAdapter(items) { selectedItem ->
            // Handle item click (e.g., dismiss popup and show a message)
            popupWindow.dismiss()
        }

        // Show the PopupWindow at the center of the screen
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0)
    }


}

class PopupAdapter(
    private val items: List<String>,
    private val itemClickListener: (String) -> Unit
) : RecyclerView.Adapter<PopupAdapter.PopupViewHolder>() {

    class PopupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.itemText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return PopupViewHolder(view)
    }

    override fun onBindViewHolder(holder: PopupViewHolder, position: Int) {
        holder.textView.text = items[position]
        holder.itemView.setOnClickListener {
            itemClickListener(items[position])
        }
    }

    override fun getItemCount(): Int = items.size
}