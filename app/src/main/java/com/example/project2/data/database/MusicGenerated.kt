package com.example.project2.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


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
    @PrimaryKey(autoGenerate = true) val music_id: Int = 0,
    @ColumnInfo(name = "session_id") val sessionId: Int,
    @ColumnInfo(name = "URL") val url: String,
    @ColumnInfo(name = "prompt") val prompt: String
)
