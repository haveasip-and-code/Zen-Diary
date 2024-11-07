package com.example.zendiary.ui.journal

import androidx.fragment.app.viewModels
<<<<<<< HEAD
import android.os.Bundle
=======
>>>>>>> khanh
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zendiary.R
<<<<<<< HEAD
=======
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.style.StyleSpan
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
>>>>>>> khanh

class JournalFragment : Fragment() {

    companion object {
        fun newInstance() = JournalFragment()
    }

    private val viewModel: JournalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

<<<<<<< HEAD
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_journal, container, false)
    }
}
=======
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

        return view
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
}
>>>>>>> khanh
