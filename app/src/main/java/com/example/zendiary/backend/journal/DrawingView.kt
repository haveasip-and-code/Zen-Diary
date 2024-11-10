package com.example.zendiary.backend.journal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paths = mutableListOf<Path>()  // To store all the drawn paths
    private val paints = mutableListOf<Paint>()  // To store the paint for each path

    private val path = Path()
    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    private var brushType: String = "Round" // Default brush type

    init {
        // Make the background transparent
        setBackgroundColor(Color.TRANSPARENT)
    }

    // New fields for customizable color and eraser mode
    private var isEraserMode = false
    private var brushColor = Color.BLACK
    private var brushWidth = 8f
    private var eraseRadius = 50f  // Define the size of the eraser

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw all paths
        for (i in paths.indices) {
            canvas.drawPath(paths[i], paints[i])
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isEraserMode) {
                    // Erase if eraser mode is enabled
                    erase(x, y)
                } else {
                    // Start a new drawing path
                    path.moveTo(x, y)
                }
                performClick()  // Handle accessibility
            }
            MotionEvent.ACTION_MOVE -> {
                if (isEraserMode) {
                    // Erase while moving if eraser mode is enabled
                    erase(x, y)
                } else {
                    // Continue drawing the current path
                    path.lineTo(x, y)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isEraserMode) {
                    // Save the path when finished drawing
                    paths.add(path)  // Add the path to the list
                    paints.add(Paint(paint))  // Add the associated paint
                    // path.reset()  // Reset for new path
                }
            }
        }
        invalidate()  // Redraw the view after touch
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        // Additional click logic can go here if needed
        return true
    }

    // New method to clear the drawing
    fun clearDrawing() {
        paths.clear()  // Clear all stored paths
        paints.clear()  // Clear all stored paints
        invalidate()  // Redraw
    }

    // New method to set brush color
    fun setBrushColor(color: Int) {
        brushColor = color
        if (!isEraserMode) {
            paint.color = brushColor
        }
    }

    // Method to update the brush type (e.g., Round, Square, Custom)
    fun setBrushType(type: String) {
        brushType = type
        when (brushType) {
            "Round" -> paint.strokeCap = Paint.Cap.ROUND
            "Square" -> paint.strokeCap = Paint.Cap.SQUARE
            "Custom" -> paint.strokeCap = Paint.Cap.ROUND // You can change this for custom brushes
        }
    }
    // New method to set brush width
    fun setBrushWidth(width: Float) {
        brushWidth = width
        paint.strokeWidth = brushWidth
    }

    // Method to toggle eraser mode
    fun enableEraserMode(enable: Boolean) {
        isEraserMode = enable
        if (isEraserMode) {
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)  // Set eraser mode (transparent)
        } else {
            paint.xfermode = null  // Revert to normal drawing
        }
    }

    // Method to erase paths under the touch point
    private fun erase(x: Float, y: Float) {
        val eraseRect = RectF(x - eraseRadius, y - eraseRadius, x + eraseRadius, y + eraseRadius)

        // Iterate over all paths and erase parts that intersect with the erase area
        val pathsToRemove = mutableListOf<Int>()
        for (i in paths.indices) {
            val pathBounds = RectF()
            paths[i].computeBounds(pathBounds, true)

            // If the erase area intersects the path, mark it for removal
            if (RectF.intersects(pathBounds, eraseRect)) {
                pathsToRemove.add(i)
            }
        }

        // Remove the paths marked for erasure
        for (i in pathsToRemove.reversed()) {
            paths.removeAt(i)
            paints.removeAt(i)
        }
    }
}

