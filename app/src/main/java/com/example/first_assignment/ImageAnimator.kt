package com.example.first_assignment

import android.os.Looper
import android.view.View
import android.widget.ImageView


class ImageAnimator(
    private val image1: ImageView,
    private val image2: ImageView,
    private val image3: ImageView,
    private val image4: ImageView
) {
    private var currentIndex = 0
    //looper ensures the handler runs on the main thread
    private val handler = android.os.Handler(Looper.getMainLooper())
    private var isRunning = false

    fun start() {
        if (!isRunning) {
            isRunning = true
            animate()
        }
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun animate() {
        if (!isRunning) return

        image1.visibility = View.INVISIBLE
        image2.visibility = View.INVISIBLE
        image3.visibility = View.INVISIBLE
        image4.visibility = View.INVISIBLE

        when(currentIndex) {
            0 -> image1.visibility = View.VISIBLE
            1 -> image2.visibility = View.VISIBLE
            2 -> image3.visibility = View.VISIBLE
            3 -> image4.visibility = View.VISIBLE
        }

        currentIndex = (currentIndex + 1) % 4
        //delay the animation by 500ms
        handler.postDelayed({ animate() }, 500)
    }
}