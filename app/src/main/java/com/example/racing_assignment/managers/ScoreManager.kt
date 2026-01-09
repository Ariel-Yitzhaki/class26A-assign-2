package com.example.racing_assignment.managers

import android.content.Context
import androidx.core.content.edit

class ScoreManager(context: Context) {

    private val prefs = context.getSharedPreferences("records", Context.MODE_PRIVATE)

    fun saveScore(score: Int, latitude: Double, longitude: Double) {
        for (i in 1..10) {
            val existingScore = prefs.getInt("record$i", 0)

            if (score > existingScore) {
                // Shift lower scores down
                for (j in 10 downTo i + 1) {
                    val scoreToMove = prefs.getInt("record${j - 1}", 0)
                    val latToMove = prefs.getFloat("lat${j - 1}", 0f)
                    val lonToMove = prefs.getFloat("lon${j - 1}", 0f)
                    prefs.edit {
                        putInt("record$j", scoreToMove)
                        putFloat("lat$j", latToMove)
                        putFloat("lon$j", lonToMove)
                    }
                }
                // Insert new score
                prefs.edit {
                    putInt("record$i", score)
                    putFloat("lat$i", latitude.toFloat())
                    putFloat("lon$i", longitude.toFloat())
                }
                break
            }
        }
    }

    fun getScore(position: Int): Int {
        return prefs.getInt("record$position", 0)
    }

    fun getLatitude(position: Int): Float {
        return prefs.getFloat("lat$position", 0f)
    }

    fun getLongitude(position: Int): Float {
        return prefs.getFloat("lon$position", 0f)
    }
}