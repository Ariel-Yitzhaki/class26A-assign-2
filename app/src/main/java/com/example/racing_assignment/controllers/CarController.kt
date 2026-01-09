package com.example.racing_assignment.controllers

import android.view.View
import android.widget.ImageView

class CarController(
    private val car1: ImageView,
    private val car2: ImageView,
    private val car3: ImageView,
    private val car4: ImageView,
    private val car5: ImageView
) {
    private val cars = arrayOf(car1, car2, car3, car4, car5)

    fun moveCar(fromLane: Int, toLane: Int) {
        cars[fromLane].visibility = View.INVISIBLE
        cars[toLane].visibility = View.VISIBLE
    }

    fun showCar(lane: Int) {
        cars.forEachIndexed { index, car ->
            car.visibility = if (index == lane) View.VISIBLE else View.INVISIBLE
        }
    }
}