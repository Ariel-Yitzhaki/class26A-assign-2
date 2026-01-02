package com.example.racing_assignment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Button
import android.widget.Toast
import kotlin.arrayOf
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.bumptech.glide.Glide

class GameFragment : Fragment() {
    private lateinit var columns: Array<Array<ImageView>>
    private val handler = Handler(Looper.getMainLooper())
    private val animatingColumns = mutableSetOf<Int>()
    private var currentLane = 2
    private lateinit var carController: CarController
    private var canMove = true
    private lateinit var lives: LivesController
    private var liveCounter = 3
    private val bombsAtBottom = mutableSetOf<Int>()
    private var useButtons = true
    private var gameEnded = false

    // Sensor properties
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0] // Tilt left/right (-10 to 10)

            when {
                x < -3 && canMove && currentLane < 4 -> {
                    canMove = false
                    carController.moveCar(currentLane, currentLane + 1)
                    currentLane++
                    if (bombsAtBottom.contains(currentLane)) {
                        checkCollision(currentLane)
                    }
                    handler.postDelayed({ canMove = true }, 300)
                }
                x > 3 && canMove && currentLane > 0 -> {
                    canMove = false
                    carController.moveCar(currentLane, currentLane - 1)
                    currentLane--
                    if (bombsAtBottom.contains(currentLane)) {
                        checkCollision(currentLane)
                    }
                    handler.postDelayed({ canMove = true }, 300)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)

        Glide.with(this)
            .load(R.drawable.background_road)
            .into(view.findViewById(R.id.background_road))

        useButtons = arguments?.getBoolean("useButtons", true) ?: true

        initializeViews(view)
        if (useButtons) {
            setupButtons(view)
        } else {
            view.findViewById<Button>(R.id.leftArrow).visibility = View.GONE
            view.findViewById<Button>(R.id.rightArrow).visibility = View.GONE
            setupSensors()
        }


        handler.post(gameLoop)

        return view
    }

    override fun onResume() {
        super.onResume()
        if (!useButtons) {
            accelerometer?.let {
                sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (!useButtons) {
            sensorManager?.unregisterListener(sensorListener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null) // Stop the game loop
    }

    private fun initializeViews(view: View) {
        carController = CarController(
            view.findViewById(R.id.raceCar1_1),
            view.findViewById(R.id.raceCar1_2),
            view.findViewById(R.id.raceCar1_3),
            view.findViewById(R.id.raceCar1_4),
            view.findViewById(R.id.raceCar1_5)
        )

        columns = arrayOf(
            arrayOf(
                view.findViewById(R.id.bomb1_1),
                view.findViewById(R.id.bomb2_1),
                view.findViewById(R.id.bomb3_1),
                view.findViewById(R.id.bomb4_1),
                view.findViewById(R.id.bomb5_1),
                view.findViewById(R.id.bomb6_1)
            ),
            arrayOf(
                view.findViewById(R.id.bomb1_2),
                view.findViewById(R.id.bomb2_2),
                view.findViewById(R.id.bomb3_2),
                view.findViewById(R.id.bomb4_2),
                view.findViewById(R.id.bomb5_2),
                view.findViewById(R.id.bomb6_2)
            ),
            arrayOf(
                view.findViewById(R.id.bomb1_3),
                view.findViewById(R.id.bomb2_3),
                view.findViewById(R.id.bomb3_3),
                view.findViewById(R.id.bomb4_3),
                view.findViewById(R.id.bomb5_3),
                view.findViewById(R.id.bomb6_3)
            ),
            arrayOf(
                view.findViewById(R.id.bomb1_4),
                view.findViewById(R.id.bomb2_4),
                view.findViewById(R.id.bomb3_4),
                view.findViewById(R.id.bomb4_4),
                view.findViewById(R.id.bomb5_4),
                view.findViewById(R.id.bomb6_4)
            ),
            arrayOf(
                view.findViewById(R.id.bomb1_5),
                view.findViewById(R.id.bomb2_5),
                view.findViewById(R.id.bomb3_5),
                view.findViewById(R.id.bomb4_5),
                view.findViewById(R.id.bomb5_5),
                view.findViewById(R.id.bomb6_5)
            )
        )

        lives = LivesController(
            view.findViewById(R.id.heart1_3),
            view.findViewById(R.id.heart1_2),
            view.findViewById(R.id.heart1_1)
        )
    }

    private fun setupButtons(view: View) {
        val buttonLeft = view.findViewById<Button>(R.id.leftArrow)
        val buttonRight = view.findViewById<Button>(R.id.rightArrow)

        buttonLeft.setOnClickListener {
            if (canMove && currentLane > 0) {
                canMove = false
                carController.moveCar(currentLane, currentLane - 1)
                currentLane--

                if (bombsAtBottom.contains(currentLane)) {
                    checkCollision(currentLane)
                }
                handler.postDelayed({ canMove = true }, 100)
            }
        }

        buttonRight.setOnClickListener {
            if (canMove && currentLane < 4) {
                canMove = false
                carController.moveCar(currentLane, currentLane + 1)
                currentLane++

                if (bombsAtBottom.contains(currentLane)) {
                    checkCollision(currentLane)
                }
                handler.postDelayed({ canMove = true }, 100)
            }
        }
    }

    private fun setupSensors() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    @Suppress("DEPRECATION")
    private fun vibrate() {
        if (!isAdded) return
        val context = requireContext()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(500)
        }
    }

    private fun checkCollision(column: Int) {
        if (gameEnded || !isAdded) return
        if (column == currentLane) {
            lives.removeLives(liveCounter - 1)
            liveCounter--
            vibrate()
            if (liveCounter == 0) {
                Toast.makeText(requireContext(), "Game Over", Toast.LENGTH_SHORT).show()
                endGame()
            } else {
                Toast.makeText(requireContext(), "Watch Out!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun endGame() {
        if (gameEnded) return
        gameEnded = true
        handler.removeCallbacksAndMessages(null)
        parentFragmentManager.popBackStack()
    }

    private val gameLoop = object : Runnable {
        override fun run() {
            runColumn()
            handler.postDelayed(this, 1600)
        }
    }

    private fun runColumn() {
        val availableColumns = columns.indices.filter { it !in animatingColumns }
        if (availableColumns.isEmpty()) return

        val randomColumn = availableColumns.random()
        animatingColumns.add(randomColumn)
        val selectedColumn = columns[randomColumn]

        val animator = ImageAnimator(
            selectedColumn[0],
            selectedColumn[1],
            selectedColumn[2],
            selectedColumn[3],
            selectedColumn[4],
            selectedColumn[5]
        ) {
            bombsAtBottom.add(randomColumn)
            checkCollision(randomColumn)
            handler.postDelayed({
                bombsAtBottom.remove(randomColumn)
            }, 400)
        }
        animator.start()
        handler.postDelayed({ animatingColumns.remove(randomColumn) }, 3200)
    }
}