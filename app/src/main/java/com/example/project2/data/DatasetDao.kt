package com.example.project2.data

import com.example.project2.data.database.Dialogue
import com.example.project2.data.database.MidiFile
import com.example.project2.data.database.MusicGenerated
import com.example.project2.data.database.Session
import com.example.project2.data.database.User
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM User")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM user WHERE username = :username AND password = :password LIMIT 1")
    suspend fun checkUser(username: String, password: String): User?
}

@Dao
interface SessionDao {
    // 初始化插入一个会话（如果有冲突则替换）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session): Long

    // 更新 title
    @Query("UPDATE sessions SET title = :newTitle WHERE id = :sessionId")
    suspend fun updateTitle(sessionId: String, newTitle: String)

    // 更新 last_used_time（true/false）
    @Query("UPDATE sessions SET last_used_time = :lastUsed WHERE id = :sessionId")
    suspend fun updateLastUsedTime(sessionId: String, lastUsed: Long)

    // 可选：获取某用户的全部会话（按照插入顺序）
    @Query("SELECT * FROM sessions WHERE user_id = :userId ORDER BY rowid DESC")
    suspend fun getSessionsForUser(userId: Int): List<Session>
}

@Dao
interface DialogueDao {
    // 插入一条对话记录
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDialogue(dialogue: Dialogue)

    // 批量插入
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDialogues(dialogues: List<Dialogue>)

    // 获取某个 session 的所有对话记录，按时间升序排序
    @Query("SELECT * FROM dialogues WHERE sessions_id = :sessionId ORDER BY timestamp ASC")
    fun getDialoguesForSession(sessionId: String): LiveData<List<Dialogue>>

    // 删除某个 session 的全部对话
    @Query("DELETE FROM dialogues WHERE sessions_id = :sessionId")
    suspend fun deleteDialoguesBySession(sessionId: String)

    // 删除所有对话（调试或重置用）
    @Query("DELETE FROM dialogues")
    suspend fun deleteAllDialogues()
}

@Dao
interface MidiFileDao {

    // 插入一条 MIDI 记录
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMidiFile(midiFile: MidiFile)

    // 获取某用户所有 MIDI 文件（用于个人创作历史）
    @Query("SELECT * FROM midi_files WHERE user_id = :userId ORDER BY created_at DESC")
    fun getMidiFilesByUser(userId: Int): LiveData<List<MidiFile>>

    // 获取某 session 下的所有 MIDI 文件（用于会话回溯）
    @Query("SELECT * FROM midi_files WHERE session_id = :sessionId ORDER BY created_at DESC")
    fun getMidiFilesBySession(sessionId: String): LiveData<List<MidiFile>>

    // 根据主键 ID 获取单个 MIDI 文件（用于详情页或播放）
    @Query("SELECT * FROM midi_files WHERE id = :id")
    suspend fun getMidiFileById(id: Int): MidiFile?

    // 删除某个 MIDI 文件
    @Delete
    suspend fun deleteMidiFile(midiFile: MidiFile)

    // 可选：删除某 session 的全部 MIDI 文件
    @Query("DELETE FROM midi_files WHERE session_id = :sessionId")
    suspend fun deleteMidiFilesBySession(sessionId: String)
}

@Dao
interface MusicGeneratedDao {

    // 插入一条生成的音乐记录
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMusic(music: MusicGenerated)

    // 查询某个会话下生成的全部音乐记录（按插入顺序倒序）
    @Query("SELECT * FROM music_generated WHERE session_id = :sessionId ORDER BY music_id DESC")
    fun getMusicBySession(sessionId: String): LiveData<List<MusicGenerated>>

    // 获取某条音乐生成记录的详情
    @Query("SELECT * FROM music_generated WHERE music_id = :musicId")
    suspend fun getMusicById(musicId: Int): MusicGenerated?

    // 删除某个 session 下生成的所有音乐记录
    @Query("DELETE FROM music_generated WHERE session_id = :sessionId")
    suspend fun deleteMusicBySession(sessionId: String)

    // 可选：删除一条生成记录（按对象）
    @Delete
    suspend fun deleteMusic(music: MusicGenerated)
}