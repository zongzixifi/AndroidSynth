package com.example.project2.SynthPage

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MusicViewModel : ViewModel() {
    private val _musicInfo = MutableStateFlow(BasicMusicInfo(120, 4, 4, "C", "major"))
    val musicInfo: StateFlow<BasicMusicInfo> = _musicInfo

    fun updateBPM(newBPM: Int) {
        _musicInfo.update { it.copy(BPM = newBPM) }
    }

    fun updateBar(newBar: Int) {
        _musicInfo.update { it.copy(bar = newBar) }
    }

    fun updateClap(newClap: Int) {
        _musicInfo.update { it.copy(clap = newClap) }
    }

    fun updateRoot(newRoot: String) {
        _musicInfo.update { it.copy(root = newRoot) }
    }

    fun updateScale(newScale: String) {
        _musicInfo.update { it.copy(scale = newScale) }
    }

    fun resetMusicInfo() {
        _musicInfo.value = BasicMusicInfo(120, 4, 4, "C", "major")
    }

}