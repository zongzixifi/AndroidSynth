package com.example.project2.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "User",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "password") val password: String
)
