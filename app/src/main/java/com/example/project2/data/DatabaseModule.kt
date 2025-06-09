package com.example.project2.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DataBaseClass {
        return Room.databaseBuilder(
                context,
                DataBaseClass::class.java,
                "harmonics_database"
            ).fallbackToDestructiveMigration(true).build()
    }
    @Singleton
    @Provides fun provideUserDao(db: DataBaseClass): UserDao = db.userDao()
    @Singleton
    @Provides fun provideSessionDao(db: DataBaseClass): SessionDao = db.sessionDao()
    @Singleton
    @Provides fun provideDialogueDao(db: DataBaseClass): DialogueDao = db.dialogueDao()
    @Singleton
    @Provides fun provideMidiFileDao(db: DataBaseClass): MidiFileDao = db.midiFileDao()
    @Singleton
    @Provides fun provideMusicGeneratedDao(db: DataBaseClass): MusicGeneratedDao = db.musicGeneratedDao()
}