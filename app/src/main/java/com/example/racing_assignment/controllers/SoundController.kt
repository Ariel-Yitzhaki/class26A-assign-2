package com.example.racing_assignment.controllers

import android.content.Context
import android.media.SoundPool
import com.example.racing_assignment.R

class SoundController(context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .build()

    private val coinSound: Int = soundPool.load(context, R.raw.coin_collect, 1)
    private val bombSound: Int = soundPool.load(context, R.raw.bomb_hit, 1)

    fun playCoinSound() {
        soundPool.play(coinSound, 1f, 1f, 1, 0, 1f)
    }

    fun playBombSound() {
        soundPool.play(bombSound, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}