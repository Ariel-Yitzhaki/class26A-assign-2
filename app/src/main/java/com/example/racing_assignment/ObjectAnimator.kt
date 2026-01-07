package com.example.racing_assignment

import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView

class ObjectAnimator(
    private val bombs: Array<ImageView>,
    private val coins: Array<ImageView>,
    private val isCoin: Boolean,
    private val getSpeed: () -> Long,
    private val onReachedBottom: () -> Unit,
    private val onFinished: () -> Unit
) {
    private var currentRow = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    fun start() {
        currentRow = 0
        isRunning = true
        animateStep()
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        hideAll()
    }

    private fun animateStep() {
        if (!isRunning) {
            hideAll()
            return
        }

        // Hide previous row
        if (currentRow > 0) {
            if (isCoin) {
                coins[currentRow - 1].visibility = View.INVISIBLE
            } else {
                bombs[currentRow - 1].visibility = View.INVISIBLE
            }
        }

        // Check if finished all rows
        if (currentRow >= 6) {
            onFinished()
            return
        }

        // Show current row
        if (isCoin) {
            coins[currentRow].visibility = View.VISIBLE
        } else {
            bombs[currentRow].visibility = View.VISIBLE
        }

        // Check if reached bottom (row 5 = index 5 = car row)
        if (currentRow == 5) {
            onReachedBottom()
        }

        currentRow++

        val delay = getSpeed() / 6
        handler.postDelayed({ animateStep() }, delay)
    }

    private fun hideAll() {
        bombs.forEach { it.visibility = View.INVISIBLE }
        coins.forEach { it.visibility = View.INVISIBLE }
    }
}