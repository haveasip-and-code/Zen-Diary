package com.example.zendiary.backend.journal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.example.zendiary.R

class PenToolBottomSheetDialog(
    private val onBrushSelected: (String, Float) -> Unit
) : DialogFragment() {

    private lateinit var radioGroupBrushes: RadioGroup
    private lateinit var seekBarBrushWidth: SeekBar
    private lateinit var btnApply: AppCompatButton
    private lateinit var tvBrushWidth: TextView

    private var selectedBrush = "Round"
    private var brushWidth = 8f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_pen_tool, container, false)

        // Initialize views
        radioGroupBrushes = view.findViewById(R.id.radio_group_brushes)
        seekBarBrushWidth = view.findViewById(R.id.seekBarBrushWidth)
        btnApply = view.findViewById(R.id.btn_apply)
        tvBrushWidth = view.findViewById(R.id.tv_brush_width)

        // Set default values
        seekBarBrushWidth.progress = brushWidth.toInt()

        // Brush selection
        radioGroupBrushes.setOnCheckedChangeListener { _, checkedId ->
            selectedBrush = when (checkedId) {
                R.id.rb_round_brush -> "Round"
                R.id.rb_square_brush -> "Square"
                R.id.rb_custom_brush -> "Custom"
                else -> "Round"
            }
        }

        // SeekBar listener for width adjustment
        seekBarBrushWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brushWidth = progress.toFloat()
                tvBrushWidth.text = "Adjust Brush Width: $brushWidth"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Apply button click listener
        btnApply.setOnClickListener {
            onBrushSelected(selectedBrush, brushWidth)
            dismiss() // Close the dialog
        }

        return view
    }
}
