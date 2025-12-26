package com.example.racing_assignment

import android.view.View
import android.widget.ImageView

class LivesController(
    lives1: ImageView,
    lives2: ImageView,
    lives3: ImageView
) {
    private val lives = arrayOf(lives1, lives2, lives3)

    fun removeLives(index: Int) {
        if (index >= 0) {
            lives[index].visibility = View.INVISIBLE
        }
    }

    fun resetLives() {
        lives[0].visibility = View.VISIBLE
        lives[1].visibility = View.VISIBLE
        lives[2].visibility = View.VISIBLE
    }

}