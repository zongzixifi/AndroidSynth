package com.example.project2.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey


@Entity(
    tableName = "music_generated",
    foreignKeys = [ForeignKey(
        entity = Session::class,
        parentColumns = ["id"],
        childColumns = ["session_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MusicGenerated(
    @PrimaryKey(autoGenerate = true) val music_id: Int,
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "URL") val url: String,
    @ColumnInfo(name = "prompt") val prompt: String
)
