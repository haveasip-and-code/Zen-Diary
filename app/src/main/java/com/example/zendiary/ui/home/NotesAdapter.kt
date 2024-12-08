package com.example.zendiary.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zendiary.R

class NotesAdapter(private val notes: List<Note>) :
    RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val header: TextView = itemView.findViewById(R.id.note_header) // Add header reference
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
        holder.header.text = currentNote.header // Set header text
        holder.previewText.text = currentNote.previewText
        holder.date.text = currentNote.date

        // Show or hide the image based on whether it's available
        if (currentNote.imageUrl.isNullOrEmpty()) {
            holder.noteImage.visibility = View.GONE
        } else {
            holder.noteImage.visibility = View.VISIBLE
            // Load the image (use a library like Glide or Coil for loading images)
            // Example using Glide:
            // Glide.with(holder.itemView.context).load(note.imageUrl).into(holder.noteImage)
        }
    }

    override fun getItemCount(): Int = notes.size
}