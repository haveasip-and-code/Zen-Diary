package com.example.zendiary.ui.journal

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
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
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zendiary.R
import com.example.zendiary.backend.journal.ColorPickerDialog
import com.example.zendiary.backend.journal.DrawingView
import com.example.zendiary.backend.journal.ImagePickerBottomSheet
import com.example.zendiary.backend.journal.PenToolBottomSheetDialog
import com.example.zendiary.utils.Note
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.properties.Delegates
import com.example.zendiary.Global
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class JournalFragment : Fragment(), ImagePickerBottomSheet.OnImageOptionSelectedListener {

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

    private val cameraPermissionRequestCode = 1001
    private val cameraRequest = 101
    private val pickImageRequest = 102

    private var selectedImageUri: Uri? = null

    private lateinit var popupWindow: PopupWindow
    private lateinit var editHeaderText: EditText
    private lateinit var editText: EditText

    private var userId = Global.userId
    private var entryId: String? = null

    private lateinit var headerEntry: String
    private lateinit var journalText: String

    private var sentimentScore by Delegates.notNull<Float>()
    private lateinit var sentimentLabel: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize the ViewModel
        viewModel = ViewModelProvider(this)[JournalViewModel::class.java]

        // Declare a nullable Note object
        var note: Note? = null

        // Retrieve the Note from the arguments safely
        arguments?.let {
            note = it.getParcelable("note")
        }

        // If it's a new entry, generate the entryId
        if (note == null) {
            if (userId?.isNotEmpty() == true) {
                viewModel.generateNewEntryId(userId!!)
            } else {
                Log.e("JournalFragment", "UserId is missing! Cannot generate entryId.")
            }
        } else {
            Log.d("JournalFragment", "Editing existing entry with ID: ${note!!.entryId}")
            entryId = note!!.entryId
        }

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_journal, container, false)
        editHeaderText = view.findViewById(R.id.et_title)
        editText = view.findViewById(R.id.et_content)

        // Initialize the toolbar popup
        initToolbarPopup()

        // Set up EditText selection listener
        setupEditTextSelectionListener()

        // Initialize views
        initViews(view)

        // Set up button click listeners
        setupButtonListeners(view)

        // Observe the entryId LiveData
        viewModel.entryId.observe(viewLifecycleOwner) { newEntryId ->
            // Use the newEntryId for your logic
            entryId = newEntryId
        }

        // Observe sentiment results from the ViewModel
        observeSentimentResult()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the navigation drawer from the parent activity
        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawerLayout != null) {
            view.findViewById<ImageButton>(R.id.ib_drawer_nav).setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        } else {
            Log.e("JournalFragment", "DrawerLayout is null")
        }

        // Check if the popup has been displayed using SharedPreferences
        view.post {
            // Uncomment and customize the showPopup function if needed
            // showPopup()
            savePopupState(false)
        }

        val themesAndStickerView = requireActivity().findViewById<View>(R.id.rl_themes_and_sticker)
        if (themesAndStickerView == null) {
            Log.e("JournalFragment", "rl_themes_and_sticker not found in layout!")
        }

        // Handle click on the relative view to navigate to the StoreFragment
        requireActivity().findViewById<View>(R.id.ib_theme)?.setOnClickListener {
            Log.e("JournalFragment", "Going to Store")
            findNavController().navigate(R.id.action_journalFragment_to_storeFragment)
        }


        userId?.let {
            entryId?.let { it1 ->
                getTextFromFirebase(it, it1, object : FirebaseCallback<Pair<String?, String?>> {
                    override fun onSuccess(result: String?) {
                        // Parsing the Pair back from the String (if needed)
                        val pair = result
                            ?.removePrefix("(") // Remove the "(" prefix
                            ?.removeSuffix(")")    // Remove the ")" suffix
                            ?.split(", ")          // Split by ", "

                        // Extract text and headerEntry
                        val text = pair?.getOrNull(0) // First value
                        val headerEntry = pair?.getOrNull(1) // Second value
                        // Set the values in the EditTexts
                        editText.setText(text ?: "") // Set the extracted text or an empty string if null
                        editHeaderText.setText(headerEntry ?: "") // Set the extracted header or an empty string if null
                        Log.d("Firebase", "Text: $text, HeaderEntry: $headerEntry")
                    }

                    override fun onFailure(errorMessage: String?) {
                        Log.e("Firebase", "Error: $errorMessage")
                    }
                })

            }
        }
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

        view.findViewById<Button>(R.id.btn_back).setOnClickListener{
            findNavController().navigateUp()
        }

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

//        view.findViewById<ImageButton>(R.id.delete_btn).setOnClickListener {
//            showDeleteConfirmationDialog()
//        }

        view.findViewById<Button>(R.id.btn_save).setOnClickListener {
            headerEntry = editHeaderText.text.toString()
            journalText = editText.text.toString()

            if (journalText.isNotEmpty()) {
                // Trigger sentiment analysis
                viewModel.analyzeSentiment(journalText)

                // Observe sentiment result
                viewModel.sentimentResult.observe(viewLifecycleOwner) { result ->
                    if (result != null) {
                        sentimentScore = result["compound"] ?: 0f
                        showSentimentDialog(sentimentScore)

                        sentimentLabel = when {
                            sentimentScore > 0.1 -> "positive"
                            sentimentScore < -0.1 -> "negative"
                            else -> "neutral"
                        }

                        Toast.makeText(requireContext(), "Sentiment analysis completed: $sentimentLabel", Toast.LENGTH_SHORT).show()

                        // Perform any additional actions with sentimentScore or sentimentLabel
                    } else {
                        Toast.makeText(requireContext(), "Failed to analyze sentiment", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please enter text to analyze", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun showSentimentDialog(sentimentScore: Float) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sentiment_result, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Bind views
        val ivSentimentIcon = dialogView.findViewById<ImageView>(R.id.ivSentimentIcon)
        val tvSentimentResult = dialogView.findViewById<TextView>(R.id.tvSentimentResult)
        val tvSentimentScore = dialogView.findViewById<TextView>(R.id.tvSentimentScore)
        val tvNotes = dialogView.findViewById<TextView>(R.id.tvNotes)
        val tvNegativeEntry = dialogView.findViewById<TextView>(R.id.tvNegativeEntry)
        val btnHome = dialogView.findViewById<Button>(R.id.btnHome)
        val btnKeep = dialogView.findViewById<Button>(R.id.btnKeep)
        val btnDelete = dialogView.findViewById<Button>(R.id.btnDelete)

        // Set sentiment score
        tvSentimentScore.text = sentimentScore.toString()

        // Update UI based on sentiment result
        when {
            sentimentScore < -0.1 -> {
                ivSentimentIcon.setImageResource(R.drawable.ic_sentiment_negative)
                tvSentimentResult.text = "Negative"
                tvNotes.text = "It seems like you're feeling down."
                btnHome.visibility = View.GONE
                tvNegativeEntry.visibility = View.VISIBLE
                btnKeep.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE
            }
            sentimentScore > 0.1 -> {
                ivSentimentIcon.setImageResource(R.drawable.ic_sentiment_positive)
                tvSentimentResult.text = "Positive"
                tvNotes.text = "Your entry is positive. Keep up the good work!"
                btnHome.visibility = View.VISIBLE
                tvNegativeEntry.visibility = View.GONE
                btnKeep.visibility = View.GONE
                btnDelete.visibility = View.GONE
            }
            else -> {
                ivSentimentIcon.setImageResource(R.drawable.ic_sentiment_neutral)
                tvSentimentResult.text = "Neutral"
                tvNotes.text = "Your entry seems neutral. No action is needed."
                btnHome.visibility = View.VISIBLE
                tvNegativeEntry.visibility = View.GONE
                btnKeep.visibility = View.GONE
                btnDelete.visibility = View.GONE
            }
        }

        // Handle button clicks
        btnHome.setOnClickListener {
            saveJournalEntry()
            dialog.dismiss()

            // Navigate to the Home screen
            findNavController().navigate(R.id.navigation_home)
        }

        btnKeep.setOnClickListener {
            saveJournalEntry()
            dialog.dismiss()

            // Navigate to the Home screen
            findNavController().navigate(R.id.navigation_home)
        }

        btnDelete.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmationDialog()
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_delete, null)
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)

        btnCancel.setOnClickListener {
            // Dismiss the dialog
            alertDialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            // Delete the entry from Firebase
            deleteJournalEntry()
            alertDialog.dismiss()

            // Navigate to the Deletion Confirmation screen
            findNavController().navigate(R.id.deletionConfirmationFragment)
        }
        alertDialog.show()
    }

    private fun saveJournalEntry() {
        userId?.let {
            entryId?.let { it1 ->
                saveEntryToFirebase(
                    userId = it,
                    entryId = it1,
                    headerEntry = headerEntry,
                    text = journalText,
                    sentimentScore = sentimentScore,
                    sentimentLabel = sentimentLabel,
                    imageView = ivImagePreview,
                    object : FirebaseCallback<Any?> {
                        override fun onSuccess(result: String?) {
                            if (result != null) {
                                Log.d("Firebase", result)
                            }
                        }

                        override fun onFailure(errorMessage: String?) {
                            Log.e("Firebase", errorMessage ?: "Error occurred")
                        }
                    }
                )
            }
        }
        showToast("Journal entry saved!")
    }

    private fun deleteEntryFirebase() {
        userId?.let { user ->
            entryId?.let { entry ->
                val database = FirebaseDatabase.getInstance()
                val entryRef = database.getReference("users/$user/entries/$entry")

                // Delete the entry from Firebase
                entryRef.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Firebase", "Entry deleted successfully.")
                        showToast("Journal entry deleted!")
                    } else {
                        Log.e("Firebase", task.exception?.message ?: "Error occurred while deleting entry.")
                        showToast("Failed to delete entry.")
                    }
                }
            } ?: showToast("Entry ID is null.")
        } ?: showToast("User ID is null.")
    }


    private fun deleteJournalEntry() {
//        if (Global.isNewEntry == false) {
//            deleteEntryFirebase()
//        }
        showToast("Journal entry deleted!")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
        startActivityForResult(intent, pickImageRequest)
    }

    // This method is invoked when the camera option is selected
    override fun onCameraOptionSelected() {
        // Check camera permission first
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Open the camera
            openCamera()
        } else {
            // Request camera permission
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), cameraPermissionRequestCode)
        }
    }

    // This method opens the camera to take a picture
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, cameraRequest)
    }

    // Handle the result after image is picked
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                pickImageRequest -> {
                    selectedImageUri = data?.data
                    insertImageIntoPreview(selectedImageUri)
                }
                cameraRequest -> {
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

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.spacing_64dp)
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

fun getTextFromFirebase(
    userId: String,
    entryId: String,
    callback: FirebaseCallback<Pair<String?, String?>> // Returning both text and headerEntry as a Pair
) {
    // Reference to Firebase Database
    val database = FirebaseDatabase.getInstance()
    val entryRef = database.getReference("users/$userId/entries/$entryId")

    // Read data from Firebase
    entryRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                // Retrieve both `text` and `headerEntry`
                val text = dataSnapshot.child("text").getValue(String::class.java)
                val headerEntry = dataSnapshot.child("headerEntry").getValue(String::class.java)
                callback.onSuccess(Pair(text, headerEntry).toString()) // Return both values as a Pair
            } else {
                callback.onFailure("Entry not found!")
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            callback.onFailure(databaseError.message)
        }
    })
}

fun saveEntryToFirebase(
    userId: String,
    entryId: String,
    headerEntry: String?,
    text: String?,
    sentimentScore: Float,
    sentimentLabel: String,
    imageView: ImageView?, // Nullable ImageView
    callback: FirebaseCallback<Any?>
) {
    val database = FirebaseDatabase.getInstance()
    val entryRef = database.getReference("users/$userId/entries/$entryId")

    // Get the current timestamp and format it
    val currentDateTime = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC") // Set time zone to UTC
    val formattedDate = dateFormat.format(currentDateTime)

    // Helper function to save data in Firebase
    fun saveDataToFirebase(imageUrl: String? = null) {
        // Create a map to hold the data
        val entryData = mapOf(
            "headerEntry" to headerEntry,
            "text" to text,
            "sentiment" to mapOf(
                "label" to sentimentLabel,
                "score" to sentimentScore
            ),
            "date" to formattedDate, // Save date as timestamp
            "image" to imageUrl // Save the image URL if available
        )

        // Save the entry data to Firebase Database
        entryRef.setValue(entryData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback.onSuccess("Entry saved successfully.")
            } else {
                callback.onFailure(task.exception?.message ?: "Unknown error")
            }
        }
    }

    // Check if the image is present
    if (imageView != null && imageView.drawable != null) {
        // Get Firebase Storage instance
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("users/$userId/entries/$entryId/image.jpg")

        // Prepare image for upload
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // Start the image upload
        imageRef.putBytes(data).addOnCompleteListener { uploadTask ->
            if (uploadTask.isSuccessful) {
                // Get the image download URL
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveDataToFirebase(uri.toString()) // Save data with the image URL
                }.addOnFailureListener {
                    callback.onFailure("Failed to get image URL: ${it.message}")
                }
            } else {
                callback.onFailure("Image upload failed: ${uploadTask.exception?.message ?: "Unknown error"}")
            }
        }
    } else {
        // If no image is present, save data without an image URL
        saveDataToFirebase()
    }
}




interface FirebaseCallback<T> {
    fun onSuccess(result: String?) // Hàm được gọi khi dữ liệu được lấy thành công
    fun onFailure(errorMessage: String?) // Hàm được gọi khi có lỗi xảy ra
}

