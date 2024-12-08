package com.example.zendiary.ui.home

import android.os.Parcel
import android.os.Parcelable

data class Note(
    val header: String,
    val previewText: String,
    val date: String,
    val imageUrl: String? = null,
    val entryId: String // Add this field for the entry ID
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(header)
        parcel.writeString(previewText)
        parcel.writeString(date)
        parcel.writeString(imageUrl)
        parcel.writeString(entryId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Note> {
        override fun createFromParcel(parcel: Parcel): Note {
            return Note(parcel)
        }

        override fun newArray(size: Int): Array<Note?> {
            return arrayOfNulls(size)
        }
    }
}