package com.example.project2.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "dialogues",
    foreignKeys = [ForeignKey(
        entity = Session::class,
        parentColumns = ["id"],
        childColumns = ["sessions_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Dialogue(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "sessions_id") val sessionId: String,
    @ColumnInfo(name = "role") val role: String,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)
