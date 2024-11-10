package com.example.zendiary.backend.journal

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.example.zendiary.R

class ColorPickerDialog(context: Context, private val onColorPicked: (Int) -> Unit) : Dialog(context) {

    private lateinit var seekBarRed: SeekBar
    private lateinit var seekBarGreen: SeekBar
    private lateinit var seekBarBlue: SeekBar
    private lateinit var btnOk: Button
    private lateinit var tvColor: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.color_picker_dialog)

        // Initialize views
        seekBarRed = findViewById(R.id.seekBarRed)
        seekBarGreen = findViewById(R.id.seekBarGreen)
        seekBarBlue = findViewById(R.id.seekBarBlue)
        btnOk = findViewById(R.id.btn_ok)
        tvColor = findViewById(R.id.tv_color)

        // Set listeners to update color preview
        val colorPreview = Color.BLACK
        updateColorPreview(colorPreview)

        seekBarRed.setOnSeekBarChangeListener(createSeekBarListener())
        seekBarGreen.setOnSeekBarChangeListener(createSeekBarListener())
        seekBarBlue.setOnSeekBarChangeListener(createSeekBarListener())

        // OK button click listener to return the selected color
        btnOk.setOnClickListener {
            val color = Color.rgb(seekBarRed.progress, seekBarGreen.progress, seekBarBlue.progress)
            onColorPicked(color)
            dismiss() // Dismiss dialog after color is picked
        }
    }

    private fun updateColorPreview(color: Int) {
        tvColor.setBackgroundColor(color)
    }

    // Create SeekBar change listener for color changes
    private fun createSeekBarListener() = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            val color = Color.rgb(seekBarRed.progress, seekBarGreen.progress, seekBarBlue.progress)
            updateColorPreview(color)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }
}
