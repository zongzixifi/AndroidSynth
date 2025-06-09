package com.example.project2.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.sql.Timestamp

@Entity(
    tableName = "sessions",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "last_used_time") val lastUsedTime: Long,
    @ColumnInfo(name = "title") val title: String
)
