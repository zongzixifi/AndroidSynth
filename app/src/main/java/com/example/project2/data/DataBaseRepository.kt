package com.example.project2.data

import com.example.project2.data.database.Dialogue
import com.example.project2.data.database.MidiFile
import com.example.project2.data.database.MusicGenerated
import com.example.project2.data.database.Session
import com.example.project2.data.database.User
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepository @Inject constructor(private val userDao: UserDao) {

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    val allUsers: LiveData<List<User>> = userDao.getAllUsers()

    suspend fun checkUser(username: String, password: String): User? {
        return userDao.checkUser(username, password)
    }
}

class SessionRepository @Inject constructor(private val sessionDao: SessionDao) {

    suspend fun insertSession(session: Session): Long {
        return sessionDao.insertSession(session)
    }

    suspend fun updateSessionTitle(sessionId: Int, newTitle: String) {
        sessionDao.updateTitle(sessionId, newTitle)
    }

    suspend fun updateSessionLastUsed(sessionId: Int, time: Long) {
        sessionDao.updateLastUsedTime(sessionId, time)
    }

    suspend fun getSessionsByUser(userId: Int): List<Session> =
        sessionDao.getSessionsForUser(userId)
}

class DialogueRepository @Inject constructor(private val dialogueDao: DialogueDao) {

    suspend fun insertDialogue(dialogue: Dialogue) {
        dialogueDao.insertDialogue(dialogue)
    }

    suspend fun insertDialogues(dialogues: List<Dialogue>) {
        dialogueDao.insertDialogues(dialogues)
    }

    suspend fun deleteDialoguesBySession(sessionId: Int) {
        dialogueDao.deleteDialoguesBySession(sessionId)
    }

    suspend fun deleteAllDialogues() {
        dialogueDao.deleteAllDialogues()
    }

    fun getDialoguesBySession(sessionId: Int): Flow<List<Dialogue>> =
        dialogueDao.getDialoguesForSessionAsFlow(sessionId)

    // 添加获取最新对话的方法
    suspend fun getLatestDialogue(sessionId: Int): Dialogue? =
        dialogueDao.getLatestDialogue(sessionId)
}

class MidiFileRepository @Inject constructor(private val midiFileDao: MidiFileDao) {

    suspend fun insertMidiFile(file: MidiFile) {
        midiFileDao.insertMidiFile(file)
    }

    fun getMidiFilesByUser(userId: Int): Flow<List<MidiFile>> =
        midiFileDao.getMidiFilesByUser(userId)

    fun getMidiFilesBySession(sessionId: Int): Flow<List<MidiFile>> =
        midiFileDao.getMidiFilesBySession(sessionId)

    suspend fun getMidiFileById(id: Int): MidiFile? {
        return midiFileDao.getMidiFileById(id)
    }

    suspend fun deleteMidiFile(midiFile: MidiFile) {
        midiFileDao.deleteMidiFile(midiFile)
    }

    suspend fun deleteMidiFilesBySession(sessionId: Int) {
        midiFileDao.deleteMidiFilesBySession(sessionId)
    }
}


class MusicGeneratedRepository @Inject constructor(private val musicGeneratedDao: MusicGeneratedDao) {

    suspend fun insertGeneratedMusic(music: MusicGenerated) {
        musicGeneratedDao.insertMusic(music)
    }

    fun getGeneratedMusicBySession(sessionId: Int): Flow<List<MusicGenerated>> =
        musicGeneratedDao.getMusicBySession(sessionId)

    suspend fun getMusicById(musicId: Int): MusicGenerated? {
        return musicGeneratedDao.getMusicById(musicId)
    }

    suspend fun deleteMusicBySession(sessionId: Int) {
        musicGeneratedDao.deleteMusicBySession(sessionId)
    }

    suspend fun deleteMusic(music: MusicGenerated) {
        musicGeneratedDao.deleteMusic(music)
    }
}