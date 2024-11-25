package com.example.zendiary.ui.journal

import android.Manifest
import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Layout
import android.text.Spannable
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zendiary.R
import com.example.zendiary.backend.journal.ColorPickerDialog
import com.example.zendiary.backend.journal.DrawingView
import com.example.zendiary.backend.journal.ImagePickerBottomSheet
import com.example.zendiary.backend.journal.PenToolBottomSheetDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class JournalFragment : Fragment(), ImagePickerBottomSheet.OnImageOptionSelectedListener {

    companion object {
        fun newInstance() = JournalFragment()
    }

    private lateinit var viewModel: JournalViewModel

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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Kiểm tra xem popup đã hiển thị chưa từ SharedPreferences
        if (true) {//getPopupState()) {
            view?.post {
                //showPopup()
                savePopupState(false)
            }
        }


        getTextFromFirebase("userId_12345", "entryId_1", object : FirebaseCallback {
            override fun onSuccess(text: String?) {
                // Xử lý khi dữ liệu được lấy thành công
                editText.setText(text);
            }

            override fun onFailure(errorMessage: String?) {
                // Xử lý khi có lỗi xảy ra
                Log.e("FirebaseText", "Error: $errorMessage")
            }
        })

    }

    fun getTextFromFirebase(userId: String, entryId: String, callback: FirebaseCallback) {
        // Tham chiếu đến Firebase Database
        val database = FirebaseDatabase.getInstance()
        val textRef = database.getReference("users/$userId/entries/$entryId/text")

        // Đọc dữ liệu từ Firebase
        textRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val text = dataSnapshot.getValue(String::class.java) // Lấy giá trị dạng String
                    callback.onSuccess(text) // Gọi callback khi đọc dữ liệu thành công
                } else {
                    callback.onFailure("Text not found!")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onFailure(databaseError.message)
            }
        })
    }


    // Lưu giá trị của biến popupIsShown vào SharedPreferences
    fun savePopupState(isPopupShown: Boolean) {
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("popupIsShown", isPopupShown)  // Lưu trạng thái của popup
        editor.apply()  // Lưu thay đổi
    }

    // Lấy giá trị của popupIsShown từ SharedPreferences
    fun getPopupState(): Boolean {
        val sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Nếu không tìm thấy giá trị lưu trữ, mặc định là true (popup sẽ được hiển thị lần đầu tiên)
        return sharedPreferences.getBoolean("popupIsShown", false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this)[JournalViewModel::class.java]

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_journal, container, false)
        editText = view.findViewById(R.id.et_content)

        initToolbarPopup() // Khởi tạo popup toolbar
        setupEditTextSelectionListener() // Lắng nghe sự kiện chọn văn bản

        // Initialize the layout and views
        initViews(view)

        // Observe the sentiment result from the ViewModel
        observeSentimentResult()

        // Set up button click listeners
        setupButtonListeners(view)

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

        ivImagePreview = view.findViewById(R.id.iv_image_preview)
        imagePreviewContainer = view.findViewById(R.id.image_preview_container)
    }

    // Track whether eraser is currently active or not
    private var isEraserActive = false

    // Function to set up button click listeners
    private fun setupButtonListeners(view: View) {

        ibAddImage = view.findViewById(R.id.ib_add_image)
        ibAddImage.setOnClickListener {
            // Show the bottom sheet dialog when the image button is clicked
            val bottomSheet = ImagePickerBottomSheet()
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

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

        view.findViewById<Button>(R.id.btn_save).setOnClickListener {
            val journalText = editText.text.toString()
            saveTextToFirebase("userId_12345", "entryId_1", journalText, object : FirebaseCallback {
                override fun onSuccess(text: String?) {
                    Toast.makeText(requireContext(), "Saved successfully!", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onFailure(errorMessage: String?) {
                    Toast.makeText(requireContext(), "Failed to save: $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            if (journalText.isNotEmpty()) {
                viewModel.analyzeSentiment(journalText)
                Toast.makeText(requireContext(), "Sentiment analysis completed", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter text to analyze", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun observeSentimentResult() {
        viewModel.sentimentResult.observe(viewLifecycleOwner) { sentiment ->
            if (sentiment != null) {
                val sentimentScore = sentiment["compound"] ?: 0f
                val sentimentMessage = when {
                    sentimentScore > 0.1 -> "Positive Sentiment"
                    sentimentScore < -0.1 -> "Negative Sentiment"
                    else -> "Neutral Sentiment"
                }
                Toast.makeText(context, sentimentMessage, Toast.LENGTH_LONG).show()
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
        popupWindow = PopupWindow(
            toolbarView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            isOutsideTouchable = true
            isFocusable = true
        }

        // Formatting buttons
        val btnBold = toolbarView.findViewById<ImageButton>(R.id.btn_bold)
        val btnItalic = toolbarView.findViewById<ImageButton>(R.id.btn_italic)
        val btnAlignLeft = toolbarView.findViewById<ImageButton>(R.id.btn_align_left)
        val btnAlignCenter = toolbarView.findViewById<ImageButton>(R.id.btn_align_center)
        val btnAlignRight = toolbarView.findViewById<ImageButton>(R.id.btn_align_right)
        val btnAlignJustify = toolbarView.findViewById<ImageButton>(R.id.btn_align_justify)
        val btnTextColor = toolbarView.findViewById<ImageButton>(R.id.btn_text_color)

        // Add click listeners
        btnBold.setOnClickListener { applyBoldStyle() }
        btnItalic.setOnClickListener { applyItalicStyle() }
        btnAlignLeft.setOnClickListener { applyAlignment("left") }
        btnAlignCenter.setOnClickListener { applyAlignment("center") }
        btnAlignRight.setOnClickListener { applyAlignment("right") }
        btnAlignJustify.setOnClickListener { applyAlignment("justify") }
        btnTextColor.setOnClickListener { openColorPickerToolBar() }
    }

    private fun applyAlignment(alignment: String) {
        val alignmentSpan = when (alignment) {
            "left" -> AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL)
            "center" -> AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)
            "right" -> AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE)
            "justify" -> AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL) // Justification can depend on Android version
            else -> null
        }

        alignmentSpan?.let {
            val start = editText.selectionStart
            val end = editText.selectionEnd
            val spannable = editText.text as Spannable
            spannable.setSpan(alignmentSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun openColorPickerToolBar() {
        // Open the color picker dialog
        val colorPickerDialog = ColorPickerDialog(requireContext()) { selectedColor ->
            // When a color is selected, apply the selected color to the text in EditText
            applyTextColor(selectedColor)
        }
        colorPickerDialog.show()
    }

    private fun applyTextColor(color: Int) {
        // Get the start and end positions of the selected text in EditText
        val start = editText.selectionStart
        val end = editText.selectionEnd

        // Check if there is a valid text selection
        if (start < end) {
            val spannable = editText.text as Spannable
            // Apply the selected color to the selected text
            spannable.setSpan(
                ForegroundColorSpan(color),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
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
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2) // 3 columns

        // List of items to display in the popup
        val items = listOf("Family", "Family", "Family", "Family", "Music", "Family")

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_16dp)
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2, spacingInPixels, true))

// Adapter
        recyclerView.adapter = PopupAdapter(items) { selectedItem ->
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

class GridSpacingItemDecoration(
    private val spanCount: Int,    // Số cột trong GridLayout
    private val spacing: Int,      // Kích thước khoảng cách (px)
    private val includeEdge: Boolean // Bao gồm khoảng cách ở cạnh ngoài hay không
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view) // Vị trí của item
        val column = position % spanCount                  // Cột của item

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) { // Các item ở hàng đầu tiên
                outRect.top = spacing
            }
            outRect.bottom = spacing // Khoảng cách dưới mỗi item
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing // Khoảng cách trên mỗi item (không bao gồm cạnh ngoài)
            }
        }
    }
}

fun getTextFromFirebase(userId: String, entryId: String, callback: FirebaseCallback) {
    // Tham chiếu đến Firebase Database
    val database = FirebaseDatabase.getInstance()
    val textRef = database.getReference("users/$userId/entries/$entryId/text")

    // Đọc dữ liệu từ Firebase
    textRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                val text = dataSnapshot.getValue(String::class.java) // Lấy giá trị dạng String
                callback.onSuccess(text) // Gọi callback khi đọc dữ liệu thành công
            } else {
                callback.onFailure("Text not found!")
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            callback.onFailure(databaseError.message)
        }
    })
}

fun saveTextToFirebase(userId: String, entryId: String, text: String?, callback: FirebaseCallback) {
    val database = FirebaseDatabase.getInstance()
    val textRef = database.getReference("users/$userId/entries/$entryId/text")

    textRef.setValue(text).addOnCompleteListener { task: Task<Void?> ->
        if (task.isSuccessful) {
            callback.onSuccess(text)
        } else {
            callback.onFailure(task.exception!!.message)
        }
    }
}

interface FirebaseCallback {
    fun onSuccess(text: String?) // Hàm được gọi khi dữ liệu được lấy thành công
    fun onFailure(errorMessage: String?) // Hàm được gọi khi có lỗi xảy ra
}

