package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import java.io.File
import java.io.FileOutputStream

object AudioPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null
    var activePlaying: Boolean = false

    fun playAudioBytes(context: Context, audioBytes: ByteArray, onComplete: () -> Unit = {}) {
        stopAudio()
        try {
            val tempFile = File(context.cacheDir, "temp_habit_audio_${System.currentTimeMillis()}.mp3")
            FileOutputStream(tempFile).use { it.write(audioBytes) }

            val player = MediaPlayer()
            player.setDataSource(tempFile.absolutePath)
            player.prepare()
            player.setOnCompletionListener {
                activePlaying = false
                onComplete()
            }
            player.start()
            mediaPlayer = player
            activePlaying = true
        } catch (_: Exception) {
            activePlaying = false
        }
    }

    fun playBase64Audio(context: Context, base64Audio: String, onComplete: () -> Unit = {}) {
        try {
            val bytes = Base64.decode(base64Audio, Base64.DEFAULT)
            playAudioBytes(context, bytes, onComplete)
        } catch (_: Exception) {
            activePlaying = false
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        activePlaying = false
    }
}
