package com.example.project2.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "midi_files",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["user_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Session::class, parentColumns = ["id"], childColumns = ["session_id"], onDelete = ForeignKey.CASCADE)
    ]
)
data class MidiFile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "session_id") val sessionId: Int,
    @ColumnInfo(name = "midi_file_path") val midiFilePath: String,
    @ColumnInfo(name = "music_file_path") val musicFilePath: String,
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "scale") val scale: String,
    @ColumnInfo(name = "bpm") val bpm: Int,
    @ColumnInfo(name = "clap") val clap: Int,
    @ColumnInfo(name = "beats") val beats: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
