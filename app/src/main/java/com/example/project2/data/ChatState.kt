package com.example.project2.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.project2.data.database.Dialogue

@Entity
data class ChatItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val character: String,
    val chatText: String
)

data class ChatState(
    val currentSessionId: Int = 0,
    val dialogues: List<Dialogue> = emptyList(),
    val isGenerating: Boolean = false,
    val error: String? = null,
    val isLoading: Boolean = false
) {
    // 转换为ChatItem格式
    val chatItems: List<ChatItem>
        get() = dialogues.map { dialogue ->
            ChatItem(
                id = dialogue.id,
                character = dialogue.role,
                chatText = dialogue.message
            )
        }
}