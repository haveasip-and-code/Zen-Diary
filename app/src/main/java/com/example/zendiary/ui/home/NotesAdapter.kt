package com.example.zendiary.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zendiary.R
import com.example.zendiary.utils.Note

class NotesAdapter(
    private val notes: List<Note>,
    private val onNoteClicked: (Note) -> Unit,
    private val onLongClick: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val header: TextView = itemView.findViewById(R.id.note_header)
        val previewText: TextView = itemView.findViewById(R.id.note_preview_text)
        val date: TextView = itemView.findViewById(R.id.note_date)
        val noteImage: ImageView = itemView.findViewById(R.id.note_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note_preview, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = notes[position]

        holder.header.text = currentNote.header ?: "No Header"
        holder.previewText.text = currentNote.previewText ?: "No Preview"
        holder.date.text = currentNote.date ?: "No Date"

        holder.itemView.setOnClickListener {
            onNoteClicked(currentNote)
        }
        holder.itemView.setOnLongClickListener {
            onLongClick(currentNote) // Trigger long-click callback
            true // Indicate the event was handled
        }
    }

    override fun getItemCount(): Int = notes.size
}
