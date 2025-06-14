package com.example.project2.data


import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.project2.data.database.Dialogue
import com.example.project2.data.database.MidiFile
import com.example.project2.data.database.MusicGenerated
import com.example.project2.data.database.Session
import com.example.project2.data.database.User

@Database(
    entities = [
        User::class,
        Session::class,
        Dialogue::class,
        MidiFile::class,
        MusicGenerated::class
    ],
    version = 4,
    exportSchema = false
)
abstract class DataBaseClass : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun dialogueDao(): DialogueDao
    abstract fun midiFileDao(): MidiFileDao
    abstract fun musicGeneratedDao(): MusicGeneratedDao
}
