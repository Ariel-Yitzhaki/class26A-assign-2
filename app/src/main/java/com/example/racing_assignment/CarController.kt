package com.example.racing_assignment

import android.view.View
import android.widget.ImageView

class CarController(
    car1: ImageView,
    car2: ImageView,
    car3: ImageView,
    car4: ImageView,
    car5: ImageView
) {
    private val cars = arrayOf(car1, car2, car3, car4, car5)

    fun moveCar(currentIndex: Int, newIndex: Int) {
        cars[currentIndex].visibility = View.INVISIBLE
        cars[newIndex].visibility = View.VISIBLE
    }
}